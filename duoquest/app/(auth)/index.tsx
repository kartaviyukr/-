import React, { useState } from 'react';
import { Alert, KeyboardAvoidingView, Platform, StyleSheet, Text, View } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { colors, spacing } from '@/theme';
import { Button, Field, Subtitle, Title } from '@/components/ui';
import { useSession } from '@/store/useSession';
import { isSupabaseConfigured } from '@/lib/supabase';

export default function AuthScreen() {
  const [mode, setMode] = useState<'signin' | 'signup'>('signin');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [name, setName] = useState('');
  const { signIn, signUp, loading } = useSession();

  const submit = async () => {
    if (!isSupabaseConfigured) {
      Alert.alert(
        'Бэкенд не настроен',
        'В сборке нет адреса Supabase. Заполните .env (EXPO_PUBLIC_SUPABASE_URL и EXPO_PUBLIC_SUPABASE_ANON_KEY) и пересоберите приложение.',
      );
      return;
    }
    if (!email.trim() || password.length < 6) {
      Alert.alert('Проверьте поля', 'Нужна почта и пароль не короче 6 символов.');
      return;
    }
    try {
      if (mode === 'signin') {
        await signIn(email, password);
      } else {
        if (!name.trim()) {
          Alert.alert('Как вас зовут?', 'Укажите имя — партнёр увидит его в квестах.');
          return;
        }
        await signUp(email, password, name);
        Alert.alert(
          'Аккаунт создан',
          'Если в проекте включено подтверждение почты, откройте письмо и вернитесь сюда для входа.',
        );
      }
    } catch (e: any) {
      Alert.alert('Не получилось', e?.message ?? 'Неизвестная ошибка');
    }
  };

  return (
    <SafeAreaView style={styles.safe}>
      <KeyboardAvoidingView
        behavior={Platform.OS === 'ios' ? 'padding' : undefined}
        style={styles.container}
      >
        <View style={styles.hero}>
          <Text style={styles.logo}>⚔️💜</Text>
          <Title style={{ fontSize: 30 }}>DuoQuest</Title>
          <Subtitle style={{ textAlign: 'center' }}>
            Задания, дедлайны, награды и наказания для двоих — в виде настоящей RPG.
          </Subtitle>
        </View>

        <View style={{ gap: spacing.md }}>
          {mode === 'signup' ? (
            <Field label="Имя" value={name} onChangeText={setName} placeholder="Как вас зовут" />
          ) : null}

          <Field
            label="Почта"
            value={email}
            onChangeText={setEmail}
            placeholder="you@example.com"
            autoCapitalize="none"
            keyboardType="email-address"
            autoComplete="email"
          />
          <Field
            label="Пароль"
            value={password}
            onChangeText={setPassword}
            placeholder="Минимум 6 символов"
            secureTextEntry
          />

          <Button
            title={mode === 'signin' ? 'Войти' : 'Создать аккаунт'}
            onPress={submit}
            loading={loading}
            icon={mode === 'signin' ? 'log-in-outline' : 'person-add-outline'}
          />
          <Button
            title={mode === 'signin' ? 'У меня ещё нет аккаунта' : 'У меня уже есть аккаунт'}
            variant="ghost"
            onPress={() => setMode(mode === 'signin' ? 'signup' : 'signin')}
          />
        </View>
      </KeyboardAvoidingView>
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  safe: { flex: 1, backgroundColor: colors.bg },
  container: { flex: 1, padding: spacing.xl, justifyContent: 'center', gap: spacing.xxl },
  hero: { alignItems: 'center', gap: spacing.sm },
  logo: { fontSize: 52 },
});
