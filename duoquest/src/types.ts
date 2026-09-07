export type TaskKind = 'personal' | 'assigned' | 'shared';
export type TaskStatus = 'open' | 'in_progress' | 'submitted' | 'completed' | 'failed' | 'cancelled';
export type Difficulty = 'trivial' | 'easy' | 'normal' | 'hard' | 'epic';
export type HeroClass = 'warrior' | 'mage' | 'rogue' | 'bard' | 'healer';

export interface Profile {
  id: string;
  display_name: string;
  avatar_emoji: string;
  push_token: string | null;
  created_at: string;
}

export interface Couple {
  id: string;
  name: string;
  invite_code: string;
  created_by: string | null;
  created_at: string;
}

export interface Character {
  id: string;
  user_id: string;
  couple_id: string | null;
  name: string;
  hero_class: HeroClass;
  level: number;
  xp: number;
  hp: number;
  max_hp: number;
  gold: number;
  strength: number;
  agility: number;
  wisdom: number;
  charisma: number;
  streak: number;
  avatar_emoji: string;
  bio: string | null;
  updated_at: string;
}

export interface Checkpoint {
  id: string;
  task_id: string;
  title: string;
  remind_at: string;
  done_at: string | null;
  position: number;
}

export interface TaskAssignee {
  task_id: string;
  user_id: string;
  done_at: string | null;
}

export interface Task {
  id: string;
  couple_id: string;
  created_by: string;
  title: string;
  description: string | null;
  kind: TaskKind;
  difficulty: Difficulty;
  status: TaskStatus;
  due_at: string | null;
  reward_text: string | null;
  reward_gold: number;
  reward_xp: number;
  penalty_text: string | null;
  penalty_hp: number;
  penalty_gold: number;
  requires_approval: boolean;
  completed_at: string | null;
  quest_title: string | null;
  quest_intro: string | null;
  created_at: string;
  updated_at: string;
  /** Подгружается джойном. */
  checkpoints?: Checkpoint[];
  assignees?: TaskAssignee[];
}

export interface StoryChapter {
  id: string;
  couple_id: string;
  chapter_no: number;
  title: string;
  body: string;
  cliffhanger: string | null;
  triggered_by: string | null;
  created_at: string;
}

export interface LedgerEntry {
  id: string;
  couple_id: string;
  task_id: string | null;
  user_id: string;
  kind: 'reward' | 'penalty';
  text: string;
  redeemed_at: string | null;
  created_at: string;
}
