import { create } from 'zustand';
import { supabase } from '@/lib/supabase';
import { syncAllReminders, syncTaskReminders } from '@/lib/notifications';
import { applyPenalty, applyReward, rewardFor } from '@/lib/game';
import { LlmError, writeChapter, writeQuestFlavor, type LlmProvider, type LlmSettings } from '@/lib/llm';
import { useSession } from '@/store/useSession';
import type { Character, Checkpoint, LedgerEntry, StoryChapter, Task, TaskStatus } from '@/types';

/** Настройки нейросети, введённые в приложении. */
function llmSettings(): LlmSettings {
  const couple = useSession.getState().couple;
  return {
    provider: (couple?.llm_provider as LlmProvider) || 'deepseek',
    apiKey: couple?.llm_api_key ?? null,
    model: couple?.llm_model ?? null,
  };
}

/** Персонажи пары — нейросети нужны их имена и классы для сюжета. */
function knownCharacters(): Character[] {
  const { character, partnerCharacter } = useSession.getState();
  return [character, partnerCharacter].filter((c): c is Character => Boolean(c));
}

export interface NewCheckpointDraft {
  title: string;
  remind_at: string;
}

export interface NewTaskDraft {
  title: string;
  description: string;
  kind: Task['kind'];
  difficulty: Task['difficulty'];
  due_at: string | null;
  reward_text: string;
  reward_xp: number;
  reward_gold: number;
  penalty_text: string;
  penalty_hp: number;
  penalty_gold: number;
  requires_approval: boolean;
  checkpoints: NewCheckpointDraft[];
  assignees: string[];
}

interface QuestState {
  tasks: Task[];
  story: StoryChapter[];
  ledger: LedgerEntry[];
  loading: boolean;
  /** Всплывающее событие для UI: левелап, награда, провал. */
  banner: { title: string; body: string; tone: 'good' | 'bad' } | null;

  loadAll: () => Promise<void>;
  subscribe: () => () => void;
  createTask: (draft: NewTaskDraft) => Promise<Task>;
  updateTask: (id: string, patch: Partial<Task>) => Promise<void>;
  deleteTask: (id: string) => Promise<void>;
  setStatus: (id: string, status: TaskStatus) => Promise<void>;
  toggleCheckpoint: (checkpoint: Checkpoint) => Promise<void>;
  submitTask: (id: string) => Promise<void>;
  approveTask: (id: string) => Promise<void>;
  failTask: (id: string) => Promise<void>;
  redeem: (entryId: string) => Promise<void>;
  generateStory: (task?: Task) => Promise<StoryChapter | null>;
  clearBanner: () => void;
}

const TASK_SELECT = '*, checkpoints(*), task_assignees(*)';

/** Приводит ответ Supabase к нашему Task (переименовывает вложенные связи). */
function normalize(row: any): Task {
  return {
    ...row,
    checkpoints: (row.checkpoints ?? []).sort(
      (a: Checkpoint, b: Checkpoint) => new Date(a.remind_at).getTime() - new Date(b.remind_at).getTime(),
    ),
    assignees: row.task_assignees ?? [],
  };
}

export const useQuests = create<QuestState>((set, get) => ({
  tasks: [],
  story: [],
  ledger: [],
  loading: false,
  banner: null,

  clearBanner: () => set({ banner: null }),

  loadAll: async () => {
    const coupleId = useSession.getState().couple?.id;
    if (!coupleId) {
      set({ tasks: [], story: [], ledger: [] });
      return;
    }
    set({ loading: true });
    try {
      const [tasksRes, storyRes, ledgerRes] = await Promise.all([
        supabase.from('tasks').select(TASK_SELECT).eq('couple_id', coupleId).order('created_at', { ascending: false }),
        supabase.from('story_chapters').select('*').eq('couple_id', coupleId).order('chapter_no', { ascending: false }),
        supabase.from('ledger').select('*').eq('couple_id', coupleId).order('created_at', { ascending: false }),
      ]);

      const tasks = (tasksRes.data ?? []).map(normalize);
      set({ tasks, story: storyRes.data ?? [], ledger: ledgerRes.data ?? [] });
      await syncAllReminders(tasks);
    } finally {
      set({ loading: false });
    }
  },

  /** Realtime: любое изменение у пары перезагружает данные. Просто и надёжно. */
  subscribe: () => {
    const coupleId = useSession.getState().couple?.id;
    if (!coupleId) return () => {};

    const channel = supabase
      .channel(`couple:${coupleId}`)
      .on('postgres_changes', { event: '*', schema: 'public', table: 'tasks', filter: `couple_id=eq.${coupleId}` }, () =>
        get().loadAll(),
      )
      .on('postgres_changes', { event: '*', schema: 'public', table: 'checkpoints' }, () => get().loadAll())
      .on(
        'postgres_changes',
        { event: '*', schema: 'public', table: 'story_chapters', filter: `couple_id=eq.${coupleId}` },
        () => get().loadAll(),
      )
      .on('postgres_changes', { event: '*', schema: 'public', table: 'ledger', filter: `couple_id=eq.${coupleId}` }, () =>
        get().loadAll(),
      )
      .subscribe();

    return () => {
      void supabase.removeChannel(channel);
    };
  },

  createTask: async (draft) => {
    const { couple, session, partner } = useSession.getState();
    if (!couple || !session) throw new Error('Сначала создайте пару');

    const { data: task, error } = await supabase
      .from('tasks')
      .insert({
        couple_id: couple.id,
        created_by: session.user.id,
        title: draft.title.trim(),
        description: draft.description.trim() || null,
        kind: draft.kind,
        difficulty: draft.difficulty,
        due_at: draft.due_at,
        reward_text: draft.reward_text.trim() || null,
        reward_xp: draft.reward_xp,
        reward_gold: draft.reward_gold,
        penalty_text: draft.penalty_text.trim() || null,
        penalty_hp: draft.penalty_hp,
        penalty_gold: draft.penalty_gold,
        requires_approval: draft.requires_approval,
      })
      .select()
      .single();
    if (error) throw error;

    // Кому квест назначен: явный список, иначе выводим из типа.
    let assignees = draft.assignees;
    if (assignees.length === 0) {
      if (draft.kind === 'personal') assignees = [session.user.id];
      else if (draft.kind === 'assigned') assignees = partner ? [partner.id] : [session.user.id];
      else assignees = partner ? [session.user.id, partner.id] : [session.user.id];
    }
    await supabase.from('task_assignees').insert(assignees.map((user_id) => ({ task_id: task.id, user_id })));

    if (draft.checkpoints.length) {
      await supabase.from('checkpoints').insert(
        draft.checkpoints.map((cp, i) => ({
          task_id: task.id,
          title: cp.title.trim() || `Точка ${i + 1}`,
          remind_at: cp.remind_at,
          position: i,
        })),
      );
    }

    // Флавор-текст в духе RPG — приятное дополнение, без него квест полноценен.
    void (async () => {
      const settings = llmSettings();
      if (!settings.apiKey) return;
      try {
        const flavor = await writeQuestFlavor(settings, {
          characters: knownCharacters(),
          task: { title: task.title, description: task.description, difficulty: task.difficulty },
        });
        await supabase
          .from('tasks')
          .update({ quest_title: flavor.quest_title, quest_intro: flavor.quest_intro })
          .eq('id', task.id);
        await get().loadAll();
      } catch {
        /* нейросеть недоступна — молчим, это не мешает пользоваться квестом */
      }
    })();

    void supabase.functions
      .invoke('notify-partner', {
        body: {
          couple_id: couple.id,
          exclude_user_id: session.user.id,
          title: '📜 Новый квест',
          body: draft.title.trim(),
          data: { taskId: task.id },
        },
      })
      .catch(() => undefined);

    await get().loadAll();
    return task;
  },

  updateTask: async (id, patch) => {
    const { error } = await supabase.from('tasks').update(patch).eq('id', id);
    if (error) throw error;
    await get().loadAll();
  },

  deleteTask: async (id) => {
    const { error } = await supabase.from('tasks').delete().eq('id', id);
    if (error) throw error;
    await get().loadAll();
  },

  setStatus: async (id, status) => {
    await get().updateTask(id, { status });
  },

  toggleCheckpoint: async (checkpoint) => {
    const done = checkpoint.done_at ? null : new Date().toISOString();
    const { error } = await supabase.from('checkpoints').update({ done_at: done }).eq('id', checkpoint.id);
    if (error) throw error;

    const task = get().tasks.find((t) => t.id === checkpoint.task_id);
    if (task) {
      const updated: Task = {
        ...task,
        checkpoints: (task.checkpoints ?? []).map((c) => (c.id === checkpoint.id ? { ...c, done_at: done } : c)),
      };
      set({ tasks: get().tasks.map((t) => (t.id === task.id ? updated : t)) });
      await syncTaskReminders(updated);
    }
  },

  /** Отмечает свою часть выполненной. Если нужна проверка — уходит в статус «на проверке». */
  submitTask: async (id) => {
    const { session, couple, profile } = useSession.getState();
    const task = get().tasks.find((t) => t.id === id);
    if (!task || !session) return;

    await supabase
      .from('task_assignees')
      .update({ done_at: new Date().toISOString() })
      .eq('task_id', id)
      .eq('user_id', session.user.id);

    // Общий квест ждёт обоих.
    const others = (task.assignees ?? []).filter((a) => a.user_id !== session.user.id);
    const everyoneDone = others.every((a) => a.done_at);

    if (!everyoneDone) {
      await get().updateTask(id, { status: 'in_progress' });
    } else if (task.requires_approval) {
      await get().updateTask(id, { status: 'submitted' });
      if (couple) {
        void supabase.functions
          .invoke('notify-partner', {
            body: {
              couple_id: couple.id,
              exclude_user_id: session.user.id,
              title: '✅ Квест ждёт проверки',
              body: `${profile?.display_name ?? 'Партнёр'} закрыл(а): ${task.title}`,
              data: { taskId: task.id },
            },
          })
          .catch(() => undefined);
      }
    } else {
      await get().approveTask(id);
    }
    await get().loadAll();
  },

  /** Подтверждение выполнения: начисляет награду, пишет долг в журнал, просит главу сюжета. */
  approveTask: async (id) => {
    const { couple, session, character, partnerCharacter, partner } = useSession.getState();
    const task = get().tasks.find((t) => t.id === id);
    if (!task || !couple || !session) return;

    const { xp, gold, bonusReason } = rewardFor(task);
    const doers = (task.assignees ?? []).map((a) => a.user_id);

    // Награждаем каждого исполнителя, чьего персонажа мы видим.
    for (const userId of doers) {
      const target = userId === session.user.id ? character : userId === partner?.id ? partnerCharacter : null;
      if (!target) continue;
      const next = applyReward(target, xp, gold);
      await supabase
        .from('characters')
        .update({ xp: next.xp, gold: next.gold, level: next.level, hp: next.hp, max_hp: next.max_hp })
        .eq('id', target.id);

      if (userId === session.user.id && next.leveledUp) {
        set({
          banner: {
            title: `🎉 Уровень ${next.level}!`,
            body: 'Здоровье полностью восстановлено.',
            tone: 'good',
          },
        });
      }
    }

    if (task.reward_text) {
      await supabase.from('ledger').insert(
        doers.map((user_id) => ({
          couple_id: couple.id,
          task_id: task.id,
          user_id,
          kind: 'reward' as const,
          text: task.reward_text!,
        })),
      );
    }

    await supabase
      .from('tasks')
      .update({ status: 'completed', completed_at: new Date().toISOString() })
      .eq('id', id);

    if (!get().banner) {
      set({
        banner: {
          title: `+${xp} XP, +${gold} золота`,
          body: bonusReason ?? 'Квест закрыт. Так держать!',
          tone: 'good',
        },
      });
    }

    await useSession.getState().refresh();
    // Глава пишется в фоне: закрытие квеста не должно падать из-за нейросети.
    void get().generateStory(task).catch(() => undefined);
    await get().loadAll();
  },

  /** Провал: штраф по HP и золоту, наказание уходит в журнал. */
  failTask: async (id) => {
    const { couple, session, character, partnerCharacter, partner } = useSession.getState();
    const task = get().tasks.find((t) => t.id === id);
    if (!task || !couple || !session) return;

    const doers = (task.assignees ?? []).map((a) => a.user_id);
    for (const userId of doers) {
      const target = userId === session.user.id ? character : userId === partner?.id ? partnerCharacter : null;
      if (!target) continue;
      const next = applyPenalty(target, task.penalty_hp, task.penalty_gold);
      await supabase.from('characters').update({ hp: next.hp, gold: next.gold, streak: 0 }).eq('id', target.id);
    }

    if (task.penalty_text) {
      await supabase.from('ledger').insert(
        doers.map((user_id) => ({
          couple_id: couple.id,
          task_id: task.id,
          user_id,
          kind: 'penalty' as const,
          text: task.penalty_text!,
        })),
      );
    }

    await supabase.from('tasks').update({ status: 'failed' }).eq('id', id);
    set({
      banner: {
        title: `−${task.penalty_hp} HP, −${task.penalty_gold} золота`,
        body: task.penalty_text ? `Наказание: ${task.penalty_text}` : 'Квест провален.',
        tone: 'bad',
      },
    });

    await useSession.getState().refresh();
    await get().loadAll();
  },

  redeem: async (entryId) => {
    const { error } = await supabase
      .from('ledger')
      .update({ redeemed_at: new Date().toISOString() })
      .eq('id', entryId);
    if (error) throw error;
    await get().loadAll();
  },

  /**
   * Пишет следующую главу через нейросеть и сохраняет её в базу.
   *
   * Запрос уходит прямо с устройства ключом, который введён в настройках, —
   * разворачивать edge-функцию для этого не нужно. Достаточно, чтобы глава
   * сгенерировалась на одном телефоне: в базе её увидят оба.
   */
  generateStory: async (task) => {
    const couple = useSession.getState().couple;
    if (!couple) return null;

    const settings = llmSettings();
    if (!settings.apiKey) {
      throw new LlmError('Ключ нейросети не задан. Откройте «Герой» → «Нейросеть» и вставьте его.');
    }

    const chapterNo = (get().story[0]?.chapter_no ?? 0) + 1;
    const draft = await writeChapter(settings, {
      characters: knownCharacters(),
      tasks: get().tasks.slice(0, 12),
      previous: get().story.slice(0, 3),
      chapterNo,
    });

    const { data: chapter, error } = await supabase
      .from('story_chapters')
      .insert({
        couple_id: couple.id,
        chapter_no: chapterNo,
        title: draft.title,
        body: draft.body,
        cliffhanger: draft.cliffhanger,
        triggered_by: task?.id ?? null,
      })
      .select()
      .single();

    // Два устройства могли попросить главу одновременно — номер занят, не беда.
    if (error) {
      await get().loadAll();
      if (error.code === '23505') return null;
      throw error;
    }

    await get().loadAll();
    return chapter as StoryChapter;
  },
}));
