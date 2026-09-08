/**
 * Дописывает в собранный index.html всё, из-за чего iOS соглашается поставить
 * страницу на домашний экран как приложение.
 *
 * Зачем отдельный шаг: expo-router умеет подменять оболочку HTML через
 * app/+html.tsx, но только при статическом рендеринге. У нас web.output = 'single'
 * (SPA), и в этом режиме Expo берёт собственный шаблон, а +html.tsx игнорирует —
 * молча, без предупреждения. Поэтому теги вставляются здесь, после экспорта.
 *
 * Запуск: node scripts/pwa-postbuild.mjs <каталог-сборки> [базовый-путь]
 */
import { readFile, writeFile } from 'node:fs/promises';
import { join } from 'node:path';

const distDir = process.argv[2] ?? 'dist';
const baseUrl = (process.argv[3] ?? process.env.EXPO_PUBLIC_BASE_URL ?? '').replace(/\/$/, '');
const asset = (path) => `${baseUrl}/${path}`;

const HEAD = `
    <meta name="description" content="Задания, сроки, награды и наказания для двоих — в виде RPG." />
    <link rel="manifest" href="${asset('manifest.json')}" />
    <meta name="theme-color" content="#0B0A14" />
    <meta name="color-scheme" content="dark" />

    <!-- iOS не читает manifest.json: установку на домашний экран включают эти теги -->
    <meta name="apple-mobile-web-app-capable" content="yes" />
    <meta name="mobile-web-app-capable" content="yes" />
    <meta name="apple-mobile-web-app-status-bar-style" content="black-translucent" />
    <meta name="apple-mobile-web-app-title" content="DuoQuest" />
    <link rel="apple-touch-icon" href="${asset('icons/apple-touch-icon.png')}" />
    <link rel="icon" type="image/png" sizes="192x192" href="${asset('icons/icon-192.png')}" />

    <style id="duoquest-shell">
      html, body, #root { background-color: #0B0A14; }
      body {
        margin: 0;
        overscroll-behavior-y: none;
        -webkit-tap-highlight-color: transparent;
      }
      /* Безопасные зоны айфона: в установленном приложении статус-бар накладывается на контент */
      #root {
        box-sizing: border-box;
        padding-top: env(safe-area-inset-top);
        padding-bottom: env(safe-area-inset-bottom);
      }
    </style>

    <script>
      if ('serviceWorker' in navigator) {
        window.addEventListener('load', function () {
          navigator.serviceWorker.register('${asset('sw.js')}').catch(function () {
            /* без service worker приложение просто работает онлайн */
          });
        });
      }
    </script>
`;

const indexPath = join(distDir, 'index.html');
let html = await readFile(indexPath, 'utf8');

if (html.includes('apple-mobile-web-app-capable')) {
  console.log('PWA-теги уже на месте, пропускаю');
  process.exit(0);
}

// Язык страницы: шаблон Expo проставляет en, приложение русскоязычное.
html = html.replace(/<html lang="[^"]*">/, '<html lang="ru">');

// Шаблон Expo отдаёт httpEquiv — это React-имя атрибута, в HTML его не существует.
html = html.replace(/httpEquiv=/g, 'http-equiv=');

// viewport-fit=cover нужен, чтобы приложение занимало экран целиком под «чёлкой».
html = html.replace(
  /<meta name="viewport"[^>]*\/?>/,
  '<meta name="viewport" content="width=device-width, initial-scale=1, maximum-scale=1, shrink-to-fit=no, viewport-fit=cover" />',
);

if (!html.includes('</head>')) {
  throw new Error('В собранном index.html нет </head> — шаблон Expo изменился, скрипт надо поправить');
}
html = html.replace('</head>', `${HEAD}  </head>`);

await writeFile(indexPath, html);
console.log(`PWA-теги добавлены в ${indexPath} (базовый путь: ${baseUrl || 'корень'})`);
