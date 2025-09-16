// default/src/index.ts
import {setGlobalOptions} from 'firebase-functions/v2';
import {onRequest} from 'firebase-functions/v2/https';
import * as admin from 'firebase-admin';
import { OPENAI_API_KEY } from "./openaiClient";

admin.initializeApp();

setGlobalOptions({
  region: "northamerica-northeast2",
  minInstances: 0,
  maxInstances: 10,
  memory: '256MiB',
  timeoutSeconds: 60,
  concurrency: 80,
  secrets: [OPENAI_API_KEY],
});

export const health = onRequest((_req, res) => {
  res.status(200).send('OK');
});
export {onExpenseCreated} from './expenses';
export {onIncomeCreated} from './incomes';
export {onGoalCreated} from './goals';


