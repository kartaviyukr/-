# Tower War (Война Башен)

Пошаговая магическая стратегия захвата территорий на картах реальных городов. Первая карта — Гент, дальше в планах Антверпен, Брюссель, Стамбул и Токио. Полная спецификация лежит в [GAME_SPEC.md](GAME_SPEC.md).

Движок — Godot 4.7.2 (GDScript со статической типизацией), платформы — Android и PC. Карты строятся из данных OpenStreetMap: © OpenStreetMap contributors, лицензия ODbL.

## Структура

```
game/                    Godot-проект
  src/sim/               детерминированная симуляция, без Node и сцен
  src/data/              загрузка данных (пакеты карт, правила)
  src/view/ src/input/   рендер карты и единый слой ввода (фаза 2)
  src/ui/                экраны
  data/rules/            общий баланс: школы, юниты, заклинания, эффекты
  data/maps/<city>/      пакеты карт (генерирует tools/geo)
  locale/                строки ru/en; locale/maps/<city>.csv — строки карт
  tests/                 GUT-тесты и фикстуры
tools/geo/               Python-пайплайн OSM → пакет карты, конфиги городов в cities/
tools/tests/             общие тесты инструментов (эталонный RNG)
docs/                    форматы данных
scripts/                 установка Godot и запуск всех тестов
archive/                 файлы, не относящиеся к игре
```

## Запуск тестов

```bash
scripts/test.sh          # скачает Godot 4.7.2 в .tools/ при первом запуске
```

Или по отдельности:

```bash
GODOT=$(scripts/setup_godot.sh)
$GODOT --headless --path game --import
$GODOT --headless --path game -s res://addons/gut/gut_cmdln.gd
cd tools/geo && python3 -m pytest
```

## Открыть в редакторе

Откройте `game/project.godot` в Godot 4.7.x. GUT (9.7.1) уже лежит в `game/addons/gut`.

## Новый город

1. Создайте `tools/geo/cities/<city>.toml` по образцу `ghent.toml`.
2. Добавьте строки карты в `game/locale/maps/<city>.csv`.
3. Запустите пайплайн (фаза 1): см. [tools/geo/README.md](tools/geo/README.md).

В коде движка нет данных о конкретных городах: всё городское хранится в пакете карты (формат описан в [docs/map_format.md](docs/map_format.md)).
