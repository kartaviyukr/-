/*
 * Service worker веб-версии: приложение целиком офлайновое, поэтому оболочка
 * кэшируется при установке. Стратегия — «сначала сеть, при её отсутствии кэш»:
 * так обновления подхватываются сразу, а без интернета всё продолжает работать.
 * Внутри APK service worker не регистрируется (страница открыта с file://).
 */
const CACHE = 'mnemo-v1';
const SHELL = ['./', 'index.html', 'style.css', 'app.js', 'data.js', 'guide.js', 'manifest.json',
  'icons/icon-192.png', 'icons/icon-512.png', 'icons/apple-touch-icon.png'];

self.addEventListener('install', event => {
  self.skipWaiting();
  event.waitUntil(caches.open(CACHE).then(c => c.addAll(SHELL)).catch(() => {}));
});

self.addEventListener('activate', event => {
  event.waitUntil((async () => {
    const names = await caches.keys();
    await Promise.all(names.filter(n => n !== CACHE).map(n => caches.delete(n)));
    await self.clients.claim();
  })());
});

self.addEventListener('fetch', event => {
  const { request } = event;
  if (request.method !== 'GET' || new URL(request.url).origin !== self.location.origin) return;
  event.respondWith((async () => {
    try {
      const response = await fetch(request);
      if (response && response.ok) {
        const cache = await caches.open(CACHE);
        cache.put(request, response.clone());
      }
      return response;
    } catch (error) {
      const cached = await caches.match(request, { ignoreSearch: true });
      if (cached) return cached;
      if (request.mode === 'navigate') {
        const shell = await caches.match('index.html');
        if (shell) return shell;
      }
      throw error;
    }
  })());
});
