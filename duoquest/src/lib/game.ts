import { difficultyMeta } from '@/theme';
import type { Character, Difficulty, HeroClass, Task } from '@/types';

/** Сколько всего XP нужно, чтобы достичь уровня `level`. Растёт квадратично. */
export function xpForLevel(level: number): number {
  if (level <= 1) return 0;
  return Math.round(100 * Math.pow(level - 1, 1.6));
}

/** Уровень, соответствующий накопленному XP. */
export function levelFromXp(xp: number): number {
  let level = 1;
  while (xpForLevel(level + 1) <= xp && level < 200) level += 1;
  return level;
}

/** Прогресс внутри текущего уровня: 0..1, плюс абсолютные значения для подписи. */
export function levelProgress(xp: number) {
  const level = levelFromXp(xp);
  const cur = xpForLevel(level);
  const next = xpForLevel(level + 1);
  const span = Math.max(1, next - cur);
  return {
    level,
    current: xp - cur,
    needed: span,
    ratio: Math.max(0, Math.min(1, (xp - cur) / span)),
    nextAt: next,
  };
}

export function maxHpForLevel(level: number): number {
  return 100 + (level - 1) * 15;
}

export const heroClasses: Record<HeroClass, { label: string; emoji: string; blurb: string; bonus: string }> = {
  warrior: { label: 'Воин', emoji: '🛡️', blurb: 'Держит удар и не отступает от дедлайнов.', bonus: '+2 Сила' },
  mage: { label: 'Маг', emoji: '🔮', blurb: 'Продумывает всё наперёд и знает, где срезать.', bonus: '+2 Мудрость' },
  rogue: { label: 'Плут', emoji: '🗡️', blurb: 'Делает быстро, тихо и в последний момент.', bonus: '+2 Ловкость' },
  bard: { label: 'Бард', emoji: '🎻', blurb: 'Уговорит кого угодно на что угодно.', bonus: '+2 Харизма' },
  healer: { label: 'Целитель', emoji: '🌿', blurb: 'Восстанавливает силы — свои и партнёра.', bonus: '+15 Макс. HP' },
};

/** Стартовые характеристики с классовым бонусом. */
export function baseStats(hero: HeroClass) {
  const stats = { strength: 5, agility: 5, wisdom: 5, charisma: 5, max_hp: 100 };
  if (hero === 'warrior') stats.strength += 2;
  if (hero === 'rogue') stats.agility += 2;
  if (hero === 'mage') stats.wisdom += 2;
  if (hero === 'bard') stats.charisma += 2;
  if (hero === 'healer') stats.max_hp += 15;
  return stats;
}

/** Награда за квест: базовая по сложности, с бонусом за досрочное выполнение. */
export function rewardFor(task: Pick<Task, 'difficulty' | 'reward_xp' | 'reward_gold' | 'due_at'>, at = new Date()) {
  const base = difficultyMeta[task.difficulty];
  let xp = task.reward_xp || base.xp;
  let gold = task.reward_gold || base.gold;
  let bonusReason: string | null = null;

  if (task.due_at) {
    const due = new Date(task.due_at).getTime();
    const hoursEarly = (due - at.getTime()) / 3_600_000;
    if (hoursEarly >= 24) {
      xp = Math.round(xp * 1.25);
      gold = Math.round(gold * 1.25);
      bonusReason = 'Досрочно, больше чем за сутки: +25%';
    } else if (hoursEarly < 0) {
      xp = Math.round(xp * 0.5);
      gold = Math.round(gold * 0.5);
      bonusReason = 'Просрочено: награда вдвое меньше';
    }
  }
  return { xp, gold, bonusReason };
}

/** Применяет награду к персонажу и сообщает, случился ли левелап. */
export function applyReward(character: Character, xp: number, gold: number) {
  const newXp = character.xp + xp;
  const newLevel = levelFromXp(newXp);
  const leveledUp = newLevel > character.level;
  const maxHp = maxHpForLevel(newLevel);
  return {
    xp: newXp,
    gold: character.gold + gold,
    level: newLevel,
    max_hp: maxHp,
    // На новом уровне полностью восстанавливаем здоровье.
    hp: leveledUp ? maxHp : Math.min(character.hp, maxHp),
    leveledUp,
  };
}

/** Применяет штраф. HP не опускается ниже 1 — «смерти» в приложении для пары не нужно. */
export function applyPenalty(character: Character, hp: number, gold: number) {
  return {
    hp: Math.max(1, character.hp - hp),
    gold: Math.max(0, character.gold - gold),
  };
}

export function difficultyDefaults(d: Difficulty) {
  const m = difficultyMeta[d];
  return { reward_xp: m.xp, reward_gold: m.gold, penalty_hp: Math.round(m.xp / 10), penalty_gold: Math.round(m.gold / 2) };
}

/** Код-приглашение в пару: 6 символов без похожих друг на друга букв и цифр. */
export function generateInviteCode(): string {
  const alphabet = 'ABCDEFGHJKLMNPQRSTUVWXYZ23456789';
  let out = '';
  for (let i = 0; i < 6; i++) out += alphabet[Math.floor(Math.random() * alphabet.length)];
  return out;
}
