import React from 'react';
import { ScrollViewStyleReset } from 'expo-router/html';
import type { PropsWithChildren } from 'react';

/**
 * Оболочка HTML для веб-сборки. Живёт только в вебе и на нативные платформы
 * не попадает.
 *
 * Здесь собрано всё, из-за чего iOS соглашается ставить страницу на домашний
 * экран как приложение: манифест, apple-touch-icon и мета-теги
 * apple-mobile-web-app-*. Без них Safari сделает обычную закладку.
 */
export default function Root({ children }: PropsWithChildren) {
  return (
    <html lang="ru">
      <head>
        <meta charSet="utf-8" />
        <meta httpEquiv="X-UA-Compatible" content="IE=edge" />
        {/* viewport-fit=cover — чтобы приложение занимало экран целиком под «чёлкой» */}
        <meta
          name="viewport"
          content="width=device-width, initial-scale=1, maximum-scale=1, viewport-fit=cover"
        />

        <title>DuoQuest</title>
        <meta name="description" content="Задания, сроки, награды и наказания для двоих — в виде RPG." />

        <link rel="manifest" href="./manifest.json" />
        <meta name="theme-color" content="#0B0A14" />
        <meta name="color-scheme" content="dark" />

        {/* iOS не читает manifest.json — ему нужны собственные теги */}
        <meta name="apple-mobile-web-app-capable" content="yes" />
        <meta name="mobile-web-app-capable" content="yes" />
        <meta name="apple-mobile-web-app-status-bar-style" content="black-translucent" />
        <meta name="apple-mobile-web-app-title" content="DuoQuest" />
        <link rel="apple-touch-icon" href="./icons/apple-touch-icon.png" />
        <link rel="icon" type="image/png" sizes="192x192" href="./icons/icon-192.png" />

        {/* Отключает «резиновую» прокрутку body: скроллом занимается сам React Native Web */}
        <ScrollViewStyleReset />

        <style dangerouslySetInnerHTML={{ __html: BASE_STYLE }} />
        <script dangerouslySetInnerHTML={{ __html: REGISTER_SW }} />
      </head>
      <body>{children}</body>
    </html>
  );
}

const BASE_STYLE = `
html, body, #root {
  height: 100%;
  background-color: #0B0A14;
}
body {
  margin: 0;
  overscroll-behavior-y: none;
  -webkit-tap-highlight-color: transparent;
}
/* Учитываем безопасные зоны айфона в установленном приложении */
#root {
  padding-top: env(safe-area-inset-top);
  padding-bottom: env(safe-area-inset-bottom);
  box-sizing: border-box;
}
`;

const REGISTER_SW = `
if ('serviceWorker' in navigator) {
  window.addEventListener('load', function () {
    navigator.serviceWorker.register('./sw.js').catch(function () {
      /* без service worker приложение просто работает онлайн */
    });
  });
}
`;
