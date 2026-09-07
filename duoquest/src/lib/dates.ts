const MINUTE = 60_000;
const HOUR = 60 * MINUTE;
const DAY = 24 * HOUR;

const MONTHS = [
  'января', 'февраля', 'марта', 'апреля', 'мая', 'июня',
  'июля', 'августа', 'сентября', 'октября', 'ноября', 'декабря',
];

function pad(n: number) {
  return String(n).padStart(2, '0');
}

export function formatDateTime(iso: string | Date | null): string {
  if (!iso) return '—';
  const d = typeof iso === 'string' ? new Date(iso) : iso;
  const now = new Date();
  const sameYear = d.getFullYear() === now.getFullYear();
  const date = `${d.getDate()} ${MONTHS[d.getMonth()]}${sameYear ? '' : ' ' + d.getFullYear()}`;
  return `${date}, ${pad(d.getHours())}:${pad(d.getMinutes())}`;
}

export function formatTime(iso: string | Date): string {
  const d = typeof iso === 'string' ? new Date(iso) : iso;
  return `${pad(d.getHours())}:${pad(d.getMinutes())}`;
}

/** Русское склонение: 1 час / 2 часа / 5 часов. */
function plural(n: number, one: string, few: string, many: string) {
  const mod10 = n % 10;
  const mod100 = n % 100;
  if (mod10 === 1 && mod100 !== 11) return one;
  if (mod10 >= 2 && mod10 <= 4 && (mod100 < 12 || mod100 > 14)) return few;
  return many;
}

/** «через 3 часа» / «просрочено на 2 дня». */
export function relativeDeadline(iso: string | null): { text: string; overdue: boolean; urgent: boolean } {
  if (!iso) return { text: 'Без срока', overdue: false, urgent: false };
  const diff = new Date(iso).getTime() - Date.now();
  const overdue = diff < 0;
  const abs = Math.abs(diff);

  let value: string;
  if (abs < HOUR) {
    const m = Math.max(1, Math.round(abs / MINUTE));
    value = `${m} ${plural(m, 'минуту', 'минуты', 'минут')}`;
  } else if (abs < DAY) {
    const h = Math.round(abs / HOUR);
    value = `${h} ${plural(h, 'час', 'часа', 'часов')}`;
  } else {
    const d = Math.round(abs / DAY);
    value = `${d} ${plural(d, 'день', 'дня', 'дней')}`;
  }

  return {
    text: overdue ? `Просрочено на ${value}` : `Осталось ${value}`,
    overdue,
    urgent: !overdue && diff < 6 * HOUR,
  };
}

/** Ближайшая «круглая» дата для дефолта в форме — завтра в 20:00. */
export function tomorrowEvening(): Date {
  const d = new Date();
  d.setDate(d.getDate() + 1);
  d.setHours(20, 0, 0, 0);
  return d;
}
