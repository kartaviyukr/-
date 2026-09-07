import React from 'react';
import { Pressable, StyleSheet, Text, View } from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import { colors, difficultyMeta, radius, spacing, statusMeta } from '@/theme';
import { relativeDeadline } from '@/lib/dates';
import { Chip, Row } from '@/components/ui';
import type { Task } from '@/types';

interface Props {
  task: Task;
  onPress: () => void;
  /** id текущего пользователя — чтобы показать, чей это квест. */
  meId: string | null;
  partnerName?: string;
}

export function TaskCard({ task, onPress, meId, partnerName }: Props) {
  const diff = difficultyMeta[task.difficulty];
  const status = statusMeta[task.status];
  const deadline = relativeDeadline(task.due_at);
  const done = task.status === 'completed';

  const checkpoints = task.checkpoints ?? [];
  const doneCheckpoints = checkpoints.filter((c) => c.done_at).length;

  const mine = (task.assignees ?? []).some((a) => a.user_id === meId);
  const shared = (task.assignees ?? []).length > 1;
  const owner = shared ? 'Общий' : mine ? 'Мой' : partnerName ?? 'Партнёру';

  const deadlineColor = done
    ? colors.textFaint
    : deadline.overdue
      ? colors.red
      : deadline.urgent
        ? colors.gold
        : colors.textDim;

  return (
    <Pressable onPress={onPress} style={({ pressed }) => [styles.card, { opacity: pressed ? 0.75 : 1 }]}>
      <View style={[styles.stripe, { backgroundColor: diff.color }]} />

      <View style={styles.body}>
        <Row style={{ justifyContent: 'space-between' }}>
          <Row gap={6}>
            <Chip label={owner} color={shared ? colors.primarySoft : mine ? colors.blue : colors.pink} filled />
            <Chip label={diff.label} color={diff.color} />
          </Row>
          <Chip label={status.label} color={status.color} filled />
        </Row>

        <Text style={[styles.title, done && styles.titleDone]} numberOfLines={2}>
          {task.title}
        </Text>

        {task.quest_title ? (
          <Text style={styles.flavor} numberOfLines={1}>
            📜 {task.quest_title}
          </Text>
        ) : null}

        <Row style={{ justifyContent: 'space-between', flexWrap: 'wrap' }} gap={spacing.md}>
          <Row gap={4}>
            <Ionicons name="time-outline" size={14} color={deadlineColor} />
            <Text style={[styles.meta, { color: deadlineColor }]}>{deadline.text}</Text>
          </Row>

          {checkpoints.length ? (
            <Row gap={4}>
              <Ionicons name="flag-outline" size={14} color={colors.textDim} />
              <Text style={styles.meta}>
                {doneCheckpoints}/{checkpoints.length}
              </Text>
            </Row>
          ) : null}

          <Row gap={4}>
            <Ionicons name="sparkles-outline" size={14} color={colors.primarySoft} />
            <Text style={[styles.meta, { color: colors.primarySoft }]}>{task.reward_xp} XP</Text>
          </Row>

          <Row gap={4}>
            <Ionicons name="diamond-outline" size={14} color={colors.gold} />
            <Text style={[styles.meta, { color: colors.gold }]}>{task.reward_gold}</Text>
          </Row>
        </Row>
      </View>
    </Pressable>
  );
}

const styles = StyleSheet.create({
  card: {
    flexDirection: 'row',
    backgroundColor: colors.card,
    borderRadius: radius.lg,
    borderWidth: 1,
    borderColor: colors.border,
    overflow: 'hidden',
  },
  stripe: { width: 4 },
  body: { flex: 1, padding: spacing.md, gap: spacing.sm },
  title: { color: colors.text, fontSize: 16, fontWeight: '700', lineHeight: 21 },
  titleDone: { color: colors.textDim, textDecorationLine: 'line-through' },
  flavor: { color: colors.primarySoft, fontSize: 12, fontStyle: 'italic' },
  meta: { color: colors.textDim, fontSize: 12, fontWeight: '600' },
});
