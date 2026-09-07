/**
 * Тонкая обёртка над бесплатными LLM-провайдерами.
 * Все три ниже имеют бесплатный тариф, которого для сюжета в приложении на двоих хватает с запасом.
 *
 * Настраивается переменными окружения edge-функции:
 *   LLM_PROVIDER — groq | openrouter | gemini   (по умолчанию groq)
 *   LLM_API_KEY  — ключ выбранного провайдера
 *   LLM_MODEL    — необязательно, если хотите другую модель
 */

export type Provider = 'groq' | 'openrouter' | 'gemini';

const DEFAULT_MODEL: Record<Provider, string> = {
  groq: 'llama-3.3-70b-versatile',
  openrouter: 'meta-llama/llama-3.3-70b-instruct:free',
  gemini: 'gemini-2.0-flash',
};

export interface ChatMessage {
  role: 'system' | 'user';
  content: string;
}

export class LlmNotConfigured extends Error {
  constructor() {
    super('LLM_API_KEY не задан — сюжет генерироваться не будет');
  }
}

/** Отправляет запрос выбранному провайдеру и возвращает текст ответа. */
export async function chat(messages: ChatMessage[], maxTokens = 700): Promise<string> {
  const provider = (Deno.env.get('LLM_PROVIDER') ?? 'groq').toLowerCase() as Provider;
  const apiKey = Deno.env.get('LLM_API_KEY');
  const model = Deno.env.get('LLM_MODEL') ?? DEFAULT_MODEL[provider] ?? DEFAULT_MODEL.groq;

  if (!apiKey) throw new LlmNotConfigured();

  if (provider === 'gemini') {
    const system = messages.filter((m) => m.role === 'system').map((m) => m.content).join('\n\n');
    const user = messages.filter((m) => m.role === 'user').map((m) => m.content).join('\n\n');

    const res = await fetch(
      `https://generativelanguage.googleapis.com/v1beta/models/${model}:generateContent?key=${apiKey}`,
      {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          systemInstruction: system ? { parts: [{ text: system }] } : undefined,
          contents: [{ role: 'user', parts: [{ text: user }] }],
          generationConfig: { temperature: 0.9, maxOutputTokens: maxTokens },
        }),
      },
    );
    if (!res.ok) throw new Error(`Gemini ${res.status}: ${await res.text()}`);
    const json = await res.json();
    return json?.candidates?.[0]?.content?.parts?.[0]?.text?.trim() ?? '';
  }

  // Groq и OpenRouter говорят на диалекте OpenAI Chat Completions.
  const endpoint =
    provider === 'openrouter'
      ? 'https://openrouter.ai/api/v1/chat/completions'
      : 'https://api.groq.com/openai/v1/chat/completions';

  const res = await fetch(endpoint, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      Authorization: `Bearer ${apiKey}`,
      ...(provider === 'openrouter' ? { 'X-Title': 'DuoQuest' } : {}),
    },
    body: JSON.stringify({ model, messages, temperature: 0.9, max_tokens: maxTokens }),
  });

  if (!res.ok) throw new Error(`${provider} ${res.status}: ${await res.text()}`);
  const json = await res.json();
  return json?.choices?.[0]?.message?.content?.trim() ?? '';
}

/** Достаёт JSON из ответа модели, даже если он завёрнут в ```-блок. */
export function parseJson<T>(raw: string): T | null {
  const cleaned = raw.replace(/^```(?:json)?/i, '').replace(/```$/, '').trim();
  try {
    return JSON.parse(cleaned) as T;
  } catch {
    const match = cleaned.match(/\{[\s\S]*\}/);
    if (!match) return null;
    try {
      return JSON.parse(match[0]) as T;
    } catch {
      return null;
    }
  }
}
