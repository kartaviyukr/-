import React, { useState } from 'react';
import { Alert, Linking, Pressable, StyleSheet, Text, View } from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import { colors, radius, spacing } from '@/theme';
import { Button, Card, Field, Label, Row, Segmented, Subtitle, Title } from '@/components/ui';
import { providers, type LlmProvider } from '@/lib/llm';
import { useSession } from '@/store/useSession';

/** Показывает ключ как sk-abc…1f09: понятно, что он тот самый, но целиком не светится. */
function maskKey(key: string): string {
  if (key.length <= 12) return '••••••••';
  return `${key.slice(0, 6)}…${key.slice(-4)}`;
}

export function LlmSettingsCard() {
  const { couple, updateLlmSettings } = useSession();
  const [editing, setEditing] = useState(false);
  const [draftKey, setDraftKey] = useState('');
  const [provider, setProvider] = useState<LlmProvider>((couple?.llm_provider as LlmProvider) || 'deepseek');
  const [busy, setBusy] = useState(false);

  const savedKey = couple?.llm_api_key ?? null;
  const info = providers[provider] ?? providers.deepseek;

  const save = async () => {
    const key = draftKey.trim();
    if (!key) {
      Alert.alert('Пустой ключ', 'Вставьте ключ целиком.');
      return;
    }
    if (info.keyPrefix && !key.startsWith(info.keyPrefix)) {
      Alert.alert(
        'Похоже, это не тот ключ',
        `Ключ ${info.label} начинается с «${info.keyPrefix}». Проверьте, что скопировали именно его.`,
      );
      return;
    }
    setBusy(true);
    try {
      await updateLlmSettings({ llm_provider: provider, llm_api_key: key });
      setDraftKey('');
      setEditing(false);
    } catch (e: any) {
      Alert.alert('Не удалось сохранить', e?.message ?? 'Попробуйте ещё раз.');
    } finally {
      setBusy(false);
    }
  };

  const clear = () =>
    Alert.alert('Убрать ключ?', 'Сюжет перестанет писаться, пока вы не вставите новый.', [
      { text: 'Отмена', style: 'cancel' },
      {
        text: 'Убрать',
        style: 'destructive',
        onPress: () => void updateLlmSettings({ llm_api_key: null }),
      },
    ]);

  return (
    <Card>
      <Row style={{ justifyContent: 'space-between' }}>
        <Title style={{ fontSize: 17 }}>Нейросеть</Title>
        {savedKey ? (
          <View style={styles.badge}>
            <Ionicons name="checkmark-circle" size={14} color={colors.green} />
            <Text style={styles.badgeText}>подключена</Text>
          </View>
        ) : null}
      </Row>

      <Subtitle>
        Пишет главы вашей истории по тому, что вы реально сделали. Ключ берётся из вашего аккаунта
        и хранится у пары — он виден обоим.
      </Subtitle>

      {savedKey && !editing ? (
        <>
          <View style={styles.keyBox}>
            <Text style={styles.keyLabel}>{providers[(couple?.llm_provider as LlmProvider) ?? 'deepseek']?.label}</Text>
            <Text style={styles.keyValue}>{maskKey(savedKey)}</Text>
          </View>
          <Row gap={spacing.sm}>
            <Button title="Заменить" variant="ghost" icon="key-outline" onPress={() => setEditing(true)} style={{ flex: 1 }} />
            <Button title="Убрать" variant="danger" onPress={clear} style={{ flex: 1 }} />
          </Row>
        </>
      ) : (
        <>
          <Label>Сервис</Label>
          <Segmented
            value={provider}
            onChange={setProvider}
            options={[
              { value: 'deepseek', label: 'DeepSeek' },
              { value: 'openrouter', label: 'OpenRouter' },
              { value: 'groq', label: 'Groq' },
            ]}
          />

          <Field
            label="Ключ API"
            value={draftKey}
            onChangeText={setDraftKey}
            placeholder={info.keyPrefix ? `${info.keyPrefix}…` : 'Вставьте ключ'}
            autoCapitalize="none"
            autoCorrect={false}
            secureTextEntry
          />

          <Pressable onPress={() => void Linking.openURL(info.keysUrl)}>
            <Text style={styles.link}>Где взять ключ {info.label} →</Text>
          </Pressable>

          <Row gap={spacing.sm}>
            <Button title="Сохранить" onPress={save} loading={busy} icon="save-outline" style={{ flex: 1 }} />
            {savedKey ? (
              <Button
                title="Отмена"
                variant="ghost"
                onPress={() => {
                  setDraftKey('');
                  setEditing(false);
                }}
                style={{ flex: 1 }}
              />
            ) : null}
          </Row>
        </>
      )}

      <Text style={styles.hint}>
        В веб-версии запрос к нейросети браузер не выпустит. Сгенерируйте главу на телефоне
        с установленным приложением — она появится у обоих.
      </Text>
    </Card>
  );
}

const styles = StyleSheet.create({
  badge: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 4,
    backgroundColor: colors.green + '1A',
    borderColor: colors.green + '55',
    borderWidth: 1,
    paddingHorizontal: 10,
    paddingVertical: 4,
    borderRadius: 999,
  },
  badgeText: { color: colors.green, fontSize: 11, fontWeight: '700' },
  keyBox: {
    backgroundColor: colors.bgElevated,
    borderRadius: radius.md,
    borderWidth: 1,
    borderColor: colors.border,
    padding: spacing.md,
    gap: 2,
  },
  keyLabel: { color: colors.textFaint, fontSize: 11, textTransform: 'uppercase', letterSpacing: 0.8 },
  keyValue: { color: colors.text, fontSize: 15, fontWeight: '700', letterSpacing: 1 },
  link: { color: colors.primarySoft, fontSize: 13, fontWeight: '600' },
  hint: { color: colors.textFaint, fontSize: 12, lineHeight: 17 },
});
