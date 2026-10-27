#!/usr/bin/env python3
"""Проверка контент-пака: схема, объёмы текста, источники, ссылки между файлами.

Запуск: python3 content/tools/validate_content.py   (код выхода 1 при ошибках)
Ошибки ломают сборку, предупреждения — только печатаются.
"""
import json
import math
import os
import re
import sys

HERE = os.path.dirname(os.path.abspath(__file__))
PACK = os.path.join(os.path.dirname(HERE), "pack")

SETTLEMENT = {"округ Москвы", "город", "рабочий посёлок", "дачный посёлок", "посёлок", "село", "деревня", "усадьба", "монастырь",
              "природа", "памятное место"}
STOP_KIND = {"смотреть снаружи", "заходим внутрь", "проезжаем"}
DISH_KIND = {"блюдо", "продукт", "тематическая замена"}
DIFFICULTY = {"лёгкий", "средний", "сложный"}
SHAPE = {"линейный", "петля"}
REGION_TYPE = {"МО_городской_округ", "МО_муниципальный_округ", "Москва_АО", "соседняя_область"}
DAYS = {"mon", "tue", "wed", "thu", "fri", "sat", "sun"}
HOURS = re.compile(r"^\d{2}:\d{2}-\d{2}:\d{2}(,\d{2}:\d{2}-\d{2}:\d{2})*$")
URL = re.compile(r"^https?://\S+$")
# Москва, область и соседние города спирали с запасом.
LAT, LON = (53.9, 58.3), (34.8, 41.0)
# Единицы — только метрические.
IMPERIAL = re.compile(r"\b(миль|мили|миля|милях|футов|фута|фут|дюйм\w*|ярдов|ярда|ярд|фунтов|фунта|фунт|унци\w*|галлон\w*)\b")

errors, warnings = [], []


LONG_ROUTES = []


def err(where, msg):
    errors.append(f"{where}: {msg}")


def warn(where, msg):
    warnings.append(f"{where}: {msg}")


def need(obj, key, typ, where, optional=False):
    if key not in obj or obj[key] is None:
        if not optional:
            err(where, f"нет поля «{key}»")
        return None
    v = obj[key]
    if typ is float:
        ok = isinstance(v, (int, float)) and not isinstance(v, bool)
    else:
        ok = isinstance(v, typ)
    if not ok:
        err(where, f"поле «{key}» должно быть {typ.__name__}")
        return None
    if isinstance(v, str) and not v.strip() and not optional:
        err(where, f"поле «{key}» пустое")
    return v


def point(obj, key, where, optional=False):
    p = need(obj, key, dict, where, optional)
    if p is None:
        return None
    lat, lon = need(p, "lat", float, f"{where}.{key}"), need(p, "lon", float, f"{where}.{key}")
    if lat is None or lon is None:
        return None
    if not (LAT[0] <= lat <= LAT[1] and LON[0] <= lon <= LON[1]):
        err(where, f"{key} вне Москвы и области: {lat}, {lon}")
    return lat, lon


def dist_km(a, b):
    p1, p2 = math.radians(a[0]), math.radians(b[0])
    dp, dl = p2 - p1, math.radians(b[1] - a[1])
    h = math.sin(dp / 2) ** 2 + math.cos(p1) * math.cos(p2) * math.sin(dl / 2) ** 2
    return 2 * 6371.0088 * math.asin(math.sqrt(h))


def paragraphs(obj, key, where, lo, hi, min_chars=300):
    v = need(obj, key, list, where)
    if v is None:
        return
    if not (lo <= len(v) <= hi):
        err(where, f"«{key}»: {len(v)} абзацев, нужно {lo}–{hi}")
    for i, p in enumerate(v):
        if not isinstance(p, str) or len(p.strip()) < 40:
            err(where, f"«{key}»[{i}] слишком короткий абзац")
        elif IMPERIAL.search(p):
            err(where, f"«{key}»[{i}] — неметрические единицы")
    total = sum(len(p) for p in v if isinstance(p, str))
    if total < min_chars:
        err(where, f"«{key}»: всего {total} знаков, нужно не меньше {min_chars}")


def polygon(obj, key, where):
    poly = obj.get(key)
    if poly is None:
        return
    if poly.get("type") != "Polygon" or not poly.get("coordinates"):
        err(where, f"{key}: ожидается GeoJSON Polygon")
        return
    ring = poly["coordinates"][0]
    if len(ring) < 4 or ring[0] != ring[-1]:
        err(where, f"{key}: внешнее кольцо должно быть замкнуто и иметь ≥ 3 вершин")


CENTERS = {}  # id места → (центр, имя), заполняется из spiral.json
KREMLIN_PT = (55.7520, 37.6175)
OKRUGS = {"tsao", "sao", "svao", "vao", "yuvao", "yuao", "yuzao", "zao", "szao"}


def foreign_place(did, pt):
    """Имя другого места спирали, к которому точка явно ближе, чем к своему (допуск 4 км)."""
    okr = did in OKRUGS
    inside = min(CENTERS, key=lambda k: dist_km(pt, CENTERS[k][0])) in OKRUGS
    if okr != inside:
        return "внутри МКАД" if inside else "за МКАД"
    own = dist_km(pt, CENTERS[did][0])
    best = min(((dist_km(pt, c), n) for k, (c, n) in CENTERS.items() if k != did and (k in OKRUGS) == okr), default=None)
    if best and own > best[0] + 4.0:
        return best[1]
    return None


def check_destination(d, fname, spiral_ids):
    w = fname
    did = need(d, "id", str, w)
    if did and did + ".json" != fname:
        err(w, f"id «{did}» не совпадает с именем файла")
    need(d, "name", str, w)
    if d.get("settlementType") not in SETTLEMENT:
        err(w, f"settlementType «{d.get('settlementType')}» не из списка")
    if d.get("mustHave") not in (1, 2, 3):
        err(w, "mustHave должно быть 1, 2 или 3")
    need(d, "regionId", str, w)
    need(d, "distanceFromKremlinKm", float, w)
    center = point(d, "coords", w)
    polygon(d, "boundaryGeoJson", w)
    if d.get("boundarySource") not in ("osm", "approx"):
        err(w, "boundarySource: osm или approx")
    paragraphs(d, "intro", w, 3, 6)
    # Версия 3 — маршрут только по своему месту и окрестностям, 6–12 точек, длина — сколько позволяет место.
    # Версия 2 (40–70 км с заездами к соседям) пока допускается с предупреждением; версия 1 — ошибка.
    ver = d.get("routeVersion")
    v3 = ver == 3
    v2 = ver in (2, 3)
    if v3:
        LONG_ROUTES.append(did)
    elif ver == 2:
        warn(w, "маршрут версии 2 заезжает к соседям — переписать на версию 3 (только свои точки)")
    else:
        err(w, "старый короткий маршрут (routeVersion 1)")
    start = end = None
    for key in ("startPoint", "endPoint"):
        rp = need(d, key, dict, w)
        if rp:
            need(rp, "name", str, f"{w}.{key}")
            need(rp, "transport", str, f"{w}.{key}")
            pt = point(rp, "coords", f"{w}.{key}")
            if key == "startPoint":
                start = pt
            else:
                end = pt
    route = need(d, "route", dict, w)
    if route:
        rw = f"{w}.route"
        need(route, "distanceKm", float, rw)
        km = route.get("distanceKm")
        if ver == 2 and isinstance(km, (int, float)) and not (40 <= km <= 70):
            err(rw, f"distanceKm {km}: маршрут версии 2 должен быть 40–70 км")
        need(route, "durationH", float, rw)
        if route.get("difficulty") not in DIFFICULTY:
            err(rw, "difficulty: лёгкий/средний/сложный")
        if route.get("shape", "линейный") not in SHAPE:
            err(rw, "shape: линейный/петля")
        for k in ("surfaceNotes", "seasonNotes", "safetyNotes"):
            need(route, k, str, rw)
        for b in route.get("barriers", []):
            if len(b.get("line", [])) < 2:
                err(rw, f"барьер «{b.get('name')}» — нужна линия из ≥ 2 точек")
    stops = need(d, "stops", list, w) or []
    if v3 and not (4 <= len(stops) <= 12):
        err(w, f"{len(stops)} точек, нужно 4–12 (цель — 6–8)")
    elif v3 and len(stops) < 6:
        warn(w, f"всего {len(stops)} точки — цель 6–8, нужно найти ещё")
    elif ver == 2 and not (8 <= len(stops) <= 12):
        err(w, f"{len(stops)} точек, нужно 8–12")
    elif len(stops) < 3:
        err(w, "меньше 3 точек")
    orders = [s.get("order") for s in stops]
    if orders != sorted(orders) or len(set(orders)) != len(orders):
        err(w, "order точек должен строго возрастать")
    seen = set()
    for s in stops:
        sw = f"{w}/{s.get('id')}"
        sid = need(s, "id", str, sw)
        if sid in seen:
            err(sw, "повтор id точки")
        seen.add(sid)
        if sid and did and not sid.startswith(did + "-"):
            err(sw, f"id точки должен начинаться с «{did}-»")
        need(s, "name", str, sw)
        pt = point(s, "coords", sw)
        if pt and center and dist_km(pt, center) > (30 if v2 else 15):
            warn(sw, f"точка в {dist_km(pt, center):.1f} км от центра места")
        if v3 and pt and did in CENTERS:
            other = foreign_place(did, pt)
            if other:
                err(sw, f"точка «{s.get('name')}» ближе к месту «{other}», чем к своему — маршрут не должен заезжать к соседям")
        if s.get("kind") not in STOP_KIND:
            err(sw, f"kind «{s.get('kind')}» не из списка")
        need(s, "visitMinutes", int, sw)
        if s.get("coordsVerified") is not True:
            warn(sw, "координаты не сверены по OSM")
        r = s.get("zoneRadiusM")
        if r is not None and not (30 <= r <= 5000):
            err(sw, "zoneRadiusM вне 30–5000 м")
        polygon(s, "zonePolygon", sw)
        sch = s.get("schedule")
        if sch is not None:
            for day, hours in sch.get("days", {}).items():
                if day not in DAYS:
                    err(sw, f"schedule: неизвестный день {day}")
                if hours != "closed" and not HOURS.match(hours):
                    err(sw, f"schedule.{day}: «{hours}» — нужен формат 10:00-18:00 или closed")
        if v2:
            paragraphs(s, "guideStory", sw, 2, 6, min_chars=200)
        else:
            paragraphs(s, "guideStory", sw, 3, 6)
        facts = need(s, "facts", list, sw) or []
        lo_f = 2 if v2 else 5
        if not (lo_f <= len(facts) <= 12):
            err(sw, f"{len(facts)} фактов, нужно {lo_f}–12")
        for i, f in enumerate(facts):
            fw = f"{sw}.facts[{i}]"
            need(f, "text", str, fw)
            url = need(f, "sourceUrl", str, fw)
            if url and not URL.match(url):
                err(fw, "sourceUrl — не ссылка")
            if not isinstance(f.get("verified"), bool):
                err(fw, "verified должно быть true/false")
            if IMPERIAL.search(f.get("text", "")):
                err(fw, "неметрические единицы")
        unverified = sum(1 for f in facts if f.get("verified") is False)
        if facts and unverified * 2 > len(facts):
            warn(sw, f"больше половины фактов не проверены ({unverified}/{len(facts)})")
    if start and stops and stops[0].get("coords"):
        first = (stops[0]["coords"]["lat"], stops[0]["coords"]["lon"])
        if dist_km(start, first) > (15 if v2 else 5):
            warn(w, "старт дальше 5 км от первой точки")
    dish = need(d, "dish", dict, w)
    if dish:
        dw = f"{w}.dish"
        need(dish, "name", str, dw)
        if dish.get("kind") not in DISH_KIND:
            err(dw, "kind: блюдо/продукт/тематическая замена")
        if dish.get("kind") == "тематическая замена" and not dish.get("honestyNote"):
            err(dw, "для тематической замены нужна honestyNote")
        paragraphs(dish, "story", dw, 1, 6, min_chars=120)
        for i, shop in enumerate(need(dish, "whereToBuy", list, dw) or []):
            sw = f"{dw}.whereToBuy[{i}]"
            need(shop, "name", str, sw)
            need(shop, "address", str, sw)
            point(shop, "coords", sw, optional=True)
            if not isinstance(shop.get("verified"), bool):
                err(sw, "verified должно быть true/false")
    title = need(d, "title", dict, w)
    if title:
        for k in ("id", "name", "legend", "ritual", "pairVariant"):
            need(title, k, str, f"{w}.title")
    sources = need(d, "sources", list, w) or []
    if not sources:
        err(w, "нет источников")
    for s in sources:
        if not URL.match(s.get("url", "")):
            err(w, f"источник «{s.get('title')}» без ссылки")
    if did and did not in spiral_ids:
        err(w, "место не упомянуто в spiral.json (destinationId)")


def main():
    manifest = json.load(open(os.path.join(PACK, "manifest.json"), encoding="utf-8"))
    for k in ("version", "updated", "spiral", "destinations"):
        if k not in manifest:
            err("manifest.json", f"нет поля «{k}»")
    spiral = json.load(open(os.path.join(PACK, manifest["spiral"]), encoding="utf-8"))
    region_ids = {r["id"] for r in spiral["regions"]}
    for r in spiral["regions"]:
        if r["type"] not in REGION_TYPE:
            err("spiral.json", f"округ {r['name']}: тип {r['type']}")
    linked = set()
    nums = []
    for p in spiral["places"]:
        CENTERS[p["id"]] = ((p["coords"]["lat"], p["coords"]["lon"]), p["name"])
        nums.append(p["num"])
        if p["regionId"] not in region_ids:
            err("spiral.json", f"{p['name']}: неизвестный округ {p['regionId']}")
        if p.get("destinationId"):
            linked.add(p["destinationId"])
    if nums != list(range(1, len(nums) + 1)):
        err("spiral.json", "номера мест должны идти подряд с 1")
    if not spiral["places"][0].get("start"):
        err("spiral.json", "первым должен быть старт спирали (ЦАО)")
    inside = [p for p in spiral["places"] if p.get("insideMkad")]
    if [p["num"] for p in inside] != list(range(1, len(inside) + 1)):
        err("spiral.json", "округа Москвы внутри МКАД должны идти первыми")
    dest_ids = [p["destinationId"] for p in spiral["places"] if p.get("destinationId")]
    if len(dest_ids) != len(set(dest_ids)):
        err("spiral.json", "у каждого места должен быть свой маршрут — один файл не может покрывать несколько мест")
    files = manifest["destinations"]
    for f in files:
        path = os.path.join(PACK, "destinations", f)
        if not os.path.exists(path):
            err("manifest.json", f"нет файла {f}")
            continue
        check_destination(json.load(open(path, encoding="utf-8")), f, linked)
    for did in linked:
        if did + ".json" not in files:
            err("spiral.json", f"destinationId {did} без файла в манифесте")
    for wmsg in warnings:
        print("ПРЕДУПРЕЖДЕНИЕ", wmsg)
    for e in errors:
        print("ОШИБКА", e)
    print(f"Мест с контентом: {len(files)}; маршрутов по своим точкам (версия 3): {len(LONG_ROUTES)}; "
          f"ошибок: {len(errors)}; предупреждений: {len(warnings)}")
    sys.exit(1 if errors else 0)


if __name__ == "__main__":
    main()
