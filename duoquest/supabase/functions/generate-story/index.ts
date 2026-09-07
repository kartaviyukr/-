// Генерация RPG-сюжета и «легенды» для квестов.
// Развернуть: supabase functions deploy generate-story
import { createClient } from 'jsr:@supabase/supabase-js@2';
import { corsHeaders, json } from '../_shared/cors.ts';
import { chat, LlmNotConfigured, parseJson } from '../_shared/llm.ts';

interface Body {
  mode: 'chapter' | 'quest_flavor';
  couple_id: string;
  task_id?: string | null;
}

const SYSTEM = `Ты — мастер-рассказчик уютной фэнтезийной RPG про двух героев-напарников,
которые в реальной жизни пара. Ты превращаешь их бытовые дела в приключение.

Правила:
- Пиши по-русски, живо и с тёплым юмором, без пошлости.
- Не поучай и не давай советов. Ты рассказчик, а не коуч.
- Опирайся на реальные задачи из списка: превращай их в события сюжета.
- Держись преемственности с предыдущими главами.
- Отвечай ТОЛЬКО валидным JSON, без markdown-обёртки и пояснений.`;

Deno.serve(async (req) => {
  if (req.method === 'OPTIONS') return new Response('ok', { headers: corsHeaders });

  try {
    const body = (await req.json()) as Body;
    if (!body?.couple_id) return json({ error: 'couple_id обязателен' }, 400);

    const authHeader = req.headers.get('Authorization') ?? '';
    const supabaseUrl = Deno.env.get('SUPABASE_URL')!;

    // Клиент от имени пользователя — только чтобы проверить, что он в этой паре.
    const asUser = createClient(supabaseUrl, Deno.env.get('SUPABASE_ANON_KEY')!, {
      global: { headers: { Authorization: authHeader } },
    });
    const { data: userData } = await asUser.auth.getUser();
    if (!userData?.user) return json({ error: 'Не авторизован' }, 401);

    const { data: membership } = await asUser
      .from('couple_members')
      .select('couple_id')
      .eq('couple_id', body.couple_id)
      .eq('user_id', userData.user.id)
      .maybeSingle();
    if (!membership) return json({ error: 'Нет доступа к этой паре' }, 403);

    // Запись — сервисным ключом, чтобы не зависеть от нюансов RLS.
    const admin = createClient(supabaseUrl, Deno.env.get('SUPABASE_SERVICE_ROLE_KEY')!);

    const [{ data: characters }, { data: recentTasks }, { data: lastChapters }] = await Promise.all([
      admin.from('characters').select('name, hero_class, level, avatar_emoji').eq('couple_id', body.couple_id),
      admin
        .from('tasks')
        .select('title, description, status, difficulty, reward_text, penalty_text, completed_at')
        .eq('couple_id', body.couple_id)
        .order('updated_at', { ascending: false })
        .limit(12),
      admin
        .from('story_chapters')
        .select('chapter_no, title, body, cliffhanger')
        .eq('couple_id', body.couple_id)
        .order('chapter_no', { ascending: false })
        .limit(3),
    ]);

    const heroes = (characters ?? [])
      .map((c) => `${c.avatar_emoji} ${c.name} (${c.hero_class}, уровень ${c.level})`)
      .join(' и ') || 'два безымянных героя';

    // ---------------------------------------------------- легенда квеста ----
    if (body.mode === 'quest_flavor') {
      if (!body.task_id) return json({ error: 'task_id обязателен' }, 400);
      const { data: task } = await admin
        .from('tasks')
        .select('title, description, difficulty')
        .eq('id', body.task_id)
        .maybeSingle();
      if (!task) return json({ error: 'Квест не найден' }, 404);

      const raw = await chat(
        [
          { role: 'system', content: SYSTEM },
          {
            role: 'user',
            content: `Герои: ${heroes}.
Реальная задача: "${task.title}"${task.description ? `. Детали: ${task.description}` : ''}.
Сложность: ${task.difficulty}.

Придумай для неё фэнтезийное название квеста и завязку на 2–3 предложения.
JSON: {"quest_title": "...", "quest_intro": "..."}`,
          },
        ],
        300,
      );

      const parsed = parseJson<{ quest_title: string; quest_intro: string }>(raw);
      if (!parsed?.quest_title) return json({ error: 'Модель вернула не JSON', raw }, 502);

      await admin
        .from('tasks')
        .update({ quest_title: parsed.quest_title, quest_intro: parsed.quest_intro })
        .eq('id', body.task_id);

      return json({ ok: true, ...parsed });
    }

    // ------------------------------------------------------- новая глава ----
    const done = (recentTasks ?? []).filter((t) => t.status === 'completed');
    const failed = (recentTasks ?? []).filter((t) => t.status === 'failed');
    const open = (recentTasks ?? []).filter((t) => t.status !== 'completed' && t.status !== 'failed');

    const previous = (lastChapters ?? [])
      .slice()
      .reverse()
      .map((c) => `Глава ${c.chapter_no}: ${c.title}\n${c.body}${c.cliffhanger ? `\n(${c.cliffhanger})` : ''}`)
      .join('\n\n') || 'Это самая первая глава — начни историю с завязки.';

    const nextNo = ((lastChapters ?? [])[0]?.chapter_no ?? 0) + 1;

    const raw = await chat([
      { role: 'system', content: SYSTEM },
      {
        role: 'user',
        content: `Герои: ${heroes}.

Предыдущие главы:
${previous}

Что герои сделали за последнее время:
Выполнено: ${done.map((t) => t.title).join('; ') || 'пока ничего'}
Провалено: ${failed.map((t) => t.title).join('; ') || 'ничего'}
В работе: ${open.map((t) => t.title).join('; ') || 'ничего'}

Напиши главу ${nextNo}: 150–220 слов. Победы героев должны продвигать сюжет,
провалы — создавать трудности. Закончи интригой в поле cliffhanger.
JSON: {"title": "...", "body": "...", "cliffhanger": "..."}`,
      },
    ]);

    const parsed = parseJson<{ title: string; body: string; cliffhanger?: string }>(raw);
    if (!parsed?.body) return json({ error: 'Модель вернула не JSON', raw }, 502);

    const { data: chapter, error } = await admin
      .from('story_chapters')
      .insert({
        couple_id: body.couple_id,
        chapter_no: nextNo,
        title: parsed.title ?? `Глава ${nextNo}`,
        body: parsed.body,
        cliffhanger: parsed.cliffhanger ?? null,
        triggered_by: body.task_id ?? null,
      })
      .select()
      .single();

    if (error) throw error;
    return json({ ok: true, chapter });
  } catch (e) {
    if (e instanceof LlmNotConfigured) return json({ error: e.message }, 503);
    console.error('generate-story:', e);
    return json({ error: String(e) }, 500);
  }
});
