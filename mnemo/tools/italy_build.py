"""Сборка www/italy.js — карта Италии и города «Путешествия в Рим».

Вход (не хранится в репозитории):
  geo/src  — клон https://github.com/openpolis/geojson-italy (нужны geojson/limits_IT_regions.geojson
             и topojson/limits_IT_municipalities.topo.json — из второго берём координаты коммун);
  cities/tier-1..4.json — города по возрастанию известности, последний — Roma.
Запуск: python3 italy_build.py <папка с geo/ и cities/>
"""
import json, math, sys, unicodedata, pathlib

BASE = pathlib.Path(sys.argv[1])
OUT = pathlib.Path(__file__).resolve().parent.parent / 'app/src/main/assets/www/italy.js'
K = 40; LON0 = 6.6; LAT0 = 47.15; C = math.cos(math.radians(42))
P = lambda lon, lat: ((lon - LON0) * C * K, (LAT0 - lat) * K)

def area_centroid(r):
    A = cx = cy = 0
    for (x0, y0), (x1, y1) in zip(r, r[1:] + r[:1]):
        f = x0 * y1 - x1 * y0; A += f; cx += (x0 + x1) * f; cy += (y0 + y1) * f
    if abs(A) < 1e-12: return 0, r[0][0], r[0][1]
    return abs(A) / 2, cx / (3 * A), cy / (3 * A)

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

# Регионы → упрощённые контуры SVG
regions = []
g = json.load(open(BASE / 'geo/src/geojson/limits_IT_regions.geojson'))
for f in g['features']:
    geom = f['geometry']
    polys = geom['coordinates'] if geom['type'] == 'MultiPolygon' else [geom['coordinates']]
    d = ''
    for poly in polys:
        r = [P(*c) for c in poly[0]]
        if area_centroid(r)[0] < 6: continue  # мелкие острова
        m = len(r) // 2
        s = dp(r[:m + 1], 0.35)[:-1] + dp(r[m:], 0.35)
        if len(s) >= 4: d += 'M' + 'L'.join(f'{x:.1f},{y:.1f}' for x, y in s) + 'Z'
    regions.append([f['properties']['reg_name'].split('/')[0], d])

# Коммуны: центр самой большой части территории
t = json.load(open(BASE / 'geo/src/topojson/limits_IT_municipalities.topo.json'))
sx, sy = t['transform']['scale']; tx, ty = t['transform']['translate']
arcs = []
for a in t['arcs']:
    x = y = 0; pts = []
    for ddx, ddy in a: x += ddx; y += ddy; pts.append((x * sx + tx, y * sy + ty))
    arcs.append(pts)
def ring(idx):
    out = []
    for i in idx:
        pts = arcs[i] if i >= 0 else arcs[~i][::-1]
        out.extend(pts if not out else pts[1:])
    return out
key = lambda s: unicodedata.normalize('NFC', s).lower().strip()
comuni = {}
for geo in t['objects']['comuni']['geometries']:
    polys = geo['arcs'] if geo['type'] == 'MultiPolygon' else [geo['arcs']]
    a, lon, lat = max((area_centroid(ring(p[0])) for p in polys), key=lambda v: v[0])
    pr = geo['properties']
    comuni[(key(pr['name']), pr['prov_acr'])] = (lon, lat, pr['reg_name'].split('/')[0])

# Если город попал в несколько списков, оставляем самую дальнюю (более «знаменитую») позицию.
raw = []
for name in ['tier-1', 'tier-1b', 'tier-2', 'tier-2b', 'tier-2c', 'tier-3', 'tier-4']:
    f = BASE / f'cities/{name}.json'
    if f.exists(): raw += [c for c in json.load(open(f)) if not (c['n'] == 'Roma' and name != 'tier-4')]
last = {}
for i, c in enumerate(raw): last[(key(c['n']), c['p'])] = i
cities = []
for i, c in enumerate(raw):
        k = (key(c['n']), c['p'])
        if last[k] != i or k not in comuni: continue
        lon, lat, reg = comuni[k]
        x, y = P(lon, lat)
        item = {'n': c.get('t') or c['n'], 'r': reg, 'x': round(x, 1), 'y': round(y, 1), 'w': c['w'], 'it': c['it'], 'ru': c['ru']}
        if c.get('photo'): item['p'] = c['photo']  # статья для фото (в данных Италии 'p' — это провинция)
        cities.append(item)
# Рим — всегда в конце
cities.sort(key=lambda c: c['n'] == 'Roma')
W = round((18.6 - LON0) * C * K); H = round((LAT0 - 36.4) * K)
data = {'w': W, 'h': H, 'regions': regions, 'cities': cities}
OUT.write_text('/* Сгенерировано mnemo/tools/italy_build.py — карта Италии и города «Путешествия в Рим». */\n'
               'window.MNEMO_ITALY = ' + json.dumps(data, ensure_ascii=False, separators=(',', ':')) + ';\n', encoding='utf-8')
print(f'{len(cities)} городов, {OUT.stat().st_size // 1024} КБ')
