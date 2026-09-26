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
    # ключ: (варианты слов в имени ассета, слова-исключения)
    "flagstone": ([["flagstone"], ["stone", "floor"], ["paving"], ["granite", "tile"]], []),
    "cobblestone": ([["cobblestone"], ["cobble"]], []),
    "stone_tiles": ([["stone", "tiles"], ["floor", "tiles"]], ["rubber"]),
    "marble": ([["marble"]], []),
    "brick": ([["red", "brick"], ["brick", "wall"], ["brick"]], []),
    "castle_wall": ([["castle"], ["stone", "wall"]], []),
    "wood_planks": ([["wood", "planks"], ["planks"], ["wood", "floor"]], ["laminate"]),
    "old_wood": ([["weathered", "planks"], ["old", "planks"], ["weathered", "wood"]], []),
    "bark": ([["bark"]], []),
    "dirt": ([["dirt", "ground"], ["brown", "ground"], ["soil"], ["dirt"]], ["concrete", "leaves", "carpet", "dirty"]),
    "mud": ([["mud"]], []),
    "grass": ([["grass", "path"], ["grass"], ["meadow"], ["lawn"]], ["sand", "rock"]),
    "forest_floor": ([["forest", "ground"], ["forrest", "ground"], ["forest", "leaves"], ["leaves"]], []),
    "sand": ([["sand"]], ["rock", "aerial"]),
    "gravel": ([["gravel"], ["pebbles"], ["rocks", "ground"]], []),
    "rock": ([["rock", "wall"], ["cliff"], ["rock", "face"], ["rock"]], ["aerial", "ground"]),
    "rocky_ground": ([["rocky", "terrain"], ["rocky"], ["rocks", "ground"]], []),
    "snow": ([["snow"]], []),
    "moss": ([["moss"], ["mossy"]], []),
    "fabric": ([["carpet"], ["rug"], ["fabric", "pattern"], ["fabric"]], []),
    "roof": ([["roof", "tiles"], ["clay", "roof"], ["roof"]], ["thatch", "rubber"]),
    "plaster": ([["plaster"], ["clay", "wall"]], []),
    "metal": ([["metal", "plate"], ["metal"]], []),
    "dry_ground": ([["dry", "ground"], ["cracked", "ground"], ["dry", "soil"], ["cracked"]], ["wood", "paint"]),
    "hay": ([["hay"], ["straw"], ["thatch"]], []),
    "aerial_grass": ([["aerial", "grass"], ["aerial", "ground"], ["grass", "field"], ["meadow"], ["grass"]], ["rock", "path"]),
    "aerial_rocks": ([["aerial", "rocks"], ["aerial", "rock"]], []),
    "aerial_sand": ([["aerial", "sand"], ["aerial", "beach"]], []),
    # кровли для городских построек
    "roof_clay": ([["clay", "roof"], ["terracotta"], ["red", "roof"], ["roof", "tiles"]], ["grey", "gray", "thatch"]),
    "roof_slate": ([["slate", "roof"], ["roof", "slates"], ["slate"]], ["floor"]),
    "roof_shingle": ([["wood", "shingles"], ["shingles"], ["shingle"]], []),
    "roof_copper": ([["copper"], ["patina"], ["green", "metal"]], []),
    "roof_tin": ([["corrugated"], ["rusty", "metal"], ["metal", "roof"]], []),
}


def get(url):
    request = urllib.request.Request(url, headers={"User-Agent": UA})
    with urllib.request.urlopen(request, timeout=60) as response:
        return response.read()


def words_of(asset_id, info):
    """Только имя и название: теги у Poly Haven слишком общие."""
    return (asset_id + " " + info.get("name", "")).lower().replace("_", " ")


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
    # Poly Haven доступен — старые плитки можно убрать и собрать заново.
    for name in os.listdir(OUT):
        if name.endswith(".jpg"):
            os.remove(os.path.join(OUT, name))
    used = set()
    for key, (variants, excluded) in WANTED.items():
        chosen = None
        for terms in variants:
            matches = []
            for asset_id, info in assets.items():
                text = words_of(asset_id, info)
                if any(word in text for word in excluded):
                    continue
                if all(term in text for term in terms):
                    matches.append((asset_id in used, -int(info.get("download_count", 0)), asset_id))
            if matches:
                matches.sort()
                print(key, terms, "кандидаты:", [m[2] for m in matches[:6]])
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


def tiles(size, width, height, shape, palette, seed, stagger=True):
    """Бесшовная кровля рядами: shape(u, v) -> яркость 0..1 внутри одной плитки."""
    rng = np.random.default_rng(seed)
    y, x = np.mgrid[0:size, 0:size]
    row = y // height
    shift = np.where(row % 2 == 1, width // 2, 0) if stagger else 0
    xs = (x + shift) % size
    col = xs // width
    u = (xs % width) / width
    v = (y % height) / height
    rows = size // height
    cols = size // width
    tone = rng.uniform(-0.12, 0.12, size=(rows + 1, cols + 1))[row, col]
    value = np.clip(shape(u, v) + tone + periodic_noise(size, 4, seed + 1) * 0.15 - 0.07, 0, 1)
    return colorize(value, palette)


def roof_recipes(n):
    """Кровли, которых может не быть на Poly Haven: рисуются рядами плиток."""
    return {
        "roof_clay_generated": lambda: tiles(
            n, 32, 32, lambda u, v: (0.35 + 0.55 * np.sin(np.pi * u)) * (1 - 0.45 * v) + (v < 0.08) * -0.3,
            [(0, (70, 26, 16)), (0.45, (150, 62, 34)), (0.75, (196, 96, 52)), (1, (232, 150, 96))], 301),
        "roof_slate_generated": lambda: tiles(
            n, 40, 20, lambda u, v: 0.55 + 0.25 * (1 - v) - (u < 0.05) * 0.35 - (v > 0.9) * 0.35,
            [(0, (28, 32, 40)), (0.5, (66, 74, 88)), (1, (118, 128, 142))], 311),
        "roof_shingle_generated": lambda: tiles(
            n, 20, 16, lambda u, v: 0.5 + 0.3 * (1 - v) - (u < 0.08) * 0.4 - (v > 0.88) * 0.3,
            [(0, (50, 34, 22)), (0.5, (112, 80, 52)), (1, (170, 132, 92))], 321),
        "roof_copper_generated": lambda: colorize(
            np.clip(periodic_noise(n, 6, 331) * 0.7 + veins(n, 332, 14) * 0.35, 0, 1),
            [(0, (60, 96, 80)), (0.45, (92, 150, 124)), (0.8, (140, 190, 160)), (1, (190, 150, 90))]),
        "roof_tin_generated": lambda: tiles(
            n, 20, n, lambda u, v: 0.5 + 0.35 * np.sin(2 * np.pi * u),
            [(0, (70, 72, 74)), (0.5, (120, 122, 124)), (1, (180, 176, 168))], 341, stagger=False),
        "roof_gold": lambda: tiles(
            n, 32, 32, lambda u, v: (0.4 + 0.5 * np.sin(np.pi * u)) * (1 - 0.4 * v),
            [(0, (110, 72, 18)), (0.5, (200, 150, 50)), (0.85, (240, 205, 100)), (1, (255, 240, 180))], 351),
        "roof_glass": lambda: tiles(
            n, 40, 40, lambda u, v: 0.65 + 0.3 * (u + v < 0.6) - ((u < 0.07) | (v < 0.07)) * 0.6,
            [(0, (48, 58, 60)), (0.5, (120, 170, 180)), (1, (215, 240, 245))], 361, stagger=False),
    }


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
        "ice": lambda: colorize(
            np.clip(periodic_noise(n, 5, 141) * 0.6 + veins(n, 142, 26) * 0.5, 0, 1),
            [(0, (120, 170, 200)), (0.5, (170, 210, 230)), (0.85, (215, 238, 248)),
             (1, (250, 254, 255))]),
        "void_ice": lambda: colorize(
            np.clip(periodic_noise(n, 5, 111) * 0.7 + veins(n, 112, 14) * 0.4, 0, 1),
            [(0, (150, 190, 210)), (0.6, (200, 228, 240)), (1, (245, 252, 255))]),
        "poison_fog": lambda: colorize(
            periodic_noise(n, 4, 121),
            [(0, (60, 80, 40)), (0.5, (100, 130, 60)), (1, (170, 200, 110))]),
        "dry_ground_generated": lambda: colorize(
            np.clip(periodic_noise(n, 5, 151) * 0.7 - veins(n, 152, 30) * 0.6 + 0.3, 0, 1),
            [(0, (80, 58, 40)), (0.4, (150, 118, 82)), (1, (196, 166, 124))]),
        "bones": lambda: colorize(
            np.clip(veins(n, 131, 22) + periodic_noise(n, 5, 132) * 0.4, 0, 1),
            [(0, (70, 62, 50)), (0.5, (120, 110, 92)), (0.8, (200, 190, 165)),
             (1, (235, 228, 205))]),
    }
    recipes.update(roof_recipes(n))
    for key, recipe in recipes.items():
        if key.endswith("_generated"):
            key = key[: -len("_generated")]
            if os.path.exists(os.path.join(OUT, key + ".jpg")):
                continue
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
