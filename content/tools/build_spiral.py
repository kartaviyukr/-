#!/usr/bin/env python3
"""Пересчёт большого маршрута (спирали к Кремлю) из таблицы мест.

Вход: content/tools/appendix_a.tsv (номер, отметка, название, тип, округ, км, азимут, lat, lon).
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
BOUNDS = [120, 100, 80, 60, 40, 20, 0]
RING_NAMES = ["дальше 120 км", "100–120 км", "80–100 км", "60–80 км", "40–60 км", "20–40 км", "ближе 20 км"]

# Места, для которых уже написан маршрут: номер в таблице -> id файла в content/pack/destinations.
WITH_CONTENT = {
    177: "reutov", 159: "kotelniki", 124: "odintsovo", 149: "balashikha",
    # Усадьбы Горенки и Пехра-Яковлевское входят в маршрут «Балашиха» как перемычки.
    176: "balashikha", 150: "balashikha",
}

SUPERLATIVES = [
    ("biggest-city", "Самый большой город — Балашиха", 149, "Крупнейший город Подмосковья по населению"),
    ("oldest-city", "Самый старый город — Волоколамск (1135)", 27, "Первое упоминание в летописи — 1135 год"),
    ("smallest-city", "Самый маленький город — Верея", 22, "Самый малонаселённый город области"),
    ("biggest-rp", "Самый большой рабочий посёлок — Нахабино", 127, ""),
    ("biggest-village", "Самая большая деревня — Путилково", 170, ""),
    ("famous-dacha", "Самый известный дачный посёлок — Переделкино", 122, ""),
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


def ring_of(d):
    for i, b in enumerate(BOUNDS):
        if d >= b:
            return i
    return len(BOUNDS) - 1


def region_type(name):
    return "Москва_АО" if name.startswith("Москва") else "МО_городской_округ"


def main():
    rows = [l.rstrip("\n").split("\t") for l in open(os.path.join(HERE, "appendix_a.tsv"), encoding="utf-8") if l.strip()]
    # Ссылка на маршрут ставится, только если файл маршрута уже есть в паке.
    ready = {k: v for k, v in WITH_CONTENT.items()
             if os.path.exists(os.path.join(ROOT, "pack", "destinations", v + ".json"))}
    places, regions = [], {}
    for num, mark, name, kind, region, *_rest in rows:
        lat, lon = float(_rest[2]), float(_rest[3])
        rid = slug(region)
        regions.setdefault(rid, {"id": rid, "name": region, "type": region_type(region), "verified": False})
        finish = kind == "финиш спирали"
        d = hav(KREMLIN, (lat, lon))
        places.append({
            "id": "kremlin" if finish else slug(name),
            "num": int(num),
            "name": name,
            "kindNote": kind,
            "regionId": rid,
            "mustHave": 3 if mark == "★" else 2 if mark == "☆" else 1,
            "coords": {"lat": lat, "lon": lon},
            "distanceFromKremlinKm": round(d, 1),
            "azimuthDeg": 0.0 if finish else round(bearing(KREMLIN, (lat, lon)), 1),
            "ring": len(BOUNDS) - 1 if finish else ring_of(d),
            "destinationId": ready.get(int(num)),
            "finish": finish,
        })
    # Порядок спирали.
    route = [p for p in places if not p["finish"]]
    ordered, start = [], None
    for ring in range(len(BOUNDS)):
        inside = [p for p in route if p["ring"] == ring]
        if not inside:
            continue
        if start is None:
            start = max(inside, key=lambda p: p["distanceFromKremlinKm"])["azimuthDeg"]
        inside.sort(key=lambda p: ((p["azimuthDeg"] - start) % 360, p["num"]))
        ordered += inside
        start = inside[-1]["azimuthDeg"]
    ordered += [p for p in places if p["finish"]]
    moved = [p["name"] for i, p in enumerate(ordered) if p["num"] != i + 1]
    if moved:
        print("Порядок изменился относительно таблицы:", ", ".join(moved[:10]), file=sys.stderr)
    for i, p in enumerate(ordered):
        p["num"] = i + 1
    ids = [p["id"] for p in ordered]
    assert len(ids) == len(set(ids)), "дубли id"
    by_num = {int(r[0]): slug(r[2]) for r in rows}
    pack = {
        "version": 1,
        "origin": {"lat": KREMLIN[0], "lon": KREMLIN[1]},
        "rings": [{"index": i, "name": n, "minKm": float(BOUNDS[i]), "maxKm": None if i == 0 else float(BOUNDS[i - 1])}
                  for i, n in enumerate(RING_NAMES)],
        "regions": sorted(regions.values(), key=lambda r: r["name"]),
        "places": ordered,
        "superlatives": [{"id": i, "title": t, "placeId": by_num.get(n) if n else None, "note": note}
                         for i, t, n, note in SUPERLATIVES],
    }
    with open(os.path.join(ROOT, "pack", "spiral.json"), "w", encoding="utf-8") as f:
        json.dump(pack, f, ensure_ascii=False, indent=1)
        f.write("\n")
    print(f"spiral.json: {len(ordered)} мест, {len(regions)} округов")


if __name__ == "__main__":
    main()
