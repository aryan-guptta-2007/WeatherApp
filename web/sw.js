const CACHE_NAME = 'weather-app-v1';
const STATIC_ASSETS = [
    './',
    './index.html',
    './style.css',
    './app.js',
    './manifest.json',
    './assets/clear.png',
    './assets/cloudy.png',
    './assets/humidity.png',
    './assets/rain.png',
    './assets/search.png',
    './assets/snow.png',
    './assets/windspeed.png'
];

// Install: Cache all static UI assets
self.addEventListener('install', (event) => {
    event.waitUntil(
        caches.open(CACHE_NAME).then((cache) => {
            return cache.addAll(STATIC_ASSETS);
        })
    );
    self.skipWaiting();
});

// Activate: Clean up old caches
self.addEventListener('activate', (event) => {
    event.waitUntil(
        caches.keys().then((keys) => {
            return Promise.all(
                keys.map((key) => {
                    if (key !== CACHE_NAME) {
                        return caches.delete(key);
                    }
                })
            );
        })
    );
    self.clients.claim();
});

// Fetch: Network first, fallback to cache for offline resilience
self.addEventListener('fetch', (event) => {
    const req = event.request;

    // For Open-Meteo API requests: try network, fallback to last cached data
    if (req.url.includes('open-meteo.com')) {
        event.respondWith(
            fetch(req)
                .then((networkRes) => {
                    const cloned = networkRes.clone();
                    caches.open('weather-data-cache').then((cache) => cache.put(req, cloned));
                    return networkRes;
                })
                .catch(() => caches.match(req))
        );
        return;
    }

    // For static assets: cache first, then network
    event.respondWith(
        caches.match(req).then((cached) => {
            return cached || fetch(req).then((res) => {
                const cloned = res.clone();
                caches.open(CACHE_NAME).then((cache) => cache.put(req, cloned));
                return res;
            });
        }).catch(() => {
            if (req.mode === 'navigate') {
                return caches.match('./index.html');
            }
        })
    );
});
