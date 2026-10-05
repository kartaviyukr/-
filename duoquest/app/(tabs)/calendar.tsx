import React, { useMemo, useState } from 'react';
import { Alert, Modal, Pressable, RefreshControl, ScrollView, StyleSheet, Text, View } from 'react-native';
import { useRouter } from 'expo-router';
import { Ionicons } from '@expo/vector-icons';
import { colors, difficultyMeta, radius, spacing } from '@/theme';
import { Button, Card, Chip, Field, Label, Row, Segmented, Subtitle, Title } from '@/components/ui';
import { MonthCalendar, type DayMarks } from '@/components/MonthCalendar';
import { dayKey, dayTitle } from '@/lib/calendar';
import { formatTime } from '@/lib/dates';
import { useGoals } from '@/store/useGoals';
import { useQuests } from '@/store/useQuests';
import { useSession } from '@/store/useSession';
import type { Goal } from '@/types';

type Owner = 'me' | 'partner' | 'both';

export default function CalendarScreen() {
  const router = useRouter();
  const [month, setMonth] = useState(() => new Date());
  const [selected, setSelected] = useState(() => dayKey(new Date()));
  const [composing, setComposing] = useState(false);

  const { goals, loading, loadGoals, toggleGoal, deleteGoal, createGoal } = useGoals();
  const { tasks } = useQuests();
  const { session, partner } = useSession();
  const meId = session?.user.id ?? null;

  // Что показывать точками под числами месяца.
  const marksByDay = useMemo(() => {
    const marks: Record<string, DayMarks> = {};
    const ensure = (key: string) =>
      (marks[key] ??= { goals: 0, goalsDone: 0, quests: 0, overdue: false });

    for (const goal of goals) {
      const entry = ensure(goal.goal_date);
      entry.goals += 1;
      if (goal.done_at) entry.goalsDone += 1;
    }

    const now = Date.now();
    for (const task of tasks) {
      if (!task.due_at) continue;
      if (task.status === 'completed' || task.status === 'cancelled') continue;
      const entry = ensure(dayKey(new Date(task.due_at)));
      entry.quests += 1;
      if (new Date(task.due_at).getTime() < now) entry.overdue = true;
    }
    return marks;
  }, [goals, tasks]);

  const dayGoals = useMemo(
    () => goals.filter((g) => g.goal_date === selected),
    [goals, selected],
  );

  const dayQuests = useMemo(
    () =>
      tasks.filter(
        (t) =>
          t.due_at &&
          dayKey(new Date(t.due_at)) === selected &&
          t.status !== 'cancelled',
      ),
    [tasks, selected],
  );

  const ownerLabel = (goal: Goal) => {
    if (goal.user_id === null) return { label: 'Общая', color: colors.primarySoft };
    if (goal.user_id === meId) return { label: 'Моя', color: colors.blue };
    return { label: partner?.display_name ?? 'Партнёру', color: colors.pink };
  };

  return (
    <>
      <ScrollView
        style={styles.container}
        contentContainerStyle={{ padding: spacing.lg, gap: spacing.lg, paddingBottom: 120 }}
        refreshControl={<RefreshControl refreshing={loading} onRefresh={loadGoals} tintColor={colors.primary} />}
      >
        <MonthCalendar
          month={month}
          selected={selected}
          marksByDay={marksByDay}
          onSelect={setSelected}
          onChangeMonth={(next) => {
            setMonth(next);
            // При переходе на текущий месяц удобно сразу встать на сегодня.
            const today = new Date();
            if (next.getMonth() === today.getMonth() && next.getFullYear() === today.getFullYear()) {
              setSelected(dayKey(today));
            }
          }}
        />

        <Row style={{ justifyContent: 'space-between' }}>
          <Title style={{ fontSize: 19 }}>{dayTitle(selected)}</Title>
          <Pressable onPress={() => setComposing(true)} style={styles.add} accessibilityLabel="Добавить цель">
            <Ionicons name="add" size={22} color="#fff" />
          </Pressable>
        </Row>

        {dayGoals.length === 0 && dayQuests.length === 0 ? (
          <Card>
            <Subtitle>
              На этот день ничего не запланировано. Нажмите «плюс», чтобы поставить цель себе,
              партнёру или общую на двоих.
            </Subtitle>
          </Card>
        ) : null}

        {dayGoals.map((goal) => {
          const owner = ownerLabel(goal);
          const done = Boolean(goal.done_at);
          return (
            <Pressable
              key={goal.id}
              onPress={() => toggleGoal(goal).catch(() => undefined)}
              onLongPress={() =>
                Alert.alert('Удалить цель?', goal.title, [
                  { text: 'Отмена', style: 'cancel' },
                  { text: 'Удалить', style: 'destructive', onPress: () => void deleteGoal(goal.id) },
                ])
              }
              style={[styles.goal, done && styles.goalDone]}
            >
              <Ionicons
                name={done ? 'checkmark-circle' : 'ellipse-outline'}
                size={24}
                color={done ? colors.green : colors.textFaint}
              />
              <View style={{ flex: 1, gap: 4 }}>
                <Text style={[styles.goalTitle, done && styles.goalTitleDone]}>{goal.title}</Text>
                {goal.note ? <Text style={styles.goalNote}>{goal.note}</Text> : null}
                <Row gap={6}>
                  <Chip label={owner.label} color={owner.color} filled />
                  {goal.created_by !== meId && goal.user_id === meId ? (
                    <Chip label="от партнёра" color={colors.textFaint} />
                  ) : null}
                </Row>
              </View>
            </Pressable>
          );
        })}

        {dayQuests.length ? (
          <View style={{ gap: spacing.sm }}>
            <Label>Дедлайны квестов</Label>
            {dayQuests.map((task) => (
              <Pressable
                key={task.id}
                onPress={() => router.push({ pathname: '/task/[id]', params: { id: task.id } })}
                style={styles.quest}
              >
                <View style={[styles.stripe, { backgroundColor: difficultyMeta[task.difficulty].color }]} />
                <View style={{ flex: 1, gap: 2 }}>
                  <Text style={styles.questTitle} numberOfLines={1}>
                    {task.title}
                  </Text>
                  <Text style={styles.questTime}>
                    до {task.due_at ? formatTime(task.due_at) : '—'}
                  </Text>
                </View>
                <Ionicons name="chevron-forward" size={18} color={colors.textFaint} />
              </Pressable>
            ))}
          </View>
        ) : null}
      </ScrollView>

      <GoalComposer
        visible={composing}
        dayKeyValue={selected}
        partnerName={partner?.display_name}
        hasPartner={Boolean(partner)}
        onClose={() => setComposing(false)}
        onSubmit={async (title, note, owner) => {
          const userId = owner === 'me' ? meId : owner === 'partner' ? (partner?.id ?? meId) : null;
          await createGoal({ title, note, goal_date: selected, user_id: userId });
        }}
      />
    </>
  );
}

interface ComposerProps {
  visible: boolean;
  dayKeyValue: string;
  partnerName?: string;
  hasPartner: boolean;
  onClose: () => void;
  onSubmit: (title: string, note: string, owner: Owner) => Promise<void>;
}

function GoalComposer({ visible, dayKeyValue, partnerName, hasPartner, onClose, onSubmit }: ComposerProps) {
  const [title, setTitle] = useState('');
  const [note, setNote] = useState('');
  const [owner, setOwner] = useState<Owner>('me');
  const [busy, setBusy] = useState(false);

  const close = () => {
    setTitle('');
    setNote('');
    setOwner('me');
    onClose();
  };

  const submit = async () => {
    if (!title.trim()) {
      Alert.alert('Нужно название', 'Коротко опишите цель.');
      return;
    }
    if (owner !== 'me' && !hasPartner) {
      Alert.alert('Партнёра ещё нет', 'Дождитесь, пока партнёр присоединится по коду.');
      return;
    }
    setBusy(true);
    try {
      await onSubmit(title, note, owner);
      close();
    } catch (e: any) {
      Alert.alert('Не удалось сохранить', e?.message ?? 'Попробуйте ещё раз.');
    } finally {
      setBusy(false);
    }
  };

  return (
    <Modal visible={visible} animationType="slide" transparent onRequestClose={close}>
      <View style={styles.backdrop}>
        <View style={styles.sheet}>
          <Row style={{ justifyContent: 'space-between' }}>
            <Title style={{ fontSize: 19 }}>Цель на {dayTitle(dayKeyValue).toLowerCase()}</Title>
            <Pressable onPress={close} hitSlop={12}>
              <Ionicons name="close" size={24} color={colors.textDim} />
            </Pressable>
          </Row>

          <Field label="Цель" value={title} onChangeText={setTitle} placeholder="Например: сходить в зал" />
          <Field
            label="Заметка"
            value={note}
            onChangeText={setNote}
            placeholder="Необязательно"
            multiline
            style={{ minHeight: 64, textAlignVertical: 'top' }}
          />

          <Label>Кому</Label>
          <Segmented
            value={owner}
            onChange={setOwner}
            options={[
              { value: 'me', label: 'Себе' },
              { value: 'partner', label: partnerName ?? 'Партнёру' },
              { value: 'both', label: 'Обоим' },
            ]}
          />

          <Button title="Поставить цель" onPress={submit} loading={busy} icon="flag-outline" />
        </View>
      </View>
    </Modal>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: colors.bg },
  add: {
    width: 40,
    height: 40,
    borderRadius: 20,
    backgroundColor: colors.primary,
    alignItems: 'center',
    justifyContent: 'center',
  },
  goal: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: spacing.md,
    backgroundColor: colors.card,
    borderRadius: radius.lg,
    borderWidth: 1,
    borderColor: colors.border,
    padding: spacing.md,
  },
  goalDone: { borderColor: colors.green + '55' },
  goalTitle: { color: colors.text, fontSize: 16, fontWeight: '600' },
  goalTitleDone: { color: colors.textDim, textDecorationLine: 'line-through' },
  goalNote: { color: colors.textDim, fontSize: 13, lineHeight: 18 },
  quest: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: spacing.md,
    backgroundColor: colors.card,
    borderRadius: radius.md,
    borderWidth: 1,
    borderColor: colors.border,
    paddingRight: spacing.md,
    overflow: 'hidden',
  },
  stripe: { width: 4, alignSelf: 'stretch' },
  questTitle: { color: colors.text, fontSize: 15, fontWeight: '600', paddingVertical: spacing.sm },
  questTime: { color: colors.textDim, fontSize: 12, paddingBottom: spacing.sm },
  backdrop: { flex: 1, backgroundColor: '#00000099', justifyContent: 'flex-end' },
  sheet: {
    backgroundColor: colors.bgElevated,
    borderTopLeftRadius: radius.xl,
    borderTopRightRadius: radius.xl,
    padding: spacing.lg,
    gap: spacing.md,
    borderTopWidth: 1,
    borderColor: colors.border,
  },
});
