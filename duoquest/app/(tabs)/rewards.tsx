import React, { useState } from 'react';
import { FlatList, RefreshControl, StyleSheet, Text, View } from 'react-native';
import { colors, radius, spacing } from '@/theme';
import { Button, Chip, EmptyState, Row, Segmented, Subtitle } from '@/components/ui';
import { formatDateTime } from '@/lib/dates';
import { useQuests } from '@/store/useQuests';
import { useSession } from '@/store/useSession';

type Tab = 'mine' | 'partner' | 'archive';

export default function RewardsScreen() {
  const [tab, setTab] = useState<Tab>('mine');
  const { ledger, loading, loadAll, redeem } = useQuests();
  const { session, partner } = useSession();
  const meId = session?.user.id;

  const items = ledger.filter((e) => {
    if (tab === 'archive') return Boolean(e.redeemed_at);
    if (e.redeemed_at) return false;
    return tab === 'mine' ? e.user_id === meId : e.user_id !== meId;
  });

  return (
    <View style={styles.container}>
      <View style={{ padding: spacing.lg, gap: spacing.md }}>
        <Subtitle>
          Здесь копятся обещанные награды и назначенные наказания. Отметьте «Закрыто», когда долг
          отдан в реальной жизни.
        </Subtitle>
        <Segmented
          value={tab}
          onChange={setTab}
          options={[
            { value: 'mine', label: 'Мне' },
            { value: 'partner', label: partner?.display_name ?? 'Партнёру' },
            { value: 'archive', label: 'Закрытые' },
          ]}
        />
      </View>

      <FlatList
        data={items}
        keyExtractor={(item) => item.id}
        contentContainerStyle={{ padding: spacing.lg, paddingTop: 0, gap: spacing.md, paddingBottom: 120 }}
        refreshControl={<RefreshControl refreshing={loading} onRefresh={loadAll} tintColor={colors.primary} />}
        renderItem={({ item }) => {
          const reward = item.kind === 'reward';
          const accent = reward ? colors.green : colors.red;
          return (
            <View style={[styles.card, { borderColor: accent + '55' }]}>
              <Row style={{ justifyContent: 'space-between' }}>
                <Chip label={reward ? '🎁 Награда' : '⚡ Наказание'} color={accent} filled />
                <Text style={styles.date}>{formatDateTime(item.created_at)}</Text>
              </Row>
              <Text style={styles.text}>{item.text}</Text>
              {item.redeemed_at ? (
                <Text style={styles.redeemed}>Закрыто {formatDateTime(item.redeemed_at)}</Text>
              ) : (
                <Button
                  title="Закрыто"
                  variant="ghost"
                  icon="checkmark-done-outline"
                  onPress={() => redeem(item.id)}
                />
              )}
            </View>
          );
        }}
        ListEmptyComponent={
          <EmptyState
            icon="gift-outline"
            title="Долгов нет"
            body="Когда квест с наградой или наказанием закроется, запись появится здесь."
          />
        }
      />
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: colors.bg },
  card: {
    backgroundColor: colors.card,
    borderRadius: radius.lg,
    borderWidth: 1,
    padding: spacing.lg,
    gap: spacing.sm,
  },
  text: { color: colors.text, fontSize: 16, lineHeight: 22, fontWeight: '600' },
  date: { color: colors.textFaint, fontSize: 11 },
  redeemed: { color: colors.textFaint, fontSize: 12, fontStyle: 'italic' },
});
