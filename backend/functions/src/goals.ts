import {onDocumentCreated} from 'firebase-functions/v2/firestore';
import * as admin from 'firebase-admin';
import { getOpenAI } from "./openaiClient"; 

/** Stable goal types for clean reporting. Keep this short. */
const GOAL_TYPES = [
  'Emergency Fund', 'Down Payment', 'Home Purchase', 'Home Renovation', 'Car Purchase',
  'Education', 'Retirement/Investing', 'Travel', 'Wedding', 'Gadget/Tech', 'Furniture/Appliances',
  'Medical/Health', 'Baby/Child Fund', 'Pet Fund', 'Moving/Relocation', 'Taxes', 'Charity/Giving',
  'Debt Payoff', 'Business', 'Other',
] as const;

type GoalExtraction = {
  title: string | null; // short human title e.g., "Tuition Fees Year 1"
  goalType: string | null; // one of GOAL_TYPES
  targetAmount: number | null; // numeric only
  currency: string | null; // ISO 4217 (CAD, USD, …)
  dueDate: string | null; // ISO-8601 (YYYY-MM-DD or datetime) or null
  startAmount: number | null; // if user mentioned they’ve saved some
  notes: string | null;
  confidence: number | null; // 0..1
};

function monthsUntil(from: Date, to: Date): number {
  let months = (to.getFullYear() - from.getFullYear()) * 12 + (to.getMonth() - from.getMonth());
  // if due-day earlier than today’s day, subtract one month
  if (to.getDate() < from.getDate()) months -= 1;
  return Math.max(0, months);
}

export const onGoalCreated = onDocumentCreated(
    'users/{userId}/goals/{goalId}',
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
      if (status !== 'pending') return;

      try {
        const today = new Date();
        const todayISO = today.toISOString().slice(0, 10); // YYYY-MM-DD

        const system = `
Extract a financial GOAL from a short, natural-language note for a Canadian user.

Return STRICT JSON (no prose, no markdown) with keys:
- title: string | null            // concise human-readable label (e.g., "New Car", "Trip to Japan")
- goalType: string | null         // EXACTLY one of: ${GOAL_TYPES.join(', ')}
- targetAmount: number | null     // numeric only; no currency symbols
- currency: string | null         // ISO 4217 (e.g., "CAD"); default to ${currencyHint} if ambiguous
- dueDate: string | null          // ISO-8601 if a date/timeframe is present; else null
- startAmount: number | null      // existing savings toward this goal; else null
- notes: string | null            // optional remainder text
- confidence: number | null       // 0..1 certainty of the parse

Rules:
1. Output MUST be a valid JSON object. No prose, no markdown.
2. Never hallucinate values:
   - If targetAmount missing, targetAmount=null.
   - If dueDate not present, dueDate=null.
   - If startAmount not present, startAmount=null.
3. Amount parsing:
   - Accept "$5000", "5k", "five thousand", "CAD 5,000", etc.
   - Normalize to numeric value (max 2 decimals).
   - If both target and current savings are mentioned, assign target → targetAmount, current → startAmount.
4. Currency:
   - Detect from symbols/words (CAD, USD, $, US$, C$).
   - Default to ${currencyHint} if ambiguous.
   - Always uppercase 3-letter ISO.
5. Due dates:
   - Parse explicit dates (“Dec 2025”, “2026-01-01”).
   - Parse relative phrases (“in 18 months”, “next year”) into ISO using "today=${todayISO}".
   - If ambiguous, set dueDate=null.
6. Goal type:
   - Match closely to GOAL_TYPES.
   - If sub-type mentioned (e.g., "RRSP" → "Retirement/Investing", "down payment" → "Down Payment"), map accordingly.
   - Avoid "Other" unless no match is reasonable.
7. Title:
   - Short, meaningful phrase (e.g., "Trip to Japan", "New Car").
   - If nothing specific, use goalType (e.g., "Emergency Fund").
8. Notes:
   - Keep concise remainder info (e.g., “for baby due in April”).
   - Null if nothing useful.
9. Confidence:
   - Start at 1.0.
   - Reduce for each ambiguity:
     • Missing amount → ≤0.6
     • Currency defaulted → −0.05..0.15
     • Type inferred weakly → −0.1..0.2
     • DueDate unclear → −0.05..0.15
   - Clamp to 0..1, round to 2 decimals.

Validation:
- Keys must exist even if null.
- goalType must be from GOAL_TYPES or null.
- amount fields must be numbers or null, never strings.
- dueDate must be ISO string or null.
- currency must be ISO 3-letter or null.

`.trim();

        const userMsg = `Input: "${input}"\nLocale: ${localeHint}\nToday: ${todayISO}`;
        const openai = getOpenAI();

        const completion = await openai.chat.completions.create({
          model: 'gpt-4o-mini',
          temperature: 0,
          response_format: {type: 'json_object'},
          messages: [
            {role: 'system', content: system},
            {role: 'user', content: userMsg},
          ],
        });

        const raw = completion.choices[0]?.message?.content ?? '{}';
        let parsed: GoalExtraction;

        try {
          parsed = JSON.parse(raw) as GoalExtraction;
        } catch (_e) {
          parsed = {
            title: null, goalType: 'Other', targetAmount: null,
            currency: currencyHint, dueDate: null, startAmount: null,
            notes: null, confidence: 0.5,
          };
        }

        // Normalize + guardrails
        const title = (parsed.title ?? '').toString().trim() || `Goal: ${input.slice(0, 60)}`;
        const goalType = (parsed.goalType && (GOAL_TYPES as readonly string[]).includes(parsed.goalType)) ?
        parsed.goalType : 'Other';
        const targetAmount = typeof parsed.targetAmount === 'number' && parsed.targetAmount >= 0 ?
        parsed.targetAmount : null;
        const currency = (parsed.currency || currencyHint).toString().toUpperCase();

        let dueDateISO: string | null = null;
        if (parsed.dueDate) {
          const d = new Date(parsed.dueDate);
          if (!Number.isNaN(d.valueOf())) {
          // store date-only ISO if time not needed
            dueDateISO = d.toISOString();
          }
        }

        const startAmount = typeof parsed.startAmount === 'number' && parsed.startAmount >= 0 ?
        parsed.startAmount : 0;
        const notes = (parsed.notes ?? '') as string;
        const confidence = Math.max(0, Math.min(1, parsed.confidence ?? 0.5));

        // Derived fields: monthsRemaining, monthlyNeeded
        let monthsRemaining: number | null = null;
        let monthlyNeeded: number | null = null;

        if (targetAmount !== null && dueDateISO) {
          const due = new Date(dueDateISO);
          const m = monthsUntil(today, due);
          monthsRemaining = m;
          if (m > 0) {
            const remaining = Math.max(0, targetAmount - startAmount);
            monthlyNeeded = Math.round((remaining / m) * 100) / 100; // 2 decimals
          } else {
          // due date now/past → one-shot target
            monthlyNeeded = Math.max(0, targetAmount - startAmount);
          }
        }

        await snap.ref.update({
          status: 'processed',
          title,
          goalType,
          targetAmount,
          currency,
          dueDate: dueDateISO, // ISO string or null
          startAmount,
          currentAmount: data.currentAmount ?? 0, // keep user-progress if they prefilled
          monthlyNeeded,
          monthsRemaining,
          notes,
          ai: {model: 'gpt-4o-mini', promptVersion: 1, confidence},
          processedAt: admin.firestore.FieldValue.serverTimestamp(),
          error: null,
        });
      } catch (e: any) {
        console.error('Goal AI extraction failed:', e);
        await snap.ref.update({status: 'error', error: e?.message?.toString() ?? 'Unknown'});
      }
    },
);
