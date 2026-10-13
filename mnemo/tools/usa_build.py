"""Сборка www/usa.js — карта США и города «Дороги в Вашингтон» (английский курс).

Вход (не хранится в репозитории):
  usgeo/folium/examples/data/us-states.json — контуры штатов (репозиторий python-visualization/folium);
  usgeo/US-Cities-Database/csv/us_cities.csv — координаты городов (репозиторий kelvins/US-Cities-Database);
  uscities/tier-1..4.json — города по возрастанию известности, последний — Washington, DC.
Проекция — как d3.geoAlbersUsa: материковая часть + вставки Аляски и Гавайев.
Запуск: python3 usa_build.py <папка с usgeo/ и uscities/>
"""
import json, math, sys, csv, unicodedata, pathlib

BASE = pathlib.Path(sys.argv[1])
OUT = pathlib.Path(__file__).resolve().parent.parent / 'app/src/main/assets/www/usa.js'
R = math.radians

def conic(rot, center, par, scale, tr):
    p1, p2 = R(par[0]), R(par[1])
    n = (math.sin(p1) + math.sin(p2)) / 2
    C = math.cos(p1) ** 2 + 2 * n * math.sin(p1)
    def raw(lon, lat):
        lam = R(((lon + rot + 180) % 360) - 180)
        rho = math.sqrt(max(0, C - 2 * n * math.sin(R(lat)))) / n
        return rho * math.sin(n * lam), -rho * math.cos(n * lam)
    cx, cy = raw(center[0] - rot, center[1])
    def f(lon, lat):
        x, y = raw(lon, lat)
        return tr[0] + scale * (x - cx), tr[1] - scale * (y - cy)
    return f

K = 1070; TX, TY = 480, 250
# центры заданы в повёрнутых координатах, как в d3.geoAlbersUsa
lower = conic(96, (-0.6, 38.7), (29.5, 45.5), K, (TX, TY))
alaska = conic(154, (-2, 58.5), (55, 65), 0.35 * K, (TX - 0.307 * K, TY + 0.201 * K))
hawaii = conic(157, (-3, 19.9), (8, 18), K, (TX - 0.205 * K, TY + 0.212 * K))
def P(lon, lat, st=None):
    if st == 'AK' or (st is None and lat > 50 and lon < -129): return alaska(lon, lat)
    if st == 'HI' or (st is None and lat < 23 and lon < -150): return hawaii(lon, lat)
    return lower(lon, lat)

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

ABBR = {}
regions = []
g = json.load(open(BASE / 'usgeo/folium/examples/data/us-states.json'))
for f in g['features']:
    st, name = f['id'], f['properties']['name']
    ABBR[st] = name
    geom = f['geometry']
    polys = geom['coordinates'] if geom['type'] == 'MultiPolygon' else [geom['coordinates']]
    d = ''
    for poly in polys:
        r = [P(lon, lat, st) for lon, lat in poly[0]]
        if area(r) < 3: continue
        m = len(r) // 2
        s = dp(r[:m + 1], 0.6)[:-1] + dp(r[m:], 0.6)
        if len(s) >= 4: d += 'M' + 'L'.join(f'{x:.1f},{y:.1f}' for x, y in s) + 'Z'
    regions.append([name, d])
ABBR['DC'] = 'District of Columbia'

key = lambda s: unicodedata.normalize('NFC', s).lower().replace('saint ', 'st. ').replace('st ', 'st. ').strip()
db = {}
for r in csv.DictReader(open(BASE / 'usgeo/US-Cities-Database/csv/us_cities.csv')):
    db.setdefault((key(r['CITY']), r['STATE_CODE']), (float(r['LONGITUDE']), float(r['LATITUDE'])))

raw = []
for name in ['tier-1', 'tier-1b', 'tier-2', 'tier-2b', 'tier-3', 'tier-4']:
    f = BASE / f'uscities/{name}.json'
    if f.exists(): raw += [c for c in json.load(open(f)) if not (c['s'] == 'DC' and name != 'tier-4')]
last = {}
for i, c in enumerate(raw): last[(key(c['n']), c['s'])] = i
cities = []
for i, c in enumerate(raw):
    k = (key(c['n']), c['s'])
    if last[k] != i or k not in db: continue
    x, y = P(*db[k], c['s'])
    cities.append({'n': c.get('t') or c['n'], 'r': ABBR.get(c['s'], c['s']), 'x': round(x, 1), 'y': round(y, 1), 'w': c['w'], 'en': c['en'], 'ru': c['ru']})
    if c.get('photo'): cities[-1]['p'] = c['photo']  # статья для фото
cities.sort(key=lambda c: c['r'] == 'District of Columbia')

xs = [float(v) for _, d in regions for pt in d.replace('M', 'L').replace('Z', '').split('L') if pt for v in pt.split(',')[:1]]
ys = [float(pt.split(',')[1]) for _, d in regions for pt in d.replace('M', 'L').replace('Z', '').split('L') if pt]
x0, y0 = min(xs) - 6, min(ys) - 6
def shift(d): return 'M' + 'L'.join(f'{float(a) - x0:.1f},{float(b) - y0:.1f}' for a, b in (p.split(',') for p in d.replace('M', 'L').replace('Z', '').split('L') if p)) + 'Z'
# пересобираем контуры со сдвигом (каждый полигон отдельно)
regions = [[n, ''.join(shift('M' + part) for part in d.split('M') if part)] for n, d in regions]
for c in cities: c['x'] = round(c['x'] - x0, 1); c['y'] = round(c['y'] - y0, 1)
data = {'w': round(max(xs) - x0 + 6), 'h': round(max(ys) - y0 + 6), 'regions': regions, 'cities': cities}
OUT.write_text('/* Сгенерировано mnemo/tools/usa_build.py — карта США и города «Дороги в Вашингтон». */\n'
               'window.MNEMO_USA = ' + json.dumps(data, ensure_ascii=False, separators=(',', ':')) + ';\n', encoding='utf-8')
print(f'{len(cities)} городов, {OUT.stat().st_size // 1024} КБ, {data["w"]}x{data["h"]}')
