import React from 'react';
import { StyleSheet, Text, View } from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import { colors, radius, spacing } from '@/theme';
import { heroClasses, levelProgress } from '@/lib/game';
import { ProgressBar, Row } from '@/components/ui';
import type { Character } from '@/types';

export function HeroCard({ character, compact }: { character: Character; compact?: boolean }) {
  const progress = levelProgress(character.xp);
  const hpRatio = character.max_hp ? character.hp / character.max_hp : 0;
  const klass = heroClasses[character.hero_class] ?? heroClasses.warrior;

  return (
    <View style={styles.card}>
      <Row gap={spacing.md}>
        <View style={styles.avatar}>
          <Text style={{ fontSize: compact ? 26 : 34 }}>{character.avatar_emoji}</Text>
        </View>

        <View style={{ flex: 1, gap: 4 }}>
          <Text style={styles.name} numberOfLines={1}>
            {character.name}
          </Text>
          <Text style={styles.klass}>
            {klass.emoji} {klass.label} · уровень {character.level}
          </Text>
        </View>

        <View style={styles.goldBadge}>
          <Ionicons name="diamond" size={13} color={colors.gold} />
          <Text style={styles.goldText}>{character.gold}</Text>
        </View>
      </Row>

      <View style={{ gap: 6 }}>
        <Row style={{ justifyContent: 'space-between' }}>
          <Text style={styles.barLabel}>Опыт</Text>
          <Text style={styles.barValue}>
            {progress.current} / {progress.needed}
          </Text>
        </Row>
        <ProgressBar ratio={progress.ratio} color={colors.primary} />
      </View>

      <View style={{ gap: 6 }}>
        <Row style={{ justifyContent: 'space-between' }}>
          <Text style={styles.barLabel}>Здоровье</Text>
          <Text style={styles.barValue}>
            {character.hp} / {character.max_hp}
          </Text>
        </Row>
        <ProgressBar ratio={hpRatio} color={hpRatio > 0.4 ? colors.green : colors.red} />
      </View>

      {compact ? null : (
        <Row style={{ justifyContent: 'space-between' }}>
          <Stat icon="barbell-outline" label="Сила" value={character.strength} />
          <Stat icon="flash-outline" label="Ловкость" value={character.agility} />
          <Stat icon="book-outline" label="Мудрость" value={character.wisdom} />
          <Stat icon="heart-outline" label="Харизма" value={character.charisma} />
        </Row>
      )}
    </View>
  );
}

function Stat({ icon, label, value }: { icon: any; label: string; value: number }) {
  return (
    <View style={styles.stat}>
      <Ionicons name={icon} size={16} color={colors.primarySoft} />
      <Text style={styles.statValue}>{value}</Text>
      <Text style={styles.statLabel}>{label}</Text>
    </View>
  );
}

const styles = StyleSheet.create({
  card: {
    backgroundColor: colors.card,
    borderRadius: radius.lg,
    borderWidth: 1,
    borderColor: colors.border,
    padding: spacing.lg,
    gap: spacing.md,
  },
  avatar: {
    width: 56,
    height: 56,
    borderRadius: 28,
    backgroundColor: colors.bgElevated,
    borderWidth: 1,
    borderColor: colors.primary,
    alignItems: 'center',
    justifyContent: 'center',
  },
  name: { color: colors.text, fontSize: 18, fontWeight: '800' },
  klass: { color: colors.textDim, fontSize: 13 },
  goldBadge: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 4,
    backgroundColor: colors.gold + '1A',
    borderColor: colors.gold + '55',
    borderWidth: 1,
    paddingHorizontal: 10,
    paddingVertical: 5,
    borderRadius: 999,
  },
  goldText: { color: colors.gold, fontWeight: '800', fontSize: 13 },
  barLabel: { color: colors.textFaint, fontSize: 11, fontWeight: '700', textTransform: 'uppercase' },
  barValue: { color: colors.textDim, fontSize: 12, fontWeight: '600' },
  stat: { alignItems: 'center', gap: 2, flex: 1 },
  statValue: { color: colors.text, fontWeight: '800', fontSize: 15 },
  statLabel: { color: colors.textFaint, fontSize: 10 },
});
