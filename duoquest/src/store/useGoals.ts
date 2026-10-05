import { create } from 'zustand';
import { supabase } from '@/lib/supabase';
import { useSession } from '@/store/useSession';
import type { Goal } from '@/types';

export interface NewGoalDraft {
  title: string;
  note: string;
  goal_date: string;
  /** Чья цель: id партнёра, свой id или null для общей. */
  user_id: string | null;
}

interface GoalsState {
  goals: Goal[];
  loading: boolean;

  loadGoals: () => Promise<void>;
  subscribeGoals: () => () => void;
  createGoal: (draft: NewGoalDraft) => Promise<void>;
  updateGoal: (id: string, patch: Partial<Goal>) => Promise<void>;
  toggleGoal: (goal: Goal) => Promise<void>;
  deleteGoal: (id: string) => Promise<void>;
}

export const useGoals = create<GoalsState>((set, get) => ({
  goals: [],
  loading: false,

  loadGoals: async () => {
    const coupleId = useSession.getState().couple?.id;
    if (!coupleId) {
      set({ goals: [] });
      return;
    }
    set({ loading: true });
    try {
      const { data } = await supabase
        .from('goals')
        .select('*')
        .eq('couple_id', coupleId)
        .order('goal_date', { ascending: true });
      set({ goals: data ?? [] });
    } finally {
      set({ loading: false });
    }
  },

  subscribeGoals: () => {
    const coupleId = useSession.getState().couple?.id;
    if (!coupleId) return () => {};

    const channel = supabase
      .channel(`goals:${coupleId}`)
      .on('postgres_changes', { event: '*', schema: 'public', table: 'goals', filter: `couple_id=eq.${coupleId}` }, () =>
        get().loadGoals(),
      )
      .subscribe();

    return () => {
      void supabase.removeChannel(channel);
    };
  },

  createGoal: async (draft) => {
    const { couple, session, profile } = useSession.getState();
    if (!couple || !session) throw new Error('Сначала создайте пару');

    const { error } = await supabase.from('goals').insert({
      couple_id: couple.id,
      created_by: session.user.id,
      user_id: draft.user_id,
      title: draft.title.trim(),
      note: draft.note.trim() || null,
      goal_date: draft.goal_date,
    });
    if (error) throw error;

    // Партнёру сообщаем, только если цель поставили ему или она общая.
    if (draft.user_id !== session.user.id) {
      void supabase.functions
        .invoke('notify-partner', {
          body: {
            couple_id: couple.id,
            exclude_user_id: session.user.id,
            title: draft.user_id === null ? '🎯 Общая цель' : '🎯 Новая цель',
            body: `${profile?.display_name ?? 'Партнёр'}: ${draft.title.trim()}`,
            data: {},
          },
        })
        .catch(() => undefined);
    }

    await get().loadGoals();
  },

  updateGoal: async (id, patch) => {
    const { error } = await supabase.from('goals').update(patch).eq('id', id);
    if (error) throw error;
    await get().loadGoals();
  },

  toggleGoal: async (goal) => {
    const done_at = goal.done_at ? null : new Date().toISOString();
    // Отмечаем сразу, не дожидаясь сети: галочка должна ставиться мгновенно.
    set({ goals: get().goals.map((g) => (g.id === goal.id ? { ...g, done_at } : g)) });

    const { error } = await supabase.from('goals').update({ done_at }).eq('id', goal.id);
    if (error) {
      await get().loadGoals();
      throw error;
    }
  },

  deleteGoal: async (id) => {
    const { error } = await supabase.from('goals').delete().eq('id', id);
    if (error) throw error;
    set({ goals: get().goals.filter((g) => g.id !== id) });
  },
}));
