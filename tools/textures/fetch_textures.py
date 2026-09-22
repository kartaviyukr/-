#!/usr/bin/env python3
"""
Готовит текстуры для приложения.

1. Скачивает бесшовные фото-текстуры с Poly Haven (лицензия CC0 — можно
   свободно использовать и распространять) и ужимает их до небольших плиток.
2. Дорисовывает то, чего на Poly Haven нет: лаву, воду, кислоту, бездну,
   магическое сияние и прочее — процедурно, тоже бесшовно.

Результат: app/src/main/assets/textures/<ключ>.jpg и CREDITS.txt.
Запускается в GitHub Actions (tools/textures/*.yml), где есть интернет.
"""
import io
import json
import math
import os
import sys
import urllib.request

import numpy as np
from PIL import Image, ImageFilter

OUT = os.path.join(os.path.dirname(__file__), "..", "..", "app", "src", "main", "assets", "textures")
SIZE = 320
UA = "FantasyMapCreator-texture-fetch/1.0 (github.com/kartaviyukr)"

# ключ -> варианты поиска: в каждом варианте все слова должны встретиться
# в имени, названии, тегах или категориях ассета
WANTED = {
    "flagstone": [["flagstone"], ["stone", "floor"], ["paving"]],
    "cobblestone": [["cobblestone"], ["cobble"]],
    "stone_tiles": [["stone", "tiles"], ["floor", "tiles"], ["tiles"]],
    "marble": [["marble"]],
    "brick": [["brick", "wall"], ["brick"]],
    "castle_wall": [["castle"], ["stone", "wall"]],
    "wood_planks": [["wood", "planks"], ["planks"], ["wood", "floor"]],
    "old_wood": [["weathered", "wood"], ["old", "wood"], ["wood"]],
    "bark": [["bark"]],
    "dirt": [["dirt"], ["soil"], ["ground"]],
    "mud": [["mud"]],
    "grass": [["grass"]],
    "forest_floor": [["forest"], ["leaves"], ["leaf"]],
    "sand": [["sand"]],
    "gravel": [["gravel"], ["pebbles"]],
    "rock": [["rock"], ["cliff"]],
    "rocky_ground": [["rocky"], ["rocks", "ground"], ["stones"]],
    "snow": [["snow"]],
    "moss": [["moss"]],
    "ice": [["ice"], ["frozen"]],
    "fabric": [["carpet"], ["rug"], ["fabric"]],
    "roof": [["roof"]],
    "plaster": [["plaster"], ["concrete"]],
    "metal": [["metal", "plate"], ["metal"]],
    "dry_ground": [["dry"], ["cracked"]],
    "hay": [["hay"], ["straw"]],
    "aerial_grass": [["aerial", "grass"], ["aerial"]],
    "aerial_rocks": [["aerial", "rock"], ["aerial"]],
    "aerial_sand": [["aerial", "sand"], ["aerial", "beach"], ["aerial"]],
}


def get(url):
    request = urllib.request.Request(url, headers={"User-Agent": UA})
    with urllib.request.urlopen(request, timeout=60) as response:
        return response.read()


def words_of(asset_id, info):
    parts = [asset_id, info.get("name", "")]
    parts += info.get("tags", []) or []
    parts += info.get("categories", []) or []
    return " ".join(parts).lower().replace("_", " ")


def tile(image):
    image = image.convert("RGB")
    side = min(image.size)
    image = image.crop((0, 0, side, side))
    return image.resize((SIZE, SIZE), Image.LANCZOS)


def save(key, image, credits, source):
    path = os.path.join(OUT, key + ".jpg")
    image.save(path, "JPEG", quality=82, optimize=True)
    credits.append(f"{key}.jpg — {source}")
    print("ok", key, "<-", source)


def fetch_polyhaven(credits):
    try:
        assets = json.loads(get("https://api.polyhaven.com/assets?t=textures"))
    except Exception as error:  # noqa: BLE001
        print("Poly Haven недоступен:", error)
        return
    print("всего текстур на Poly Haven:", len(assets))
    used = set()
    for key, variants in WANTED.items():
        chosen = None
        for terms in variants:
            matches = []
            for asset_id, info in assets.items():
                text = words_of(asset_id, info)
                if all(term in text for term in terms):
                    matches.append((asset_id in used, -int(info.get("download_count", 0)), asset_id))
            if matches:
                matches.sort()
                chosen = matches[0][2]
                break
        if chosen is None:
            print("нет подходящей текстуры для", key)
            continue
        try:
            files = json.loads(get(f"https://api.polyhaven.com/files/{chosen}"))
            diffuse = files.get("Diffuse") or files.get("diffuse") or files.get("Color")
            url = diffuse["1k"]["jpg"]["url"]
            image = Image.open(io.BytesIO(get(url)))
            save(key, tile(image), credits,
                 f"Poly Haven «{assets[chosen].get('name', chosen)}» "
                 f"https://polyhaven.com/a/{chosen} (CC0)")
            used.add(chosen)
        except Exception as error:  # noqa: BLE001
            print("не удалось скачать", key, chosen, error)


# ---------------------------------------------------------------- процедурные

def periodic_noise(size, cells, seed):
    """Бесшовный шум: сумма синусов с целыми частотами."""
    rng = np.random.default_rng(seed)
    y, x = np.mgrid[0:size, 0:size] / size * 2 * math.pi
    result = np.zeros((size, size))
    for octave in range(1, 6):
        for _ in range(cells):
            fx = rng.integers(-2 ** octave, 2 ** octave + 1)
            fy = rng.integers(-2 ** octave, 2 ** octave + 1)
            phase = rng.uniform(0, 2 * math.pi)
            result += np.sin(fx * x + fy * y + phase) / (octave ** 1.3)
    result -= result.min()
    result /= max(result.max(), 1e-6)
    return result


def colorize(values, stops):
    """values 0..1 -> цвет по опорным точкам [(t, (r,g,b)), ...]."""
    out = np.zeros(values.shape + (3,))
    ts = [s[0] for s in stops]
    for channel in range(3):
        out[..., channel] = np.interp(values, ts, [s[1][channel] for s in stops])
    return Image.fromarray(out.clip(0, 255).astype("uint8"), "RGB")


def veins(size, seed, sharp=12.0):
    base = periodic_noise(size, 6, seed)
    return np.exp(-sharp * np.abs(base - 0.5))


def generated(credits):
    n = SIZE
    recipes = {
        "lava": lambda: colorize(
            np.clip(veins(n, 11, 10) * 0.9 + periodic_noise(n, 4, 12) * 0.25, 0, 1),
            [(0, (38, 16, 12)), (0.35, (70, 22, 12)), (0.6, (190, 50, 10)),
             (0.8, (250, 140, 20)), (1, (255, 230, 120))]),
        "water": lambda: colorize(
            np.clip(periodic_noise(n, 5, 21) * 0.7 + veins(n, 22, 18) * 0.35, 0, 1),
            [(0, (38, 92, 128)), (0.6, (58, 124, 160)), (0.85, (104, 170, 200)),
             (1, (190, 225, 240))]),
        "deep_water": lambda: colorize(
            periodic_noise(n, 5, 31),
            [(0, (10, 30, 58)), (0.7, (22, 58, 94)), (1, (50, 96, 132))]),
        "acid": lambda: colorize(
            np.clip(periodic_noise(n, 5, 41) * 0.6 + veins(n, 42, 16) * 0.5, 0, 1),
            [(0, (40, 70, 20)), (0.5, (110, 170, 30)), (0.85, (180, 230, 60)),
             (1, (230, 255, 150))]),
        "swamp": lambda: colorize(
            periodic_noise(n, 6, 51),
            [(0, (38, 46, 28)), (0.45, (62, 72, 40)), (0.7, (86, 92, 52)),
             (1, (120, 118, 70))]),
        "abyss": lambda: colorize(
            np.clip(periodic_noise(n, 5, 61) * 0.8 + veins(n, 62, 20) * 0.3, 0, 1),
            [(0, (4, 4, 10)), (0.7, (14, 12, 28)), (0.95, (48, 30, 80)),
             (1, (110, 70, 170))]),
        "magic": lambda: colorize(
            np.clip(veins(n, 71, 9) * 0.8 + periodic_noise(n, 5, 72) * 0.3, 0, 1),
            [(0, (30, 20, 60)), (0.5, (70, 50, 150)), (0.85, (140, 110, 240)),
             (1, (230, 220, 255))]),
        "blood": lambda: colorize(
            periodic_noise(n, 6, 81),
            [(0, (50, 6, 8)), (0.6, (110, 14, 16)), (1, (170, 40, 36))]),
        "ash": lambda: colorize(
            periodic_noise(n, 6, 91),
            [(0, (48, 46, 44)), (0.6, (92, 88, 84)), (1, (150, 144, 138))]),
        "crystal": lambda: colorize(
            np.clip(veins(n, 101, 7) * 0.7 + periodic_noise(n, 5, 102) * 0.4, 0, 1),
            [(0, (40, 70, 100)), (0.5, (90, 160, 200)), (1, (220, 250, 255))]),
        "void_ice": lambda: colorize(
            np.clip(periodic_noise(n, 5, 111) * 0.7 + veins(n, 112, 14) * 0.4, 0, 1),
            [(0, (150, 190, 210)), (0.6, (200, 228, 240)), (1, (245, 252, 255))]),
        "poison_fog": lambda: colorize(
            periodic_noise(n, 4, 121),
            [(0, (60, 80, 40)), (0.5, (100, 130, 60)), (1, (170, 200, 110))]),
        "bones": lambda: colorize(
            np.clip(veins(n, 131, 22) + periodic_noise(n, 5, 132) * 0.4, 0, 1),
            [(0, (70, 62, 50)), (0.5, (120, 110, 92)), (0.8, (200, 190, 165)),
             (1, (235, 228, 205))]),
    }
    for key, recipe in recipes.items():
        path = os.path.join(OUT, key + ".jpg")
        try:
            image = recipe().filter(ImageFilter.SMOOTH)
            save(key, image, credits, "нарисована процедурно для приложения (CC0)")
        except Exception as error:  # noqa: BLE001
            print("не удалось нарисовать", key, error)


def main():
    os.makedirs(OUT, exist_ok=True)
    credits = []
    fetch_polyhaven(credits)
    generated(credits)
    with open(os.path.join(OUT, "CREDITS.txt"), "w", encoding="utf-8") as handle:
        handle.write("Текстуры приложения. Все — CC0 (общественное достояние).\n")
        handle.write("Фото-текстуры: Poly Haven, https://polyhaven.com/license\n\n")
        handle.write("\n".join(credits) + "\n")
    print("готово:", len(credits), "текстур")
    if not credits:
        sys.exit(1)


if __name__ == "__main__":
    main()
