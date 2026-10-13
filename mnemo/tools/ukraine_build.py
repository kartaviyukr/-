"""Сборка www/ukraine.js — карта Украины и города «Дороги в Киев» (украинский курс).

Вход (не хранится в репозитории):
  uageo/ukraine_geojson/UA_FULL_Ukraine.geojson — области (репозиторий EugeneBorshch/ukraine_geojson;
     в нём у некоторых областей кольца перепутаны, поэтому рисуем все кольца);
  uacities/tier-1..4.json — города по возрастанию известности (с координатами), последний — Київ.
Запуск: python3 ukraine_build.py <папка с uageo/ и uacities/>
"""
import json, math, sys, pathlib

BASE = pathlib.Path(sys.argv[1])
OUT = pathlib.Path(__file__).resolve().parent.parent / 'app/src/main/assets/www/ukraine.js'
K = 40; LON0 = 21.9; LAT0 = 52.55; C = math.cos(math.radians(48.5))
P = lambda lon, lat: ((lon - LON0) * C * K, (LAT0 - lat) * K)

def area(r):
    return abs(sum(x0 * y1 - x1 * y0 for (x0, y0), (x1, y1) in zip(r, r[1:] + r[:1]))) / 2
def dp(pts, eps):
    if len(pts) < 3: return pts
    (x0, y0), (x1, y1) = pts[0], pts[-1]
    dx, dy = x1 - x0, y1 - y0; L = math.hypot(dx, dy) or 1e-9
    dmax, idx = 0, 0
    for i in range(1, len(pts) - 1):
        d = abs(dy * pts[i][0] - dx * pts[i][1] + x1 * y0 - y1 * x0) / L
        if d > dmax: dmax, idx = d, i
    if dmax > eps: return dp(pts[:idx + 1], eps)[:-1] + dp(pts[idx:], eps)
    return [pts[0], pts[-1]]

regions = []
g = json.load(open(BASE / 'uageo/ukraine_geojson/UA_FULL_Ukraine.geojson'))
for f in g['features']:
    name = f['properties'].get('name:uk') or f['properties']['name']
    geom = f['geometry']
    polys = geom['coordinates'] if geom['type'] == 'MultiPolygon' else [geom['coordinates']]
    d = ''
    for poly in polys:
        for ring in poly:
            r = [P(*c[:2]) for c in ring]
            if area(r) < 4: continue
            m = len(r) // 2
            s = dp(r[:m + 1], 0.3)[:-1] + dp(r[m:], 0.3)
            if len(s) >= 4: d += 'M' + 'L'.join(f'{x:.1f},{y:.1f}' for x, y in s) + 'Z'
    regions.append([name, d])

raw = []
for name in ['tier-1', 'tier-1b', 'tier-2', 'tier-2b', 'tier-3', 'tier-4']:
    f = BASE / f'uacities/{name}.json'
    if f.exists(): raw += [c for c in json.load(open(f)) if not (c['n'] == 'Київ' and name != 'tier-4')]
key = lambda c: c['n'].lower()  # один город — одна открытка
last = {}
for i, c in enumerate(raw): last[key(c)] = i
cities = []
for i, c in enumerate(raw):
    if last[key(c)] != i: continue
    x, y = P(float(c['lon']), float(c['lat']))
    o = c['o'].strip()
    reg = 'Крим' if 'Крим' in o else o if o.endswith('область') else o + ' область'
    cities.append({'n': c.get('t') or c['n'], 'r': reg, 'x': round(x, 1), 'y': round(y, 1), 'w': c['w'], 'uk': c['uk'], 'ru': c['ru']})
    if c.get('photo'): cities[-1]['p'] = c['photo']  # статья для фото
cities.sort(key=lambda c: c['n'] == 'Київ')
W = round((40.4 - LON0) * C * K); H = round((LAT0 - 44.2) * K)
data = {'w': W, 'h': H, 'regions': regions, 'cities': cities}
OUT.write_text('/* Сгенерировано mnemo/tools/ukraine_build.py — карта Украины и города «Дороги в Киев». */\n'
               'window.MNEMO_UKRAINE = ' + json.dumps(data, ensure_ascii=False, separators=(',', ':')) + ';\n', encoding='utf-8')
print(f'{len(cities)} городов, {OUT.stat().st_size // 1024} КБ, {W}x{H}')
