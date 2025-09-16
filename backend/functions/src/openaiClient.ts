import OpenAI from "openai";
import { defineSecret } from "firebase-functions/params";

export const OPENAI_API_KEY = defineSecret("OPENAI_API_KEY");

let _client: OpenAI | null = null;

export function getOpenAI(): OpenAI {
  if (_client) return _client;
  const key = OPENAI_API_KEY.value();  // pulls from Secret Manager in prod, env var in emulator
  if (!key) throw new Error("Missing OPENAI_API_KEY secret/env.");
  _client = new OpenAI({ apiKey: key });
  return _client;
}

