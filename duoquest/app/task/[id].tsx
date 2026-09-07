import React, { useMemo, useState } from 'react';
import { Alert, Pressable, StyleSheet, Text, View } from 'react-native';
import { useLocalSearchParams, useRouter } from 'expo-router';
import { Ionicons } from '@expo/vector-icons';
import { colors, difficultyMeta, radius, spacing, statusMeta } from '@/theme';
import { Button, Card, Chip, Row, Screen, Subtitle, Title } from '@/components/ui';
import { formatDateTime, relativeDeadline } from '@/lib/dates';
import { useQuests } from '@/store/useQuests';
import { useSession } from '@/store/useSession';

export default function TaskDetailScreen() {
  const { id } = useLocalSearchParams<{ id: string }>();
  const router = useRouter();
  const { tasks, toggleCheckpoint, submitTask, approveTask, failTask, deleteTask, setStatus } = useQuests();
  const { session, partner } = useSession();
  const [busy, setBusy] = useState(false);

  const task = useMemo(() => tasks.find((t) => t.id === id), [tasks, id]);
  const meId = session?.user.id ?? null;

  if (!task) {
    return (
      <Screen>
        <Subtitle>Квест не найден — возможно, он был удалён.</Subtitle>
        <Button title="Назад" variant="ghost" onPress={() => router.back()} />
      </Screen>
    );
  }

  const diff = difficultyMeta[task.difficulty];
  const status = statusMeta[task.status];
  const deadline = relativeDeadline(task.due_at);
  const checkpoints = task.checkpoints ?? [];
  const assignees = task.assignees ?? [];

  const isMine = assignees.some((a) => a.user_id === meId);
  const myEntry = assignees.find((a) => a.user_id === meId);
  const iAmCreator = task.created_by === meId;
  const closed = task.status === 'completed' || task.status === 'failed' || task.status === 'cancelled';

  // Проверять чужую работу может тот, кто выдал квест и сам его не выполняет.
  const canApprove = task.status === 'submitted' && (iAmCreator || !isMine);

  const run = async (fn: () => Promise<void>) => {
    setBusy(true);
    try {
      await fn();
    } catch (e: any) {
      Alert.alert('Ошибка', e?.message ?? 'Попробуйте ещё раз.');
    } finally {
      setBusy(false);
    }
  };

  const confirmDelete = () =>
    Alert.alert('Удалить квест?', 'Действие необратимо.', [
      { text: 'Отмена', style: 'cancel' },
      {
        text: 'Удалить',
        style: 'destructive',
        onPress: () => run(async () => {
          await deleteTask(task.id);
          router.back();
        }),
      },
    ]);

  return (
    <Screen>
      <View style={{ gap: spacing.sm }}>
        <Row gap={6} style={{ flexWrap: 'wrap' }}>
          <Chip label={status.label} color={status.color} filled />
          <Chip label={diff.label} color={diff.color} />
          <Chip
            label={assignees.length > 1 ? 'Общий' : isMine ? 'Мой' : partner?.display_name ?? 'Партнёру'}
            color={assignees.length > 1 ? colors.primarySoft : isMine ? colors.blue : colors.pink}
            filled
          />
        </Row>
        <Title>{task.title}</Title>
        {task.description ? <Subtitle>{task.description}</Subtitle> : null}
      </View>

      {task.quest_title ? (
        <Card style={{ borderColor: colors.primary + '66' }}>
          <Text style={styles.questLabel}>📜 Легенда квеста</Text>
          <Text style={styles.questTitle}>{task.quest_title}</Text>
          {task.quest_intro ? <Text style={styles.questIntro}>{task.quest_intro}</Text> : null}
        </Card>
      ) : null}

      <Card>
        <Row style={{ justifyContent: 'space-between' }}>
          <View style={{ gap: 2 }}>
            <Text style={styles.metaLabel}>Срок</Text>
            <Text style={styles.metaValue}>{formatDateTime(task.due_at)}</Text>
          </View>
          <Chip
            label={deadline.text}
            color={closed ? colors.textFaint : deadline.overdue ? colors.red : deadline.urgent ? colors.gold : colors.green}
            filled
          />
        </Row>

        <View style={styles.divider} />

        <Row style={{ justifyContent: 'space-between' }}>
          <Row gap={6}>
            <Ionicons name="sparkles-outline" size={16} color={colors.primarySoft} />
            <Text style={[styles.metaValue, { color: colors.primarySoft }]}>+{task.reward_xp} XP</Text>
          </Row>
          <Row gap={6}>
            <Ionicons name="diamond-outline" size={16} color={colors.gold} />
            <Text style={[styles.metaValue, { color: colors.gold }]}>+{task.reward_gold}</Text>
          </Row>
          <Row gap={6}>
            <Ionicons name="heart-dislike-outline" size={16} color={colors.red} />
            <Text style={[styles.metaValue, { color: colors.red }]}>−{task.penalty_hp} HP</Text>
          </Row>
        </Row>
      </Card>

      {checkpoints.length ? (
        <Card>
          <Text style={styles.sectionTitle}>Реперные точки</Text>
          {checkpoints.map((cp) => (
            <Pressable
              key={cp.id}
              onPress={() => !closed && run(() => toggleCheckpoint(cp))}
              style={styles.checkpointRow}
            >
              <Ionicons
                name={cp.done_at ? 'checkmark-circle' : 'ellipse-outline'}
                size={22}
                color={cp.done_at ? colors.green : colors.textFaint}
              />
              <View style={{ flex: 1 }}>
                <Text style={[styles.cpTitle, cp.done_at && styles.cpTitleDone]}>{cp.title}</Text>
                <Text style={styles.cpTime}>{formatDateTime(cp.remind_at)}</Text>
              </View>
            </Pressable>
          ))}
        </Card>
      ) : null}

      {task.reward_text || task.penalty_text ? (
        <Card>
          {task.reward_text ? (
            <View style={{ gap: 4 }}>
              <Text style={[styles.metaLabel, { color: colors.green }]}>🎁 Награда</Text>
              <Text style={styles.dealText}>{task.reward_text}</Text>
            </View>
          ) : null}
          {task.penalty_text ? (
            <View style={{ gap: 4 }}>
              <Text style={[styles.metaLabel, { color: colors.red }]}>⚡ Наказание</Text>
              <Text style={styles.dealText}>{task.penalty_text}</Text>
            </View>
          ) : null}
        </Card>
      ) : null}

      {!closed ? (
        <View style={{ gap: spacing.sm }}>
          {isMine && !myEntry?.done_at ? (
            <Button
              title={task.requires_approval ? 'Готово, отправить на проверку' : 'Готово'}
              icon="checkmark-done-outline"
              loading={busy}
              onPress={() => run(() => submitTask(task.id))}
            />
          ) : null}

          {isMine && myEntry?.done_at && task.status !== 'submitted' ? (
            <Subtitle style={{ textAlign: 'center' }}>Ваша часть выполнена — ждём партнёра.</Subtitle>
          ) : null}

          {canApprove ? (
            <>
              <Button
                title="Принять и наградить"
                variant="success"
                icon="trophy-outline"
                loading={busy}
                onPress={() => run(() => approveTask(task.id))}
              />
              <Button
                title="Не принять"
                variant="ghost"
                icon="close-outline"
                loading={busy}
                onPress={() => run(() => setStatus(task.id, 'in_progress'))}
              />
            </>
          ) : null}

          {iAmCreator ? (
            <Button
              title="Отметить как провал"
              variant="danger"
              icon="skull-outline"
              loading={busy}
              onPress={() =>
                Alert.alert('Провалить квест?', 'Будет применён штраф и записано наказание.', [
                  { text: 'Отмена', style: 'cancel' },
                  { text: 'Провалить', style: 'destructive', onPress: () => run(() => failTask(task.id)) },
                ])
              }
            />
          ) : null}
        </View>
      ) : (
        <Subtitle style={{ textAlign: 'center' }}>
          Квест закрыт {task.completed_at ? formatDateTime(task.completed_at) : ''}
        </Subtitle>
      )}

      {iAmCreator ? (
        <Button title="Удалить квест" variant="ghost" icon="trash-outline" onPress={confirmDelete} />
      ) : null}
    </Screen>
  );
}

const styles = StyleSheet.create({
  questLabel: { color: colors.textFaint, fontSize: 11, textTransform: 'uppercase', letterSpacing: 1 },
  questTitle: { color: colors.gold, fontSize: 17, fontWeight: '800' },
  questIntro: { color: colors.textDim, fontSize: 14, lineHeight: 21, fontStyle: 'italic' },
  metaLabel: { color: colors.textFaint, fontSize: 11, textTransform: 'uppercase', letterSpacing: 0.8, fontWeight: '700' },
  metaValue: { color: colors.text, fontSize: 15, fontWeight: '700' },
  divider: { height: 1, backgroundColor: colors.border },
  sectionTitle: { color: colors.text, fontSize: 16, fontWeight: '800' },
  checkpointRow: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: spacing.md,
    paddingVertical: spacing.sm,
    borderRadius: radius.sm,
  },
  cpTitle: { color: colors.text, fontSize: 15, fontWeight: '600' },
  cpTitleDone: { color: colors.textDim, textDecorationLine: 'line-through' },
  cpTime: { color: colors.textFaint, fontSize: 12 },
  dealText: { color: colors.text, fontSize: 15, lineHeight: 21 },
});
