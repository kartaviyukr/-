import React, { useMemo, useState } from 'react';
import { FlatList, Pressable, RefreshControl, StyleSheet, Text, View } from 'react-native';
import { useRouter } from 'expo-router';
import { Ionicons } from '@expo/vector-icons';
import { colors, radius, spacing } from '@/theme';
import { Chip, EmptyState, Row, Segmented } from '@/components/ui';
import { TaskCard } from '@/components/TaskCard';
import { useQuests } from '@/store/useQuests';
import { useSession } from '@/store/useSession';
import type { Task } from '@/types';

type Filter = 'active' | 'mine' | 'partner' | 'shared' | 'done';

export default function QuestsScreen() {
  const router = useRouter();
  const [filter, setFilter] = useState<Filter>('active');
  const { tasks, loading, loadAll, banner, clearBanner } = useQuests();
  const { session, partner, character } = useSession();
  const meId = session?.user.id ?? null;

  const filtered = useMemo(() => {
    const isOpen = (t: Task) => t.status !== 'completed' && t.status !== 'cancelled';
    const assignedTo = (t: Task, id: string | null) => (t.assignees ?? []).some((a) => a.user_id === id);

    switch (filter) {
      case 'active':
        return tasks.filter(isOpen);
      case 'mine':
        return tasks.filter((t) => isOpen(t) && assignedTo(t, meId) && (t.assignees ?? []).length === 1);
      case 'partner':
        return tasks.filter((t) => isOpen(t) && partner && assignedTo(t, partner.id) && (t.assignees ?? []).length === 1);
      case 'shared':
        return tasks.filter((t) => isOpen(t) && (t.assignees ?? []).length > 1);
      case 'done':
        return tasks.filter((t) => t.status === 'completed' || t.status === 'failed');
    }
  }, [tasks, filter, meId, partner]);

  const overdue = tasks.filter(
    (t) => t.due_at && new Date(t.due_at) < new Date() && t.status !== 'completed' && t.status !== 'cancelled',
  ).length;

  return (
    <View style={styles.container}>
      {banner ? (
        <Pressable
          onPress={clearBanner}
          style={[styles.banner, { borderColor: banner.tone === 'good' ? colors.green : colors.red }]}
        >
          <Text style={[styles.bannerTitle, { color: banner.tone === 'good' ? colors.green : colors.red }]}>
            {banner.title}
          </Text>
          <Text style={styles.bannerBody}>{banner.body}</Text>
        </Pressable>
      ) : null}

      <View style={styles.header}>
        <Row style={{ justifyContent: 'space-between' }}>
          <View>
            <Text style={styles.greeting}>{character ? `${character.avatar_emoji} ${character.name}` : 'Квесты'}</Text>
            <Row gap={6}>
              <Chip label={`Активных: ${tasks.filter((t) => t.status !== 'completed' && t.status !== 'cancelled').length}`} />
              {overdue ? <Chip label={`Просрочено: ${overdue}`} color={colors.red} filled /> : null}
            </Row>
          </View>
          <Pressable onPress={() => router.push('/task/new')} style={styles.fabSmall}>
            <Ionicons name="add" size={26} color="#fff" />
          </Pressable>
        </Row>

        <Segmented
          value={filter}
          onChange={setFilter}
          options={[
            { value: 'active', label: 'Все' },
            { value: 'mine', label: 'Мои' },
            { value: 'partner', label: 'Партнёра' },
            { value: 'shared', label: 'Общие' },
            { value: 'done', label: 'Архив' },
          ]}
        />
      </View>

      <FlatList
        data={filtered}
        keyExtractor={(item) => item.id}
        contentContainerStyle={{ padding: spacing.lg, paddingTop: 0, gap: spacing.md, paddingBottom: 120 }}
        refreshControl={
          <RefreshControl refreshing={loading} onRefresh={loadAll} tintColor={colors.primary} />
        }
        renderItem={({ item }) => (
          <TaskCard
            task={item}
            meId={meId}
            partnerName={partner?.display_name}
            onPress={() => router.push({ pathname: '/task/[id]', params: { id: item.id } })}
          />
        )}
        ListEmptyComponent={
          <EmptyState
            icon="document-text-outline"
            title="Здесь пока пусто"
            body={
              filter === 'done'
                ? 'Закрытые и проваленные квесты будут собираться тут.'
                : 'Создайте первый квест — себе, партнёру или общий на двоих.'
            }
          />
        }
      />
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: colors.bg },
  header: { padding: spacing.lg, gap: spacing.md },
  greeting: { color: colors.text, fontSize: 20, fontWeight: '800', marginBottom: 6 },
  fabSmall: {
    width: 46,
    height: 46,
    borderRadius: 23,
    backgroundColor: colors.primary,
    alignItems: 'center',
    justifyContent: 'center',
  },
  banner: {
    margin: spacing.lg,
    marginBottom: 0,
    padding: spacing.md,
    borderRadius: radius.md,
    borderWidth: 1,
    backgroundColor: colors.card,
    gap: 2,
  },
  bannerTitle: { fontWeight: '800', fontSize: 15 },
  bannerBody: { color: colors.textDim, fontSize: 13 },
});
