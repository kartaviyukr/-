import type { Task } from '@/types';

/**
 * Веб-версия модуля уведомлений (Metro подставляет её вместо notifications.ts
 * при сборке PWA).
 *
 * Ограничение платформы: в вебе нет системного планировщика, который разбудит
 * приложение в заданный момент. Поэтому напоминания ставятся таймерами и
 * срабатывают, только пока вкладка или установленное приложение открыто.
 * Всё, что должно приходить при закрытом приложении, требует Web Push
 * с отправкой по расписанию на сервере.
 */

type Timer = ReturnType<typeof setTimeout>;

/** Активные таймеры по идентификатору напоминания — чтобы уметь их пересоздавать. */
const timers = new Map<string, Timer>();

const canNotify = () => typeof window !== 'undefined' && 'Notification' in window;

export async function registerForPushNotifications(): Promise<string | null> {
  if (!canNotify()) return null;
  try {
    if (Notification.permission === 'default') await Notification.requestPermission();
  } catch {
    /* пользователь мог запретить показ запроса — не повод падать */
  }
  // Push-токена в этой сборке нет: сообщения от партнёра приходят через realtime,
  // пока приложение открыто.
  return null;
}

function show(title: string, body: string, taskId: string) {
  if (!canNotify() || Notification.permission !== 'granted') return;
  try {
    const notification = new Notification(title, { body, tag: taskId, icon: './icons/icon-192.png' });
    notification.onclick = () => {
      window.focus();
      window.location.hash = '';
      notification.close();
    };
  } catch {
    /* некоторые браузеры запрещают конструктор вне service worker */
  }
}

function schedule(key: string, when: Date, title: string, body: string, taskId: string) {
  const delay = when.getTime() - Date.now();
  // setTimeout не переживает задержку больше ~24 суток (переполнение int32).
  if (delay <= 0 || delay > 2_147_483_647) return;
  timers.set(
    key,
    setTimeout(() => {
      timers.delete(key);
      show(title, body, taskId);
    }, delay),
  );
}

function clearFor(taskId: string) {
  for (const [key, timer] of timers) {
    if (key.startsWith(`${taskId}:`)) {
      clearTimeout(timer);
      timers.delete(key);
    }
  }
}

export async function syncTaskReminders(task: Task) {
  clearFor(task.id);
  if (task.status === 'completed' || task.status === 'cancelled' || task.status === 'failed') return;

  for (const cp of task.checkpoints ?? []) {
    if (cp.done_at) continue;
    schedule(
      `${task.id}:cp-${cp.id}`,
      new Date(cp.remind_at),
      `⚔️ ${cp.title}`,
      `Реперная точка квеста «${task.title}»`,
      task.id,
    );
  }

  if (task.due_at) {
    const due = new Date(task.due_at);
    schedule(
      `${task.id}:due-1h`,
      new Date(due.getTime() - 3_600_000),
      '⏳ Остался час',
      `Дедлайн квеста «${task.title}» через час`,
      task.id,
    );
    schedule(`${task.id}:due`, due, '🔥 Дедлайн!', `Срок квеста «${task.title}» истёк`, task.id);
  }
}

export async function syncAllReminders(tasks: Task[]) {
  for (const timer of timers.values()) clearTimeout(timer);
  timers.clear();
  for (const task of tasks) await syncTaskReminders(task);
}

/** В вебе уведомление открывает вкладку само — отдельный обработчик не нужен. */
export function addNotificationResponseListener(_onTaskId: (taskId: string) => void): () => void {
  return () => {};
}
