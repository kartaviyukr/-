import React, { useState } from 'react';
import { Alert, FlatList, RefreshControl, StyleSheet, Text, View } from 'react-native';
import { colors, radius, spacing } from '@/theme';
import { Button, Card, EmptyState, Row, Subtitle } from '@/components/ui';
import { formatDateTime } from '@/lib/dates';
import { useQuests } from '@/store/useQuests';

export default function StoryScreen() {
  const { story, loading, loadAll, generateStory } = useQuests();
  const [busy, setBusy] = useState(false);

  const next = async () => {
    setBusy(true);
    try {
      const chapter = await generateStory();
      if (!chapter) {
        Alert.alert(
          'Глава не пришла',
          'Проверьте, что edge-функция generate-story развёрнута и в ней задан ключ LLM (переменная LLM_API_KEY).',
        );
      }
    } finally {
      setBusy(false);
    }
  };

  return (
    <View style={styles.container}>
      <FlatList
        data={story}
        keyExtractor={(item) => item.id}
        contentContainerStyle={{ padding: spacing.lg, gap: spacing.md, paddingBottom: 120 }}
        refreshControl={<RefreshControl refreshing={loading} onRefresh={loadAll} tintColor={colors.primary} />}
        ListHeaderComponent={
          <View style={{ gap: spacing.md, marginBottom: spacing.xs }}>
            <Subtitle>
              Хроника вашего приключения. Новая глава пишется нейросетью каждый раз, когда вы закрываете
              квест, — и опирается на то, что вы реально сделали.
            </Subtitle>
            <Button
              title="Написать следующую главу"
              icon="sparkles-outline"
              onPress={next}
              loading={busy}
              variant="ghost"
            />
          </View>
        }
        renderItem={({ item }) => (
          <Card>
            <Row style={{ justifyContent: 'space-between' }}>
              <Text style={styles.chapterNo}>Глава {item.chapter_no}</Text>
              <Text style={styles.date}>{formatDateTime(item.created_at)}</Text>
            </Row>
            <Text style={styles.title}>{item.title}</Text>
            <Text style={styles.body}>{item.body}</Text>
            {item.cliffhanger ? (
              <View style={styles.cliff}>
                <Text style={styles.cliffText}>…{item.cliffhanger}</Text>
              </View>
            ) : null}
          </Card>
        )}
        ListEmptyComponent={
          <EmptyState
            icon="book-outline"
            title="История ещё не началась"
            body="Закройте первый квест — и нейросеть напишет первую главу вашего приключения."
          />
        }
      />
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: colors.bg },
  chapterNo: { color: colors.gold, fontWeight: '800', fontSize: 12, textTransform: 'uppercase', letterSpacing: 1 },
  date: { color: colors.textFaint, fontSize: 11 },
  title: { color: colors.text, fontSize: 19, fontWeight: '800', lineHeight: 25 },
  body: { color: colors.textDim, fontSize: 15, lineHeight: 23 },
  cliff: {
    borderLeftWidth: 3,
    borderLeftColor: colors.primary,
    paddingLeft: spacing.md,
    paddingVertical: spacing.xs,
    backgroundColor: colors.bgElevated,
    borderRadius: radius.sm,
  },
  cliffText: { color: colors.primarySoft, fontSize: 14, fontStyle: 'italic', lineHeight: 21 },
});
