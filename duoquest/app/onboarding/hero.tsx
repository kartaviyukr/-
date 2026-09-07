import React, { useState } from 'react';
import { Alert, Pressable, ScrollView, StyleSheet, Text, View } from 'react-native';
import { colors, radius, spacing } from '@/theme';
import { Button, Card, Field, Screen, Subtitle, Title } from '@/components/ui';
import { heroClasses } from '@/lib/game';
import { useSession } from '@/store/useSession';
import type { HeroClass } from '@/types';

const EMOJI = ['🧝', '🧙', '🦸', '🧛', '🥷', '👸', '🤴', '🧚', '🐉', '🦊', '🐺', '🦁'];

export default function HeroOnboarding() {
  const { profile, createCharacter } = useSession();
  const [name, setName] = useState(profile?.display_name ?? '');
  const [heroClass, setHeroClass] = useState<HeroClass>('warrior');
  const [emoji, setEmoji] = useState('🧝');
  const [busy, setBusy] = useState(false);

  const submit = async () => {
    setBusy(true);
    try {
      await createCharacter(name, heroClass, emoji);
    } catch (e: any) {
      Alert.alert('Не получилось', e?.message ?? 'Попробуйте ещё раз.');
    } finally {
      setBusy(false);
    }
  };

  return (
    <Screen>
      <View style={{ gap: spacing.sm }}>
        <Title>Создайте героя</Title>
        <Subtitle>
          За выполненные задания герой получает опыт и золото, за проваленные — теряет здоровье.
          Класс даёт небольшой стартовый бонус.
        </Subtitle>
      </View>

      <Card>
        <Field label="Имя героя" value={name} onChangeText={setName} placeholder="Как зовут вашего героя" />

        <View style={{ gap: spacing.sm }}>
          <Text style={styles.sectionLabel}>Аватар</Text>
          <ScrollView horizontal showsHorizontalScrollIndicator={false} contentContainerStyle={{ gap: spacing.sm }}>
            {EMOJI.map((e) => (
              <Pressable
                key={e}
                onPress={() => setEmoji(e)}
                style={[styles.emoji, emoji === e && styles.emojiActive]}
              >
                <Text style={{ fontSize: 26 }}>{e}</Text>
              </Pressable>
            ))}
          </ScrollView>
        </View>
      </Card>

      <View style={{ gap: spacing.sm }}>
        <Text style={styles.sectionLabel}>Класс</Text>
        {(Object.keys(heroClasses) as HeroClass[]).map((key) => {
          const k = heroClasses[key];
          const active = key === heroClass;
          return (
            <Pressable
              key={key}
              onPress={() => setHeroClass(key)}
              style={[styles.classRow, active && styles.classRowActive]}
            >
              <Text style={{ fontSize: 28 }}>{k.emoji}</Text>
              <View style={{ flex: 1 }}>
                <Text style={styles.className}>{k.label}</Text>
                <Text style={styles.classBlurb}>{k.blurb}</Text>
              </View>
              <Text style={[styles.classBonus, active && { color: colors.gold }]}>{k.bonus}</Text>
            </Pressable>
          );
        })}
      </View>

      <Button title="В путь!" onPress={submit} loading={busy} icon="sparkles-outline" />
    </Screen>
  );
}

const styles = StyleSheet.create({
  sectionLabel: {
    color: colors.textFaint,
    fontSize: 12,
    fontWeight: '700',
    textTransform: 'uppercase',
    letterSpacing: 0.6,
  },
  emoji: {
    width: 52,
    height: 52,
    borderRadius: radius.md,
    backgroundColor: colors.bgElevated,
    borderWidth: 1,
    borderColor: colors.border,
    alignItems: 'center',
    justifyContent: 'center',
  },
  emojiActive: { borderColor: colors.primary, backgroundColor: colors.primary + '22' },
  classRow: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: spacing.md,
    padding: spacing.md,
    borderRadius: radius.md,
    backgroundColor: colors.card,
    borderWidth: 1,
    borderColor: colors.border,
  },
  classRowActive: { borderColor: colors.primary, backgroundColor: colors.cardHi },
  className: { color: colors.text, fontWeight: '700', fontSize: 15 },
  classBlurb: { color: colors.textDim, fontSize: 12, lineHeight: 17 },
  classBonus: { color: colors.textFaint, fontSize: 11, fontWeight: '700' },
});
