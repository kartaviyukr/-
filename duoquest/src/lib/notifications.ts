import * as Device from 'expo-device';
import * as Notifications from 'expo-notifications';
import Constants from 'expo-constants';
import { Platform } from 'react-native';
import type { Task } from '@/types';

/** Показывать уведомления, даже когда приложение открыто. */
Notifications.setNotificationHandler({
  handleNotification: async () => ({
    shouldShowBanner: true,
    shouldShowList: true,
    shouldPlaySound: true,
    shouldSetBadge: false,
  }),
});

export async function registerForPushNotifications(): Promise<string | null> {
  if (Platform.OS === 'android') {
    await Notifications.setNotificationChannelAsync('duoquest', {
      name: 'DuoQuest',
      importance: Notifications.AndroidImportance.MAX,
      vibrationPattern: [0, 250, 250, 250],
      lightColor: '#7C5CFF',
      sound: 'default',
    });
  }

  const existing = await Notifications.getPermissionsAsync();
  let status = existing.status;
  if (status !== 'granted') {
    const asked = await Notifications.requestPermissionsAsync();
    status = asked.status;
  }
  if (status !== 'granted') return null;

  // Push-токен нужен только для сообщений от партнёра и требует реального устройства
  // и настроенного EAS-проекта. Локальные напоминания работают и без него.
  if (!Device.isDevice) return null;
  const projectId =
    Constants.expoConfig?.extra?.eas?.projectId ?? process.env.EXPO_PUBLIC_EAS_PROJECT_ID;
  if (!projectId) return null;

  try {
    const token = await Notifications.getExpoPushTokenAsync({ projectId });
    return token.data;
  } catch (e) {
    console.warn('[DuoQuest] Не удалось получить push-токен:', e);
    return null;
  }
}

/** Идентификатор локального напоминания, чтобы уметь его пересоздать/снять. */
const tag = (taskId: string, suffix: string) => `dq:${taskId}:${suffix}`;

async function cancelByTag(prefix: string) {
  const scheduled = await Notifications.getAllScheduledNotificationsAsync();
  await Promise.all(
    scheduled
      .filter((n) => typeof n.content.data?.tag === 'string' && (n.content.data.tag as string).startsWith(prefix))
      .map((n) => Notifications.cancelScheduledNotificationAsync(n.identifier)),
  );
}

async function scheduleAt(when: Date, title: string, body: string, data: Record<string, unknown>) {
  if (when.getTime() <= Date.now() + 5_000) return;
  await Notifications.scheduleNotificationAsync({
    content: { title, body, data, sound: 'default' },
    trigger: {
      type: Notifications.SchedulableTriggerInputTypes.DATE,
      date: when,
      channelId: 'duoquest',
    },
  });
}

/**
 * Перепланирует все локальные напоминания одной задачи: реперные точки,
 * предупреждение за час до дедлайна и сам дедлайн.
 */
export async function syncTaskReminders(task: Task) {
  await cancelByTag(`dq:${task.id}:`);
  if (task.status === 'completed' || task.status === 'cancelled' || task.status === 'failed') return;

  for (const cp of task.checkpoints ?? []) {
    if (cp.done_at) continue;
    await scheduleAt(new Date(cp.remind_at), `⚔️ ${cp.title}`, `Реперная точка квеста «${task.title}»`, {
      tag: tag(task.id, `cp-${cp.id}`),
      taskId: task.id,
    });
  }

  if (task.due_at) {
    const due = new Date(task.due_at);
    await scheduleAt(new Date(due.getTime() - 3_600_000), '⏳ Остался час', `Дедлайн квеста «${task.title}» через час`, {
      tag: tag(task.id, 'due-1h'),
      taskId: task.id,
    });
    await scheduleAt(due, '🔥 Дедлайн!', `Срок квеста «${task.title}» истёк`, {
      tag: tag(task.id, 'due'),
      taskId: task.id,
    });
  }
}

/** Полная пересборка расписания — вызывается после каждой синхронизации списка. */
export async function syncAllReminders(tasks: Task[]) {
  await Notifications.cancelAllScheduledNotificationsAsync();
  for (const t of tasks) await syncTaskReminders(t);
}
