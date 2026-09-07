import React, { useState } from 'react';
import { Alert, Share, StyleSheet, Text, View } from 'react-native';
import { colors, spacing } from '@/theme';
import { Button, Card, Field, Screen, Segmented, Subtitle, Title } from '@/components/ui';
import { useSession } from '@/store/useSession';

export default function CoupleOnboarding() {
  const [mode, setMode] = useState<'create' | 'join'>('create');
  const [name, setName] = useState('');
  const [code, setCode] = useState('');
  const [busy, setBusy] = useState(false);
  const { createCouple, joinCouple, couple, signOut } = useSession();

  const submit = async () => {
    setBusy(true);
    try {
      if (mode === 'create') {
        const created = await createCouple(name);
        Alert.alert(
          'Пара создана',
          `Код приглашения: ${created.invite_code}\n\nПередайте его партнёру — он вводит код на этом же экране.`,
        );
      } else {
        await joinCouple(code);
      }
    } catch (e: any) {
      Alert.alert('Не получилось', e?.message ?? 'Проверьте код и попробуйте снова.');
    } finally {
      setBusy(false);
    }
  };

  return (
    <Screen>
      <View style={{ gap: spacing.sm }}>
        <Title>Свяжите два телефона</Title>
        <Subtitle>
          Один из вас создаёт пару и получает код, второй вводит этот код. После этого квесты, сюжет и
          персонажи синхронизируются между Android и iPhone.
        </Subtitle>
      </View>

      <Segmented
        value={mode}
        onChange={setMode}
        options={[
          { value: 'create', label: 'Создать пару' },
          { value: 'join', label: 'Ввести код' },
        ]}
      />

      <Card>
        {mode === 'create' ? (
          <>
            <Field
              label="Название пары"
              value={name}
              onChangeText={setName}
              placeholder="Например: Мы вдвоём"
            />
            <Button title="Создать пару" onPress={submit} loading={busy} icon="heart-outline" />
          </>
        ) : (
          <>
            <Field
              label="Код приглашения"
              value={code}
              onChangeText={(t) => setCode(t.toUpperCase())}
              placeholder="ABC123"
              autoCapitalize="characters"
              maxLength={6}
              style={styles.code}
            />
            <Button title="Присоединиться" onPress={submit} loading={busy} icon="enter-outline" />
          </>
        )}
      </Card>

      {couple ? (
        <Card>
          <Text style={styles.codeLabel}>Ваш код приглашения</Text>
          <Text style={styles.codeBig}>{couple.invite_code}</Text>
          <Button
            title="Отправить партнёру"
            variant="ghost"
            icon="share-outline"
            onPress={() =>
              Share.share({
                message: `Заходи в DuoQuest, код нашей пары: ${couple.invite_code}`,
              })
            }
          />
        </Card>
      ) : null}

      <Button title="Выйти из аккаунта" variant="ghost" onPress={signOut} />
    </Screen>
  );
}

const styles = StyleSheet.create({
  code: { letterSpacing: 6, fontSize: 20, fontWeight: '800', textAlign: 'center' },
  codeLabel: { color: colors.textFaint, fontSize: 12, textAlign: 'center', textTransform: 'uppercase' },
  codeBig: { color: colors.gold, fontSize: 34, fontWeight: '900', letterSpacing: 8, textAlign: 'center' },
});
