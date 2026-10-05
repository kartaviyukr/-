import type { Character, StoryChapter, Task } from '@/types';

/**
 * Обращение к нейросети прямо из приложения.
 *
 * Ключ вводится в настройках и хранится у пары, поэтому edge-функция для сюжета
 * не обязательна. Достаточно, чтобы глава сгенерировалась на одном телефоне —
 * она сохраняется в базу и приезжает партнёру сама.
 *
 * Оговорка про веб-версию: браузер отправит запрос только если API отвечает
 * нужными CORS-заголовками. DeepSeek их не шлёт, поэтому в PWA генерация
 * упадёт — пишем об этом понятным текстом, а не «Network request failed».
 */

export type LlmProvider = 'deepseek' | 'openrouter' | 'groq';

interface ProviderInfo {
  label: string;
  endpoint: string;
  defaultModel: string;
  keysUrl: string;
  keyPrefix?: string;
}

export const providers: Record<LlmProvider, ProviderInfo> = {
  deepseek: {
    label: 'DeepSeek',
    endpoint: 'https://api.deepseek.com/chat/completions',
    defaultModel: 'deepseek-chat',
    keysUrl: 'https://platform.deepseek.com/api_keys',
    keyPrefix: 'sk-',
  },
  openrouter: {
    label: 'OpenRouter',
    endpoint: 'https://openrouter.ai/api/v1/chat/completions',
    defaultModel: 'deepseek/deepseek-chat-v3-0324:free',
    keysUrl: 'https://openrouter.ai/keys',
  },
  groq: {
    label: 'Groq',
    endpoint: 'https://api.groq.com/openai/v1/chat/completions',
    defaultModel: 'llama-3.3-70b-versatile',
    keysUrl: 'https://console.groq.com/keys',
  },
};

export class LlmError extends Error {}

export interface LlmSettings {
  provider: LlmProvider;
  apiKey: string | null;
  model: string | null;
}

const SYSTEM = `Ты — мастер-рассказчик уютной фэнтезийной RPG про двух героев-напарников,
которые в реальной жизни пара. Ты превращаешь их бытовые дела в приключение.

Правила:
- Пиши по-русски, живо и с тёплым юмором, без пошлости.
- Не поучай и не давай советов. Ты рассказчик, а не коуч.
- Опирайся на реальные задачи из списка: превращай их в события сюжета.
- Держись преемственности с предыдущими главами.
- Отвечай ТОЛЬКО валидным JSON, без markdown-обёртки и пояснений.`;

interface ChatMessage {
  role: 'system' | 'user';
  content: string;
}

async function chat(settings: LlmSettings, messages: ChatMessage[], maxTokens: number): Promise<string> {
  if (!settings.apiKey) {
    throw new LlmError('Ключ нейросети не задан. Откройте «Герой» → «Нейросеть» и вставьте его.');
  }

  const info = providers[settings.provider] ?? providers.deepseek;
  const model = settings.model || info.defaultModel;

  let response: Response;
  try {
    response = await fetch(info.endpoint, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        Authorization: `Bearer ${settings.apiKey}`,
      },
      body: JSON.stringify({ model, messages, temperature: 0.9, max_tokens: maxTokens }),
    });
  } catch {
    throw new LlmError(
      `Не удалось связаться с ${info.label}. В веб-версии это обычное дело: браузер не выпускает ` +
        'такие запросы. Сгенерируйте главу на телефоне с установленным приложением — она появится у обоих.',
    );
  }

  if (response.status === 401 || response.status === 403) {
    throw new LlmError(`${info.label} не принял ключ. Проверьте, что он скопирован целиком и не отозван.`);
  }
  if (response.status === 402) {
    throw new LlmError(`На балансе ${info.label} закончились средства.`);
  }
  if (response.status === 429) {
    throw new LlmError(`${info.label} просит подождать: слишком много запросов подряд.`);
  }
  if (!response.ok) {
    throw new LlmError(`${info.label} ответил ошибкой ${response.status}.`);
  }

  const json = await response.json();
  const text = json?.choices?.[0]?.message?.content;
  if (typeof text !== 'string' || !text.trim()) {
    throw new LlmError(`${info.label} вернул пустой ответ.`);
  }
  return text.trim();
}

/** Достаёт JSON из ответа модели, даже если он завёрнут в ```-блок. */
function parseJson<T>(raw: string): T | null {
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

function describeHeroes(characters: Character[]): string {
  const list = characters.map((c) => `${c.avatar_emoji} ${c.name} (${c.hero_class}, уровень ${c.level})`);
  return list.join(' и ') || 'два безымянных героя';
}

export interface ChapterDraft {
  title: string;
  body: string;
  cliffhanger: string | null;
}

export async function writeChapter(
  settings: LlmSettings,
  input: { characters: Character[]; tasks: Task[]; previous: StoryChapter[]; chapterNo: number },
): Promise<ChapterDraft> {
  const done = input.tasks.filter((t) => t.status === 'completed');
  const failed = input.tasks.filter((t) => t.status === 'failed');
  const open = input.tasks.filter((t) => t.status !== 'completed' && t.status !== 'failed');

  const previous =
    input.previous
      .slice()
      .reverse()
      .map((c) => `Глава ${c.chapter_no}: ${c.title}\n${c.body}${c.cliffhanger ? `\n(${c.cliffhanger})` : ''}`)
      .join('\n\n') || 'Это самая первая глава — начни историю с завязки.';

  const raw = await chat(
    settings,
    [
      { role: 'system', content: SYSTEM },
      {
        role: 'user',
        content: `Герои: ${describeHeroes(input.characters)}.

Предыдущие главы:
${previous}

Что герои сделали за последнее время:
Выполнено: ${done.map((t) => t.title).join('; ') || 'пока ничего'}
Провалено: ${failed.map((t) => t.title).join('; ') || 'ничего'}
В работе: ${open.map((t) => t.title).join('; ') || 'ничего'}

Напиши главу ${input.chapterNo}: 150–220 слов. Победы героев должны продвигать сюжет,
провалы — создавать трудности. Закончи интригой в поле cliffhanger.
JSON: {"title": "...", "body": "...", "cliffhanger": "..."}`,
      },
    ],
    900,
  );

  const parsed = parseJson<ChapterDraft>(raw);
  if (!parsed?.body) throw new LlmError('Модель вернула ответ не в том формате. Попробуйте ещё раз.');

  return {
    title: parsed.title || `Глава ${input.chapterNo}`,
    body: parsed.body,
    cliffhanger: parsed.cliffhanger || null,
  };
}

export interface QuestFlavor {
  quest_title: string;
  quest_intro: string;
}

export async function writeQuestFlavor(
  settings: LlmSettings,
  input: { characters: Character[]; task: Pick<Task, 'title' | 'description' | 'difficulty'> },
): Promise<QuestFlavor> {
  const raw = await chat(
    settings,
    [
      { role: 'system', content: SYSTEM },
      {
        role: 'user',
        content: `Герои: ${describeHeroes(input.characters)}.
Реальная задача: "${input.task.title}"${input.task.description ? `. Детали: ${input.task.description}` : ''}.
Сложность: ${input.task.difficulty}.

Придумай для неё фэнтезийное название квеста и завязку на 2–3 предложения.
JSON: {"quest_title": "...", "quest_intro": "..."}`,
      },
    ],
    400,
  );

  const parsed = parseJson<QuestFlavor>(raw);
  if (!parsed?.quest_title) throw new LlmError('Модель вернула ответ не в том формате.');
  return parsed;
}
