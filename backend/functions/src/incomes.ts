import {onDocumentCreated} from 'firebase-functions/v2/firestore';
import * as admin from 'firebase-admin';
import { getOpenAI } from "./openaiClient"; 

/** Keep income types stable for reports. */
const INCOME_TYPES = [
  'Salary', 'Overtime', 'Bonus', 'Commission', 'Tips', 'Stock Compensation', 'Freelance/Contract',
  'Business', 'Rental', 'Interest', 'Dividend', 'Capital Gains', 'Royalties', 'Pension/Annuity',
  'Government Benefits', 'Alimony/Child Support', 'Gifts', 'Prize/Award', 'Cashback/Rebate', 'Other'
] as const;

type IncomeExtraction = {
  amount: number | null;
  currency: string | null; // ISO-4217 like "CAD"
  type: string | null; // one of INCOME_TYPES
  source: string | null; // employer/bank/etc.
  txnAt: string | null; // ISO-8601 or null
  notes: string | null;
  confidence: number | null; // 0..1
};

export const onIncomeCreated = onDocumentCreated(
    'users/{userId}/incomes/{incomeId}',
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
`Extract INCOME details from a short, natural-language note for a Canadian user.

Return STRICT JSON (no prose, no markdown) with keys:
- amount: number | null       // numeric amount only; no symbols
- currency: string | null     // ISO 4217 (e.g., "CAD"); default to ${currencyHint} if ambiguous
- type: string | null         // EXACTLY one of: ${INCOME_TYPES.join(', ')}
- notes: string | null        // optional remainder text
- confidence: number | null   // 0..1 certainty of the entire parse

Rules:
1. Output MUST be a single valid JSON object. No commentary, no markdown.
2. Never hallucinate an amount. If no explicit number is present, set amount=null and confidence<=0.4.
3. Normalize currency to uppercase 3-letter ISO code (e.g., CAD, USD, EUR).
4. If multiple numeric values appear:
   - Prefer an explicit "total", "net pay", "deposit", or "received" amount.
   - If hourly rate + hours are shown (e.g., "20/hr for 8 hours"), compute the total (20 * 8 = 160).
   - Ignore irrelevant numbers (dates, IDs, tax rates).
5. Type selection:
   - Use the closest match in INCOME_TYPES.
   - If user text matches a sub-type (e.g., "paycheck" → "Salary", "cashback" → "Cashback/Rebate"), map accordingly.
   - If a refund or reimbursement is clearly income, use type="Refund".
6. Notes: include only concise remainder info (e.g., "for project X", "from RBC"), or null if nothing useful.
7. Confidence scoring:
   - Start at 1.0, reduce for each ambiguity:
     • Missing/uncertain amount → ≤0.4
     • Currency defaulted (ambiguous) → −0.05..0.15
     • Type inferred weakly → −0.1..0.2
     • Very noisy text → −0.1..0.3
   - Round to 2 decimals, clamp 0..1.

Validation:
- Keys must exist even if null.
- amount must be number or null (not string).
- currency must be 3-letter ISO code or null.
- type must be from INCOME_TYPE_ENUM or null.
- notes can be null if not needed.
- confidence always required.`.trim();

        const openai = getOpenAI();
        const completion = await openai.chat.completions.create({
          model: 'gpt-4o-mini',
          temperature: 0,
          response_format: {type: 'json_object'},
          messages: [
            {role: 'system', content: system},
            {role: 'user', content: `Input: "${input}"\nLocale: ${localeHint}`},
          ],
        });

        const raw = completion.choices[0]?.message?.content ?? '{}';
        // AFTER (no empty catch; set fallback here)
        let parsed: IncomeExtraction;
        try {
          parsed = JSON.parse(raw) as IncomeExtraction;
        } catch (_e) {
          parsed = {
            amount: null, currency: currencyHint, type: 'Other', source: null,
            txnAt: null, notes: null, confidence: 0.5,
          };
        }

        // Normalize + guardrails
        const amount = typeof parsed.amount === 'number' ? parsed.amount : null;
        const currency = (parsed.currency || currencyHint).toUpperCase();
        const type = (parsed.type && INCOME_TYPES.includes(parsed.type as any)) ?
        parsed.type : 'Other';
        const source = parsed.source || null;
        const txnAt = parsed.txnAt || null;
        const notes = parsed.notes || '';
        const confidence = Math.max(0, Math.min(1, parsed.confidence ?? 0.5));

        await snap.ref.update({
          status: 'processed',
          amount, currency, type, source, txnAt, notes,
          ai: {model: 'gpt-4o-mini', promptVersion: 1, confidence},
          processedAt: admin.firestore.FieldValue.serverTimestamp(),
          error: null,
        });
      } catch (e: any) {
        console.error('Income AI extraction failed:', e);
        await snap.ref.update({
          status: 'error',
          error: e?.message?.toString() ?? 'Unknown',
        });
      }
    },
);
