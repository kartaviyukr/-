import { create } from 'zustand';
import type { Session } from '@supabase/supabase-js';
import { supabase } from '@/lib/supabase';
import { registerForPushNotifications } from '@/lib/notifications';
import { baseStats, generateInviteCode } from '@/lib/game';
import type { Character, Couple, HeroClass, Profile } from '@/types';

interface SessionState {
  session: Session | null;
  profile: Profile | null;
  couple: Couple | null;
  partner: Profile | null;
  character: Character | null;
  partnerCharacter: Character | null;
  loading: boolean;
  hydrated: boolean;

  bootstrap: () => Promise<void>;
  refresh: () => Promise<void>;
  signIn: (email: string, password: string) => Promise<void>;
  signUp: (email: string, password: string, name: string) => Promise<void>;
  signOut: () => Promise<void>;
  createCouple: (name: string) => Promise<Couple>;
  joinCouple: (code: string) => Promise<Couple>;
  leaveCouple: () => Promise<void>;
  createCharacter: (name: string, heroClass: HeroClass, emoji: string) => Promise<void>;
  updateProfile: (patch: Partial<Pick<Profile, 'display_name' | 'avatar_emoji'>>) => Promise<void>;
  patchCharacter: (patch: Partial<Character>) => Promise<void>;
}

export const useSession = create<SessionState>((set, get) => ({
  session: null,
  profile: null,
  couple: null,
  partner: null,
  character: null,
  partnerCharacter: null,
  loading: false,
  hydrated: false,

  bootstrap: async () => {
    const { data } = await supabase.auth.getSession();
    set({ session: data.session });
    if (data.session) await get().refresh();
    set({ hydrated: true });

    supabase.auth.onAuthStateChange((_event, session) => {
      const had = get().session?.user.id;
      set({ session });
      if (!session) {
        set({ profile: null, couple: null, partner: null, character: null, partnerCharacter: null });
      } else if (session.user.id !== had) {
        void get().refresh();
      }
    });
  },

  refresh: async () => {
    const userId = get().session?.user.id ?? (await supabase.auth.getUser()).data.user?.id;
    if (!userId) return;

    const { data: profile } = await supabase.from('profiles').select('*').eq('id', userId).maybeSingle();

    // Пара пользователя (в приложении она ровно одна).
    const { data: membership } = await supabase
      .from('couple_members')
      .select('couple_id, couples(*)')
      .eq('user_id', userId)
      .maybeSingle();

    const couple = (membership?.couples as Couple | undefined) ?? null;

    let partner: Profile | null = null;
    let partnerCharacter: Character | null = null;
    if (couple) {
      const { data: others } = await supabase
        .from('couple_members')
        .select('user_id, profiles(*)')
        .eq('couple_id', couple.id)
        .neq('user_id', userId);
      partner = ((others?.[0]?.profiles as Profile | undefined) ?? null) || null;

      if (partner) {
        const { data: pc } = await supabase
          .from('characters')
          .select('*')
          .eq('user_id', partner.id)
          .maybeSingle();
        partnerCharacter = pc ?? null;
      }
    }

    const { data: character } = await supabase.from('characters').select('*').eq('user_id', userId).maybeSingle();

    set({ profile: profile ?? null, couple, partner, character: character ?? null, partnerCharacter });

    // Токен пуша обновляем тихо и только если он изменился.
    const token = await registerForPushNotifications();
    if (token && profile && profile.push_token !== token) {
      await supabase.from('profiles').update({ push_token: token }).eq('id', userId);
    }
  },

  signIn: async (email, password) => {
    set({ loading: true });
    try {
      const { error } = await supabase.auth.signInWithPassword({ email: email.trim(), password });
      if (error) throw error;
      await get().refresh();
    } finally {
      set({ loading: false });
    }
  },

  signUp: async (email, password, name) => {
    set({ loading: true });
    try {
      const { data, error } = await supabase.auth.signUp({
        email: email.trim(),
        password,
        options: { data: { display_name: name.trim() } },
      });
      if (error) throw error;
      // Если в проекте включено подтверждение почты, сессии ещё нет — это не ошибка.
      if (data.session) await get().refresh();
    } finally {
      set({ loading: false });
    }
  },

  signOut: async () => {
    await supabase.auth.signOut();
    set({ session: null, profile: null, couple: null, partner: null, character: null, partnerCharacter: null });
  },

  createCouple: async (name) => {
    const userId = get().session?.user.id;
    if (!userId) throw new Error('Нужно войти в аккаунт');

    // Пользователь может состоять только в одной паре — освобождаем место.
    await supabase.from('couple_members').delete().eq('user_id', userId);

    const { data: couple, error } = await supabase
      .from('couples')
      .insert({ name: name.trim() || 'Наша пара', invite_code: generateInviteCode(), created_by: userId })
      .select()
      .single();
    if (error) throw error;

    const { error: memberError } = await supabase
      .from('couple_members')
      .insert({ couple_id: couple.id, user_id: userId });
    if (memberError) throw memberError;

    await get().refresh();
    return couple;
  },

  joinCouple: async (code) => {
    const userId = get().session?.user.id;
    if (!userId) throw new Error('Нужно войти в аккаунт');

    const normalized = code.trim().toUpperCase();
    const { data: couple, error } = await supabase.rpc('join_couple', { p_invite_code: normalized });
    if (error) throw error;
    if (!couple) throw new Error('Пара с таким кодом не найдена');

    await get().refresh();
    return couple as Couple;
  },

  leaveCouple: async () => {
    const userId = get().session?.user.id;
    const coupleId = get().couple?.id;
    if (!userId || !coupleId) return;
    await supabase.from('couple_members').delete().eq('couple_id', coupleId).eq('user_id', userId);
    await supabase.from('characters').update({ couple_id: null }).eq('user_id', userId);
    await get().refresh();
  },

  createCharacter: async (name, heroClass, emoji) => {
    const userId = get().session?.user.id;
    if (!userId) throw new Error('Нужно войти в аккаунт');
    const stats = baseStats(heroClass);

    const { error } = await supabase.from('characters').upsert(
      {
        user_id: userId,
        couple_id: get().couple?.id ?? null,
        name: name.trim() || 'Безымянный герой',
        hero_class: heroClass,
        avatar_emoji: emoji,
        level: 1,
        xp: 0,
        hp: stats.max_hp,
        max_hp: stats.max_hp,
        gold: 100,
        strength: stats.strength,
        agility: stats.agility,
        wisdom: stats.wisdom,
        charisma: stats.charisma,
      },
      { onConflict: 'user_id' },
    );
    if (error) throw error;
    await get().refresh();
  },

  updateProfile: async (patch) => {
    const userId = get().session?.user.id;
    if (!userId) return;
    const { error } = await supabase.from('profiles').update(patch).eq('id', userId);
    if (error) throw error;
    set({ profile: get().profile ? { ...get().profile!, ...patch } : null });
  },

  patchCharacter: async (patch) => {
    const character = get().character;
    if (!character) return;
    const { error } = await supabase.from('characters').update(patch).eq('id', character.id);
    if (error) throw error;
    set({ character: { ...character, ...patch } });
  },
}));
