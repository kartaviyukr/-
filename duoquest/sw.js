/**
 * Минимальный service worker: нужен, чтобы приложение считалось устанавливаемым
 * и открывалось при плохой связи.
 *
 * Стратегия намеренно простая — network-first с запасным ответом из кэша.
 * Приложение почти целиком работает с живыми данными Supabase, кэшировать
 * ответы API нельзя: партнёр увидел бы устаревшие квесты.
 */
const CACHE = 'duoquest-shell-v1';

self.addEventListener('install', (event) => {
  // Новая версия вступает в силу сразу, без ожидания закрытия всех вкладок.
  self.skipWaiting();
  event.waitUntil(caches.open(CACHE));
});

self.addEventListener('activate', (event) => {
  event.waitUntil(
    (async () => {
      const names = await caches.keys();
      await Promise.all(names.filter((name) => name !== CACHE).map((name) => caches.delete(name)));
      await self.clients.claim();
    })(),
  );
});

self.addEventListener('fetch', (event) => {
  const { request } = event;
  if (request.method !== 'GET') return;

  const url = new URL(request.url);
  // Чужие домены (в том числе Supabase) через кэш не пропускаем.
  if (url.origin !== self.location.origin) return;

  event.respondWith(
    (async () => {
      try {
        const response = await fetch(request);
        if (response && response.ok) {
          const cache = await caches.open(CACHE);
          cache.put(request, response.clone());
        }
        return response;
      } catch (error) {
        const cached = await caches.match(request);
        if (cached) return cached;

        // Переход по маршруту в офлайне: отдаём оболочку, дальше решает клиент.
        if (request.mode === 'navigate') {
          const shell = await caches.match('./');
          if (shell) return shell;
        }
        throw error;
      }
    })(),
  );
});
