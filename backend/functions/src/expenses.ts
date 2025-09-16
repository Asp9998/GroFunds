import {onDocumentCreated} from 'firebase-functions/v2/firestore';
import * as admin from 'firebase-admin';
import { getOpenAI } from "./openaiClient"; 

/** Stable categories for charts/filters. Keep ≤12 top-level. */
export const CATEGORY_SET: Record<string, string[]> = {
  'Food & Drink': ['Groceries', 'Restaurants', 'Coffee & Tea', 'Snacks', 'Alcohol', 'Other'],
  'Shopping': ['Clothing', 'Electronics', 'Household', 'Furniture', 'Other'],
  'Transport': ['Fuel', 'Transit', 'Taxi', 'Parking', 'Tolls', 'Maintenance', 'Other'],
  'Bills': ['Phone', 'Internet', 'Utilities', 'Rent', 'Other'],
  'Debt & Loans': ['Credit Card Interest', 'Credit Card Principal', 'Student Loan', 'Auto Loan', 'Personal Loan', 'Other'],
  'Entertainment': ['Streaming', 'Movies', 'Games', 'Events', 'Hobbies', 'Other'],
  'Subscriptions & Services': ['Apps & Software', 'Cloud Storage', 'News & Magazines', 'Productivity', 'Other'],
  'Health & Fitness': ['Pharmacy', 'Doctor', 'Dental', 'Gym', 'Other'],
  'Travel': ['Flights', 'Hotel', 'Car Rental', 'Baggage', 'Other'],
  'Personal Care': ['Hair/Salon', 'Toiletries', 'Laundry', 'Skin Care', 'Body care', 'Other'],
  'Childcare & Family': ['Daycare', 'School Fees', 'Baby Supplies', 'Allowance', 'Other'],
  'Pets': ['Food', 'Vet', 'Grooming', 'Other'],
  'Insurance': ['Auto', 'Home', 'Health', 'Life', 'Other'],
  'Education': ['Tuition', 'Books', 'Courses', 'Library', 'Other'],
  'Gifts & Donations': ['Gifts', 'Charity', 'Other'],
  'Taxes': ['Income Tax', 'Property Tax', 'Sales Tax', 'Other'],
  'Other': ['Other']
};

type ExpenseExtraction = {
  amount: number | null;
  currency: string | null;
  category: string | null;
  subcategory: string | null;
  merchant: string | null;
  txnAt: string | null; // ISO 8601 or null
  notes: string | null;
  confidence: number | null; // 0..1
};

export const onExpenseCreated = onDocumentCreated(

    'users/{userId}/expenses/{expenseId}',
    async (event) => {
      const snap = event.data;
      if (!snap) return;

      const data = snap.data() as any;
      const input = (data.input ?? '').toString().trim();
      const status = (data.status ?? 'pending').toString();
      const currencyHint = (data.currencyHint ?? 'CAD').toString();
      const localeHint = (data.localeHint ?? 'en-CA').toString();

      if (!input) {
        await snap.ref.update({status: 'error', error: 'Empty input'});
        return;
      }
      if (status !== 'pending') return; // ignore already-processed docs

      try {
        const system = 
`
You extract expense details from a short, natural-language note for a Canadian user.

Return STRICT JSON (no prose, no markdown) with EXACT keys:
- amount: number | null       // numeric total paid; no currency symbols
- currency: string | null     // ISO 4217 (e.g., "CAD"); default to ${currencyHint} if ambiguous
- category: string | null     // one of: ${Object.keys(CATEGORY_SET).join(', ')}
- subcategory: string | null  // one of CATEGORY_SET[category]
- merchant: string | null     // trimmed brand/store/vendor name; null if unclear
- notes: string | null        // remainder text the user might care about (concise); null if none
- confidence: number | null   // 0..1 confidence of the entire parse

Inputs you receive:
- currencyHint: "${currencyHint}"  // user's default currency (e.g., "CAD")
- CATEGORY_SET: ${CATEGORY_SET}  // JSON map: { [category]: string[] of subcategories }
- SUBCAT_TO_CAT: CATEGORY_SET[category] // JSON map: { [subcategoryLower]: parentCategory }

GENERAL RULES
1) Output MUST be a single valid JSON object with the exact keys above. No extra keys, no trailing commas, no commentary.
2) Never hallucinate amount or date. If amount not explicitly present, amount=null and confidence<=0.4. If no date/time, txnAt=null.
3) Prefer a concrete category over "Other". Only use "Other" when nothing in CATEGORY_SET is reasonably close.
4) If the note text directly matches a valid subcategory (case-insensitive), assign that subcategory and its parent category automatically. Only use "Other" if the text contains nothing close to any valid subcategory.
4) If the text implies a refund/cashback/reimbursement:
   - If CATEGORY_SET contains "Refunds/Adjustments", set category="Refunds/Adjustments".
   - Else set amount as a NEGATIVE number and choose the most relevant category (e.g., the original purchase domain), and put "refund/cashback" in notes.
5) If the text implies a transfer or card payment (e.g., "paid credit card", "moved to savings"):
   - If CATEGORY_SET contains "Cash & Transfers (Exclude)", use that; otherwise pick the nearest category and include "transfer" in notes.

AMOUNT PARSING
A) Accept formats like:
   - "$20", "20$", "20 dollars", "CAD 20", "20 CAD", "20.00", "1,234.56"
   - Words: "twenty", "two hundred and ten"
   - Magnitudes: "20k", "20 thousand", "1.5k", "2 million"
   - Multiplicative: "3 x 4.99", "2 * 12", "2 coffees @ 4.50"
   - Totals with tip/tax: "20 + 3 tip", "subtotal 18, tip 2, total 20"
B) Choose the MOST LIKELY TOTAL PAID:
   - Prefer an explicit "total".
   - If subtotal + tip/tax are present, sum them.
   - For multiplicative expressions, multiply quantity by unit price.
   - Ignore percentages unless they affect a computed total that’s explicitly stated (e.g., "15% tip on 20" → infer 3 if that’s clearly the intended final).
C) Normalize to a number with a dot decimal separator (max 2 decimals).
D) If multiple plausible totals appear, pick the one most consistent with the note (e.g., closest to “total/paid/charged”), else pick the largest plausible total.

CURRENCY INFERENCE
- Detect from symbols/words: "$" + context “US”/“USD” = USD; “CAD”, “C$”, “CA$” = CAD; “US$”=USD; “€”=EUR, etc.
- If ambiguous, default to currencyHint and set a slightly lower confidence if currency wasn’t explicit.
- Always output uppercase 3-letter code.


CATEGORY & SUBCATEGORY RESOLUTION
1) If the text strongly matches a subcategory by name or synonym, set subcategory accordingly and set category = SUBCAT_TO_CAT[subcategoryLower].
2) If only a category-level concept appears, set category and select the best-fitting subcategory from CATEGORY_SET[category]; if none, use "Other".
3) Avoid category="Other" in 99% of cases—prefer the closest valid category using synonyms and merchant hints.
4) Coffee rule: If it suggests coffee/latte/espresso/cappuccino, prefer category="Food & Drink", subcategory close to "Coffee & Tea".
5) Merchant hints (examples):
   - "Uber", "Lyft" → Transport/Taxi
   - "Shell", "Petro-Canada", "Esso" → Transport/Fuel
   - "TTC", "Transit", "Bus", "Subway" → Transport/Transit
   - "Starbucks", "Tim Hortons" → Food & Drink/Coffee & Tea
   - "McDonald's", "Subway (restaurant)", "KFC" → Food & Drink/Restaurants
   - "Walmart", "Costco", "No Frills", "Superstore" → Food & Drink/Groceries (unless the note clearly says non-grocery)
   - "Rogers", "Bell", "Telus" → Bills/Phone or Internet (as applicable)
   - "Netflix", "Disney+", "Spotify" → Entertainment/Streaming
   Adjust to closest available subcategory names in CATEGORY_SET.

MERCHANT
- Extract a concise merchant name (brand or store). Remove descriptors like “store”, “inc.”, “ltd.” unless part of common branding.
- If unclear, merchant=null.

NOTES
- Include only meaningful remainder (e.g., “team lunch”, “birthday”, “work trip”, “refund”, “with tip”).
- Keep concise; null if nothing useful remains.

CONFIDENCE (0..1)
- Start from 1.0 and subtract for each ambiguity:
  - Amount missing/uncertain: ≤0.4
  - Currency not explicit (used default): −0.05..0.15
  - Category inferred weakly: −0.1..0.2
  - Merchant unclear: −0.05
  - Very noisy text or conflicts: −0.1..0.3
- Round to 2 decimals; clamp 0..1.

VALIDATION
- Keys must exist even if null.
- amount must be a number or null (not a string).
- currency must be a 3-letter code or null.
- category must be in CATEGORY_SET keys when not null.
- subcategory must be in CATEGORY_SET[category] or "Other".
- Return only the JSON object.

EXAMPLES

INPUT: "Starbucks latte $5.25 yesterday 7pm"
OUTPUT:
{"amount":5.25,"currency":"${currencyHint}","category":"Food & Drink","subcategory":"Coffee & Tea","merchant":"Starbucks","txnAt":"2025-09-09T19:00:00-05:00","notes":null,"confidence":0.96}

INPUT: "Uber to airport 18 + 3 tip"
OUTPUT:
{"amount":21,"currency":"${currencyHint}","category":"Transport","subcategory":"Taxi","merchant":"Uber","txnAt":null,"notes":"with tip","confidence":0.9}

INPUT: "Groceries 2 x 14.99 at Walmart on 2025-09-02"
OUTPUT:
{"amount":29.98,"currency":"${currencyHint}","category":"Food & Drink","subcategory":"Groceries","merchant":"Walmart","txnAt":"2025-09-02T00:00:00-05:00","notes":null,"confidence":0.94}

INPUT: "paid 2 hundred and 10 for car wash"
OUTPUT:
{"amount":210,"currency":"${currencyHint}","category":"Transport","subcategory":"Other","merchant":null,"txnAt":null,"notes":"car wash","confidence":0.78}

INPUT: "CAD 45 Rogers internet"
OUTPUT:
{"amount":45,"currency":"CAD","category":"Bills","subcategory":"Internet","merchant":"Rogers","txnAt":null,"notes":null,"confidence":0.93}

INPUT: "Refund from Amazon 29.99"
OUTPUT:
{"amount":-29.99,"currency":"${currencyHint}","category":"Shopping","subcategory":"Other","merchant":"Amazon","txnAt":null,"notes":"refund","confidence":0.82}
`

.trim();

        const user = `Input: "${input}"\nLocale: ${localeHint}`;
        const openai = getOpenAI();

        const completion = await openai.chat.completions.create({
          model: 'gpt-4o-mini',
          temperature: 0,
          response_format: {type: 'json_object'},
          messages: [
            {role: 'system', content: system},
            {role: 'user', content: user},
          ],
        });

        const raw = completion.choices[0]?.message?.content ?? '{}';
        let parsed: ExpenseExtraction;
        try {
          parsed = JSON.parse(raw) as ExpenseExtraction;
        } catch {
          parsed = {
            amount: null, currency: currencyHint, category: 'Other',
            subcategory: 'Other', merchant: null, txnAt: null,
            notes: null, confidence: 0.2,
          };
        }

        // Normalize
        const amount = typeof parsed.amount === 'number' ? parsed.amount : null;
        const currency = (parsed.currency || currencyHint).toString().toUpperCase();
        const category = (parsed.category && CATEGORY_SET[parsed.category]) ? parsed.category : 'Other';
        const subcategory = (parsed.subcategory && CATEGORY_SET[category]?.includes(parsed.subcategory)) ?
        parsed.subcategory : 'Other';
        const merchant = parsed.merchant || null;
        const txnAt = parsed.txnAt || null;
        const notes = parsed.notes || '';
        const confidence = Math.max(0, Math.min(1, parsed.confidence ?? 0.5));

        await snap.ref.update({
          status: 'processed',
          amount, currency,
          category, subcategory,
          merchant, txnAt,
          notes,
          ai: {model: 'gpt-4o-mini', promptVersion: 1, confidence},
          processedAt: admin.firestore.FieldValue.serverTimestamp(),
          error: null,
        });
      } catch (e: any) {
        console.error('Expense AI extraction failed:', e);
        await snap.ref.update({status: 'error', error: e?.message?.toString() ?? 'Unknown'});
      }
    },
);
