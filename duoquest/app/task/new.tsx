import React, { useState } from 'react';
import { Alert, Pressable, StyleSheet, Switch, Text, View } from 'react-native';
import { useRouter } from 'expo-router';
import { Ionicons } from '@expo/vector-icons';
import { colors, difficultyMeta, radius, spacing } from '@/theme';
import { Button, Card, Field, Label, Row, Screen, Segmented, Subtitle } from '@/components/ui';
import { DateTimeField } from '@/components/DateTimeField';
import { difficultyDefaults } from '@/lib/game';
import { formatDateTime, tomorrowEvening } from '@/lib/dates';
import { useQuests, type NewCheckpointDraft } from '@/store/useQuests';
import { useSession } from '@/store/useSession';
import type { Difficulty, TaskKind } from '@/types';

export default function NewTaskScreen() {
  const router = useRouter();
  const { createTask } = useQuests();
  const { partner } = useSession();

  const [title, setTitle] = useState('');
  const [description, setDescription] = useState('');
  const [kind, setKind] = useState<TaskKind>('assigned');
  const [difficulty, setDifficulty] = useState<Difficulty>('normal');
  const [dueAt, setDueAt] = useState<Date | null>(tomorrowEvening());
  const [rewardText, setRewardText] = useState('');
  const [penaltyText, setPenaltyText] = useState('');
  const [requiresApproval, setRequiresApproval] = useState(true);
  const [checkpoints, setCheckpoints] = useState<NewCheckpointDraft[]>([]);
  const [busy, setBusy] = useState(false);

  const defaults = difficultyDefaults(difficulty);

  const addCheckpoint = () => {
    const base = dueAt ? new Date(dueAt.getTime() - 3 * 3_600_000) : new Date(Date.now() + 3_600_000);
    setCheckpoints([...checkpoints, { title: '', remind_at: base.toISOString() }]);
  };

  const submit = async () => {
    if (!title.trim()) {
      Alert.alert('Нужно название', 'Коротко опишите, что надо сделать.');
      return;
    }
    if (kind === 'assigned' && !partner) {
      Alert.alert('Партнёра ещё нет', 'Дождитесь, пока партнёр присоединится по коду, или создайте квест себе.');
      return;
    }
    setBusy(true);
    try {
      await createTask({
        title,
        description,
        kind,
        difficulty,
        due_at: dueAt ? dueAt.toISOString() : null,
        reward_text: rewardText,
        reward_xp: defaults.reward_xp,
        reward_gold: defaults.reward_gold,
        penalty_text: penaltyText,
        penalty_hp: defaults.penalty_hp,
        penalty_gold: defaults.penalty_gold,
        requires_approval: requiresApproval,
        checkpoints: checkpoints.filter((c) => c.title.trim() || c.remind_at),
        assignees: [],
      });
      router.back();
    } catch (e: any) {
      Alert.alert('Не удалось создать квест', e?.message ?? 'Попробуйте ещё раз.');
    } finally {
      setBusy(false);
    }
  };

  return (
    <Screen>
      <Card>
        <Field label="Что нужно сделать" value={title} onChangeText={setTitle} placeholder="Например: помыть посуду" />
        <Field
          label="Подробности"
          value={description}
          onChangeText={setDescription}
          placeholder="Необязательно"
          multiline
          numberOfLines={3}
          style={{ minHeight: 80, textAlignVertical: 'top' }}
        />
      </Card>

      <Card>
        <Label>Кому квест</Label>
        <Segmented
          value={kind}
          onChange={setKind}
          options={[
            { value: 'personal', label: 'Себе' },
            { value: 'assigned', label: partner?.display_name ?? 'Партнёру' },
            { value: 'shared', label: 'Обоим' },
          ]}
        />

        <Label>Сложность</Label>
        <View style={styles.difficultyRow}>
          {(Object.keys(difficultyMeta) as Difficulty[]).map((d) => {
            const meta = difficultyMeta[d];
            const active = d === difficulty;
            return (
              <Pressable
                key={d}
                onPress={() => setDifficulty(d)}
                style={[styles.diffChip, active && { borderColor: meta.color, backgroundColor: meta.color + '22' }]}
              >
                <Text style={[styles.diffLabel, active && { color: meta.color }]}>{meta.label}</Text>
                <Text style={styles.diffStars}>{'★'.repeat(meta.stars)}</Text>
              </Pressable>
            );
          })}
        </View>
        <Subtitle>
          Награда: {defaults.reward_xp} XP и {defaults.reward_gold} золота. Штраф за провал:{' '}
          {defaults.penalty_hp} HP и {defaults.penalty_gold} золота.
        </Subtitle>
      </Card>

      <Card>
        <DateTimeField label="Срок выполнения" value={dueAt} onChange={setDueAt} clearable />
        <Subtitle>
          За сутки до срока и в момент дедлайна придёт уведомление. Закрытие больше чем за сутки
          до срока даёт +25% к награде.
        </Subtitle>
      </Card>

      <Card>
        <Row style={{ justifyContent: 'space-between' }}>
          <Label>Реперные точки</Label>
          <Pressable onPress={addCheckpoint} hitSlop={10}>
            <Ionicons name="add-circle-outline" size={22} color={colors.primarySoft} />
          </Pressable>
        </Row>
        <Subtitle>Промежуточные напоминания на конкретное время внутри квеста.</Subtitle>

        {checkpoints.length === 0 ? (
          <Text style={styles.hint}>Пока не добавлено ни одной точки.</Text>
        ) : null}

        {checkpoints.map((cp, index) => (
          <View key={index} style={styles.checkpoint}>
            <Row style={{ justifyContent: 'space-between' }}>
              <Text style={styles.cpIndex}>Точка {index + 1}</Text>
              <Pressable onPress={() => setCheckpoints(checkpoints.filter((_, i) => i !== index))} hitSlop={10}>
                <Ionicons name="trash-outline" size={18} color={colors.red} />
              </Pressable>
            </Row>
            <Field
              value={cp.title}
              onChangeText={(t) =>
                setCheckpoints(checkpoints.map((c, i) => (i === index ? { ...c, title: t } : c)))
              }
              placeholder="Что напомнить"
            />
            <DateTimeField
              value={new Date(cp.remind_at)}
              onChange={(d) =>
                d &&
                setCheckpoints(
                  checkpoints.map((c, i) => (i === index ? { ...c, remind_at: d.toISOString() } : c)),
                )
              }
            />
          </View>
        ))}
      </Card>

      <Card>
        <Field
          label="🎁 Награда за выполнение"
          value={rewardText}
          onChangeText={setRewardText}
          placeholder="Например: массаж или выбор фильма на вечер"
        />
        <Field
          label="⚡ Наказание за провал"
          value={penaltyText}
          onChangeText={setPenaltyText}
          placeholder="Например: мыть посуду всю неделю"
        />
        <Subtitle>Текстом — то, что вы отдаёте друг другу в реальной жизни. Попадёт во вкладку «Долги».</Subtitle>

        <Row style={{ justifyContent: 'space-between' }}>
          <View style={{ flex: 1 }}>
            <Text style={styles.switchLabel}>Нужно подтверждение</Text>
            <Text style={styles.switchHint}>Партнёр проверяет, прежде чем квест закроется.</Text>
          </View>
          <Switch
            value={requiresApproval}
            onValueChange={setRequiresApproval}
            trackColor={{ true: colors.primary, false: colors.border }}
            thumbColor="#fff"
          />
        </Row>
      </Card>

      <Button title="Создать квест" onPress={submit} loading={busy} icon="flame-outline" />
      {dueAt ? <Subtitle style={{ textAlign: 'center' }}>Срок: {formatDateTime(dueAt)}</Subtitle> : null}
    </Screen>
  );
}

const styles = StyleSheet.create({
  difficultyRow: { flexDirection: 'row', flexWrap: 'wrap', gap: spacing.sm },
  diffChip: {
    paddingHorizontal: spacing.md,
    paddingVertical: spacing.sm,
    borderRadius: radius.md,
    borderWidth: 1,
    borderColor: colors.border,
    backgroundColor: colors.bgElevated,
    alignItems: 'center',
    gap: 2,
  },
  diffLabel: { color: colors.textDim, fontSize: 13, fontWeight: '700' },
  diffStars: { color: colors.gold, fontSize: 10 },
  checkpoint: {
    gap: spacing.sm,
    padding: spacing.md,
    borderRadius: radius.md,
    backgroundColor: colors.bgElevated,
    borderWidth: 1,
    borderColor: colors.border,
  },
  cpIndex: { color: colors.primarySoft, fontSize: 12, fontWeight: '700' },
  hint: { color: colors.textFaint, fontSize: 13, fontStyle: 'italic' },
  switchLabel: { color: colors.text, fontSize: 15, fontWeight: '600' },
  switchHint: { color: colors.textDim, fontSize: 12 },
});
