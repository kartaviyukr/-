import 'react-native-gesture-handler';
import React, { useEffect } from 'react';
import { ActivityIndicator, View } from 'react-native';
import { Stack, useRouter, useSegments } from 'expo-router';
import { StatusBar } from 'expo-status-bar';
import * as SplashScreen from 'expo-splash-screen';
import * as Notifications from 'expo-notifications';
import { SafeAreaProvider } from 'react-native-safe-area-context';
import { GestureHandlerRootView } from 'react-native-gesture-handler';
import { colors } from '@/theme';
import { useSession } from '@/store/useSession';

void SplashScreen.preventAutoHideAsync();

/** Держит пользователя в нужной ветке навигации: вход → пара → герой → приложение. */
function useAuthRedirect() {
  const { session, couple, character, hydrated } = useSession();
  const segments = useSegments();
  const router = useRouter();

  useEffect(() => {
    if (!hydrated) return;

    const group = segments[0];
    const inAuth = group === '(auth)';
    const inOnboarding = group === 'onboarding';

    if (!session) {
      if (!inAuth) router.replace('/(auth)');
      return;
    }
    if (!couple) {
      if (segments[1] !== 'couple') router.replace('/onboarding/couple');
      return;
    }
    if (!character) {
      if (segments[1] !== 'hero') router.replace('/onboarding/hero');
      return;
    }
    if (inAuth || inOnboarding) router.replace('/(tabs)');
  }, [session, couple, character, hydrated, segments, router]);
}

function RootNavigator() {
  const { hydrated, bootstrap } = useSession();
  const router = useRouter();

  useEffect(() => {
    void bootstrap();
  }, [bootstrap]);

  useEffect(() => {
    if (hydrated) void SplashScreen.hideAsync();
  }, [hydrated]);

  // Тап по уведомлению открывает соответствующий квест.
  useEffect(() => {
    const sub = Notifications.addNotificationResponseReceivedListener((response) => {
      const taskId = response.notification.request.content.data?.taskId;
      if (typeof taskId === 'string') router.push({ pathname: '/task/[id]', params: { id: taskId } });
    });
    return () => sub.remove();
  }, [router]);

  useAuthRedirect();

  if (!hydrated) {
    return (
      <View style={{ flex: 1, backgroundColor: colors.bg, alignItems: 'center', justifyContent: 'center' }}>
        <ActivityIndicator color={colors.primary} size="large" />
      </View>
    );
  }

  return (
    <Stack
      screenOptions={{
        headerStyle: { backgroundColor: colors.bg },
        headerTintColor: colors.text,
        headerTitleStyle: { fontWeight: '800' },
        contentStyle: { backgroundColor: colors.bg },
      }}
    >
      <Stack.Screen name="(auth)" options={{ headerShown: false }} />
      <Stack.Screen name="(tabs)" options={{ headerShown: false }} />
      <Stack.Screen name="onboarding/couple" options={{ title: 'Ваша пара' }} />
      <Stack.Screen name="onboarding/hero" options={{ title: 'Ваш герой' }} />
      <Stack.Screen name="task/new" options={{ title: 'Новый квест', presentation: 'modal' }} />
      <Stack.Screen name="task/[id]" options={{ title: 'Квест' }} />
    </Stack>
  );
}

export default function RootLayout() {
  return (
    <GestureHandlerRootView style={{ flex: 1 }}>
      <SafeAreaProvider>
        <StatusBar style="light" />
        <RootNavigator />
      </SafeAreaProvider>
    </GestureHandlerRootView>
  );
}
