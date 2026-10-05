/** Работа с календарной сеткой и датами без внешних библиотек. */

export const WEEKDAYS = ['Пн', 'Вт', 'Ср', 'Чт', 'Пт', 'Сб', 'Вс'];

export const MONTHS = [
  'Январь', 'Февраль', 'Март', 'Апрель', 'Май', 'Июнь',
  'Июль', 'Август', 'Сентябрь', 'Октябрь', 'Ноябрь', 'Декабрь',
];

/** Ключ дня YYYY-MM-DD в местном времени (не UTC — иначе дата «уезжает» на сутки). */
export function dayKey(date: Date): string {
  const pad = (n: number) => String(n).padStart(2, '0');
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}`;
}

/** Разбирает YYYY-MM-DD как местную дату, а не как UTC-полночь. */
export function parseDayKey(key: string): Date {
  const [year, month, day] = key.split('-').map(Number);
  return new Date(year, (month ?? 1) - 1, day ?? 1, 12, 0, 0, 0);
}

export function isSameDay(a: Date, b: Date): boolean {
  return dayKey(a) === dayKey(b);
}

export function addMonths(date: Date, delta: number): Date {
  // Число ставим первым, иначе 31 марта минус месяц даст 3 марта.
  return new Date(date.getFullYear(), date.getMonth() + delta, 1);
}

export interface CalendarCell {
  date: Date;
  key: string;
  inCurrentMonth: boolean;
  isToday: boolean;
}

/**
 * Сетка месяца: всегда целые недели с понедельника, чтобы таблица не прыгала
 * по высоте от месяца к месяцу.
 */
export function monthGrid(month: Date): CalendarCell[] {
  const first = new Date(month.getFullYear(), month.getMonth(), 1);
  // getDay(): воскресенье = 0, а неделя у нас начинается с понедельника.
  const offset = (first.getDay() + 6) % 7;

  const start = new Date(first);
  start.setDate(first.getDate() - offset);

  const today = new Date();
  const cells: CalendarCell[] = [];

  for (let i = 0; i < 42; i++) {
    const date = new Date(start.getFullYear(), start.getMonth(), start.getDate() + i);
    cells.push({
      date,
      key: dayKey(date),
      inCurrentMonth: date.getMonth() === month.getMonth(),
      isToday: isSameDay(date, today),
    });
  }

  // Шестая неделя нужна не всегда — лишнюю пустую строку убираем.
  const lastWeek = cells.slice(35);
  return lastWeek.every((cell) => !cell.inCurrentMonth) ? cells.slice(0, 35) : cells;
}

export function monthTitle(month: Date): string {
  const now = new Date();
  const year = month.getFullYear() === now.getFullYear() ? '' : ` ${month.getFullYear()}`;
  return `${MONTHS[month.getMonth()]}${year}`;
}

/** «Сегодня» / «Завтра» / «12 мая» — для заголовка выбранного дня. */
export function dayTitle(key: string): string {
  const date = parseDayKey(key);
  const today = new Date();
  const tomorrow = new Date();
  tomorrow.setDate(today.getDate() + 1);
  const yesterday = new Date();
  yesterday.setDate(today.getDate() - 1);

  if (isSameDay(date, today)) return 'Сегодня';
  if (isSameDay(date, tomorrow)) return 'Завтра';
  if (isSameDay(date, yesterday)) return 'Вчера';

  const months = [
    'января', 'февраля', 'марта', 'апреля', 'мая', 'июня',
    'июля', 'августа', 'сентября', 'октября', 'ноября', 'декабря',
  ];
  const year = date.getFullYear() === today.getFullYear() ? '' : ` ${date.getFullYear()}`;
  return `${date.getDate()} ${months[date.getMonth()]}${year}`;
}
