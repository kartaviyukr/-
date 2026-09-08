import type { ExpoConfig } from 'expo/config';

const config: ExpoConfig = {
  name: 'DuoQuest',
  slug: 'duoquest',
  version: '1.0.0',
  orientation: 'portrait',
  scheme: 'duoquest',
  userInterfaceStyle: 'dark',
  newArchEnabled: true,
  assetBundlePatterns: ['**/*'],
  ios: {
    supportsTablet: true,
    bundleIdentifier: 'com.duoquest.app',
    infoPlist: {
      UIBackgroundModes: ['remote-notification'],
      ITSAppUsesNonExemptEncryption: false,
    },
  },
  android: {
    package: 'com.duoquest.app',
    adaptiveIcon: {
      foregroundImage: './assets/adaptive-icon.png',
      backgroundColor: '#0B0A14',
    },
    edgeToEdgeEnabled: true,
    permissions: [
      'android.permission.POST_NOTIFICATIONS',
      'android.permission.SCHEDULE_EXACT_ALARM',
      'android.permission.VIBRATE',
      'android.permission.RECEIVE_BOOT_COMPLETED',
    ],
  },
  icon: './assets/icon.png',
  plugins: [
    'expo-router',
    [
      'expo-notifications',
      {
        color: '#7C5CFF',
        defaultChannel: 'duoquest',
      },
    ],
    [
      'expo-splash-screen',
      {
        backgroundColor: '#0B0A14',
        image: './assets/splash.png',
        imageWidth: 200,
      },
    ],
  ],
  web: {
    // SPA: одна index.html и маршрутизация на клиенте. Для GitHub Pages это
    // важно — там нет сервера, который умел бы отдавать index.html на любой путь.
    bundler: 'metro',
    output: 'single',
    favicon: './assets/favicon.png',
  },
  experiments: {
    typedRoutes: true,
    // Сайт живёт не в корне домена, а в подпапке репозитория.
    // Значение подставляется сборкой; локально baseUrl не нужен.
    baseUrl: process.env.EXPO_PUBLIC_BASE_URL || undefined,
  },
  extra: {
    router: {},
    eas: {
      projectId: process.env.EXPO_PUBLIC_EAS_PROJECT_ID || undefined,
    },
  },
};

export default config;
