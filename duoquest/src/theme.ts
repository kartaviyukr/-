/** Единая тёмная «фэнтези»-палитра приложения. */
export const colors = {
  bg: '#0B0A14',
  bgElevated: '#141225',
  card: '#1A1830',
  cardHi: '#221F3C',
  border: '#2E2A4D',
  primary: '#7C5CFF',
  primarySoft: '#A78BFA',
  pink: '#FF5C8D',
  gold: '#F7C948',
  green: '#3DD68C',
  red: '#FF5A5F',
  blue: '#4CC9F0',
  text: '#EDEBFF',
  textDim: '#9E98C4',
  textFaint: '#6B6690',
} as const;

export const radius = { sm: 8, md: 14, lg: 20, xl: 28 } as const;
export const spacing = { xs: 4, sm: 8, md: 12, lg: 16, xl: 24, xxl: 32 } as const;

/** Цвет и подпись для каждой сложности квеста. */
export const difficultyMeta = {
  trivial: { label: 'Пустяк', color: colors.textDim, xp: 20, gold: 10, stars: 1 },
  easy: { label: 'Лёгкий', color: colors.green, xp: 50, gold: 25, stars: 2 },
  normal: { label: 'Обычный', color: colors.blue, xp: 100, gold: 50, stars: 3 },
  hard: { label: 'Сложный', color: colors.pink, xp: 200, gold: 120, stars: 4 },
  epic: { label: 'Эпический', color: colors.gold, xp: 400, gold: 300, stars: 5 },
} as const;

export const statusMeta = {
  open: { label: 'Открыт', color: colors.blue },
  in_progress: { label: 'В работе', color: colors.primarySoft },
  submitted: { label: 'На проверке', color: colors.gold },
  completed: { label: 'Выполнен', color: colors.green },
  failed: { label: 'Провален', color: colors.red },
  cancelled: { label: 'Отменён', color: colors.textFaint },
} as const;

export const kindMeta = {
  personal: { label: 'Себе', icon: 'person' },
  assigned: { label: 'Партнёру', icon: 'send' },
  shared: { label: 'Общий', icon: 'people' },
} as const;
