#!/usr/bin/env python3
"""Пересчёт большого маршрута (спирали от Кремля наружу) из таблицы мест.

Вход: content/tools/places.tsv (номер, отметка, название, тип, округ, км, азимут, lat, lon).
Расстояние и азимут пересчитываются по координатам, кольца и порядок строятся тем же
алгоритмом, что в приложении (SpiralBuilder.kt). Если после уточнения координат место
сменило кольцо — это нормально, порядок перегенерируется.

Выход: content/pack/spiral.json
"""
import json
import math
import os
import sys

HERE = os.path.dirname(os.path.abspath(__file__))
ROOT = os.path.dirname(HERE)
KREMLIN = (55.752, 37.618)
# Кольцо 0 — Москва внутри МКАД (административные округа), дальше — по расстоянию от Кремля.
LOWER = [0, 20, 40, 60, 80, 100, 120, 150, 200]
RING_NAMES = ["Москва внутри МКАД", "ближе 20 км", "20–40 км", "40–60 км", "60–80 км", "80–100 км", "100–120 км", "120–150 км", "150–200 км", "дальше 200 км"]
# Спираль стартует с ЦАО — округа, где стоит Кремль.
START = "ЦАО"

SUPERLATIVES = [
    ("biggest-city", "Самый большой город — Балашиха", "Балашиха", "Крупнейший город Подмосковья по населению"),
    ("oldest-city", "Самый старый город — Волоколамск (1135)", "Волоколамск", "Первое упоминание в летописи — 1135 год"),
    ("smallest-city", "Самый маленький город — Верея", "Верея", "Самый малонаселённый город области"),
    ("biggest-rp", "Самый большой рабочий посёлок — Нахабино", "Нахабино", ""),
    ("biggest-village", "Самая большая деревня — Путилково", "Путилково", ""),
    ("famous-dacha", "Самый известный дачный посёлок — Переделкино", "Переделкино", ""),
    ("biggest-selo", "Самое большое село — уточняется", None, "Проверить при наполнении контента"),
]

TRANSLIT = dict(zip("абвгдеёжзийклмнопрстуфхцчшщъыьэюя",
                    ["a", "b", "v", "g", "d", "e", "e", "zh", "z", "i", "y", "k", "l", "m", "n", "o", "p", "r", "s", "t",
                     "u", "f", "kh", "ts", "ch", "sh", "shch", "", "y", "", "e", "yu", "ya"]))


def slug(name):
    out = []
    for ch in name.lower():
        if ch in TRANSLIT:
            out.append(TRANSLIT[ch])
        elif ch.isascii() and ch.isalnum():
            out.append(ch)
        else:
            out.append("-")
    s = "".join(out)
    while "--" in s:
        s = s.replace("--", "-")
    return s.strip("-")


def hav(a, b):
    r = 6371.0088
    p1, p2 = math.radians(a[0]), math.radians(b[0])
    dp, dl = p2 - p1, math.radians(b[1] - a[1])
    h = math.sin(dp / 2) ** 2 + math.cos(p1) * math.cos(p2) * math.sin(dl / 2) ** 2
    return 2 * r * math.asin(math.sqrt(h))


def bearing(a, b):
    p1, p2 = math.radians(a[0]), math.radians(b[0])
    dl = math.radians(b[1] - a[1])
    y = math.sin(dl) * math.cos(p2)
    x = math.cos(p1) * math.sin(p2) - math.sin(p1) * math.cos(p2) * math.cos(dl)
    return (math.degrees(math.atan2(y, x)) + 360) % 360


def ring_of(d, inside_mkad=False):
    if inside_mkad:
        return 0
    return 1 + max(i for i, b in enumerate(LOWER) if d >= b)


def region_type(name):
    if name.startswith("Москва"):
        return "Москва_АО"
    if name.endswith("область"):
        return "соседняя_область"
    return "МО_городской_округ"


def main():
    rows = [l.rstrip("\n").split("\t") for l in open(os.path.join(HERE, "places.tsv"), encoding="utf-8") if l.strip()]
    # Маршрут привязывается к месту, если в паке есть файл destinations/<id места>.json.
    dest_dir = os.path.join(ROOT, "pack", "destinations")
    ready = {f[:-5] for f in os.listdir(dest_dir) if f.endswith(".json")}
    places, regions = [], {}
    for num, mark, name, kind, region, *_rest in rows:
        lat, lon = float(_rest[2]), float(_rest[3])
        rid = slug(region)
        regions.setdefault(rid, {"id": rid, "name": region, "type": region_type(region), "verified": False})
        start = name == START
        inside_mkad = kind.startswith("административный округ Москвы")
        d = 0.0 if start else hav(KREMLIN, (lat, lon))
        places.append({
            "id": slug(name),
            "num": int(num),
            "name": name,
            "kindNote": kind,
            "regionId": rid,
            "mustHave": 3 if mark == "★" else 2 if mark == "☆" else 1,
            "coords": {"lat": lat, "lon": lon},
            "distanceFromKremlinKm": round(d, 1),
            "azimuthDeg": 0.0 if start else round(bearing(KREMLIN, (lat, lon)), 1),
            "ring": ring_of(d, inside_mkad),
            "destinationId": slug(name) if slug(name) in ready else None,
            "start": start,
            "insideMkad": inside_mkad,
            # Точные значения — для сортировки (в JSON идут округлённые).
            "_d": d,
            "_az": 0.0 if start else bearing(KREMLIN, (lat, lon)),
        })
    # Порядок спирали: старт, затем кольца изнутри наружу, внутри кольца — по часовой стрелке.
    ordered = [p for p in places if p["start"]]
    route = [p for p in places if not p["start"]]
    from_az = None
    for ring in range(len(RING_NAMES)):
        inside = [p for p in route if p["ring"] == ring]
        if not inside:
            continue
        if from_az is None:
            from_az = min(inside, key=lambda p: p["_d"])["_az"]
        inside.sort(key=lambda p: ((p["_az"] - from_az) % 360, p["_d"], p["num"]))
        ordered += inside
        from_az = inside[-1]["_az"]
    for i, p in enumerate(ordered):
        p["num"] = i + 1
        del p["_d"], p["_az"]
    ids = [p["id"] for p in ordered]
    assert len(ids) == len(set(ids)), "дубли id"
    assert ordered and ordered[0]["start"], "нет старта " + START
    by_name = {p["name"]: p["id"] for p in places}
    pack = {
        "version": 2,
        "origin": {"lat": KREMLIN[0], "lon": KREMLIN[1]},
        "rings": [{"index": 0, "name": RING_NAMES[0], "minKm": 0.0, "maxKm": None}] +
                 [{"index": i + 1, "name": RING_NAMES[i + 1], "minKm": float(LOWER[i]),
                   "maxKm": float(LOWER[i + 1]) if i + 1 < len(LOWER) else None} for i in range(len(LOWER))],
        "regions": sorted(regions.values(), key=lambda r: r["name"]),
        "places": ordered,
        "superlatives": [{"id": i, "title": t, "placeId": by_name.get(n) if n else None, "note": note}
                         for i, t, n, note in SUPERLATIVES],
    }
    # Манифест: все маршруты в порядке спирали.
    manifest_path = os.path.join(ROOT, "pack", "manifest.json")
    manifest = json.load(open(manifest_path, encoding="utf-8"))
    manifest["destinations"] = [p["destinationId"] + ".json" for p in ordered if p["destinationId"]]
    with open(manifest_path, "w", encoding="utf-8") as f:
        json.dump(manifest, f, ensure_ascii=False, indent=2)
        f.write("\n")
    with open(os.path.join(ROOT, "pack", "spiral.json"), "w", encoding="utf-8") as f:
        json.dump(pack, f, ensure_ascii=False, indent=1)
        f.write("\n")
    print(f"spiral.json: {len(ordered)} мест, {len(regions)} округов; старт — {ordered[0]['name']}, финиш — {ordered[-1]['name']}")


if __name__ == "__main__":
    main()
