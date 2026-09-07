import React from 'react';
import { Alert, RefreshControl, ScrollView, Share, StyleSheet, Text, View } from 'react-native';
import { colors, radius, spacing } from '@/theme';
import { Button, Card, Row, Subtitle, Title } from '@/components/ui';
import { HeroCard } from '@/components/HeroCard';
import { useSession } from '@/store/useSession';
import { useQuests } from '@/store/useQuests';

export default function ProfileScreen() {
  const { session, profile, couple, partner, character, partnerCharacter, refresh, signOut, leaveCouple } =
    useSession();
  const { tasks, loading } = useQuests();

  const completed = tasks.filter((t) => t.status === 'completed').length;
  const failed = tasks.filter((t) => t.status === 'failed').length;
  const rate = completed + failed > 0 ? Math.round((completed / (completed + failed)) * 100) : 100;

  const confirmLeave = () =>
    Alert.alert('Выйти из пары?', 'Квесты и сюжет останутся у партнёра. Вы сможете войти снова по коду.', [
      { text: 'Отмена', style: 'cancel' },
      { text: 'Выйти', style: 'destructive', onPress: () => void leaveCouple() },
    ]);

  return (
    <ScrollView
      style={styles.container}
      contentContainerStyle={{ padding: spacing.lg, gap: spacing.lg, paddingBottom: 120 }}
      refreshControl={<RefreshControl refreshing={loading} onRefresh={refresh} tintColor={colors.primary} />}
    >
      {character ? <HeroCard character={character} /> : null}

      <Card>
        <Title style={{ fontSize: 17 }}>Статистика</Title>
        <Row style={{ justifyContent: 'space-between' }}>
          <Stat value={String(completed)} label="Закрыто" color={colors.green} />
          <Stat value={String(failed)} label="Провалено" color={colors.red} />
          <Stat value={`${rate}%`} label="Успешность" color={colors.gold} />
          <Stat value={String(character?.streak ?? 0)} label="Серия" color={colors.primarySoft} />
        </Row>
      </Card>

      {partnerCharacter ? (
        <View style={{ gap: spacing.sm }}>
          <Text style={styles.sectionLabel}>Герой партнёра</Text>
          <HeroCard character={partnerCharacter} compact />
        </View>
      ) : null}

      <Card>
        <Title style={{ fontSize: 17 }}>{couple?.name ?? 'Пара'}</Title>
        <Subtitle>
          {partner
            ? `Вы в паре с ${partner.display_name}. Всё синхронизируется в реальном времени.`
            : 'Партнёр ещё не присоединился. Передайте ему код приглашения.'}
        </Subtitle>

        <View style={styles.codeBox}>
          <Text style={styles.codeLabel}>Код приглашения</Text>
          <Text style={styles.code}>{couple?.invite_code ?? '—'}</Text>
        </View>

        <Button
          title="Отправить код партнёру"
          variant="ghost"
          icon="share-outline"
          onPress={() =>
            Share.share({ message: `Заходи в DuoQuest, код нашей пары: ${couple?.invite_code}` })
          }
        />
      </Card>

      <Card>
        <Title style={{ fontSize: 17 }}>Аккаунт</Title>
        <Subtitle>
          {profile?.display_name} · вошли как {session?.user.email}
        </Subtitle>
        <Button title="Выйти из пары" variant="ghost" icon="exit-outline" onPress={confirmLeave} />
        <Button title="Выйти из аккаунта" variant="danger" icon="log-out-outline" onPress={signOut} />
      </Card>
    </ScrollView>
  );
}

function Stat({ value, label, color }: { value: string; label: string; color: string }) {
  return (
    <View style={{ alignItems: 'center', flex: 1, gap: 2 }}>
      <Text style={[styles.statValue, { color }]}>{value}</Text>
      <Text style={styles.statLabel}>{label}</Text>
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: colors.bg },
  sectionLabel: {
    color: colors.textFaint,
    fontSize: 12,
    fontWeight: '700',
    textTransform: 'uppercase',
    letterSpacing: 0.6,
  },
  statValue: { fontSize: 20, fontWeight: '900' },
  statLabel: { color: colors.textFaint, fontSize: 11 },
  codeBox: {
    backgroundColor: colors.bgElevated,
    borderRadius: radius.md,
    borderWidth: 1,
    borderColor: colors.border,
    paddingVertical: spacing.md,
    alignItems: 'center',
    gap: 2,
  },
  codeLabel: { color: colors.textFaint, fontSize: 11, textTransform: 'uppercase', letterSpacing: 1 },
  code: { color: colors.gold, fontSize: 26, fontWeight: '900', letterSpacing: 6 },
});
