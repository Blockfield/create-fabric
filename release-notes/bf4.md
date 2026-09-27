---
schema_version: 1
repository: Blockfield/create-fabric
version: bf4
previous_version: bf3
date: 2026-09-27
kind: mod
backfilled: false
---

# Blockfield/create-fabric bf4

## Кратко

Create Fabric bf4: невидимые рельсовые полотна кривых сталкиваются только с сущностями (чинятся дрели/ролики/вентиляторы и разборка contraption), широкие пути прописаны в тегах путей и отдают дроп.

## Для игроков

Исправления: механизмы на contraption больше не ломаются о невидимые полотна кривых, вертикальные вентиляторы снова дуют сквозь них; широкие пути корректно учитываются при выживательном строительстве кривых, их нельзя увезти contraption, разрушение даёт урезанные частицы, сломанный путь дропает предмет. Простреливаемость путей пулями TACZ не меняется (пули проверяют PassThroughBlocks/tacz:bullet_ignore и collision shape, тег create:tracks в этой цепочке не участвует — проверено чтением TACZ bf7). Остальное: сведений нет.

## Технические изменения

- FakeTrackBlock.getCollisionShape: пусто для запросов без сущности; сущности (игроки, мобы, предметы, снаряды) сталкиваются как раньше.
- Теги create:tracks, girdable_tracks, c:relocation_not_supported, has_reduced_destroy_effects (+ item tracks): добавлены railways:track_create_andesite_wide и railways:track_dark_oak_wide (генерация через CreateRegistrateTags — у wide-треков нет datagen).
- Лут широких путей переехал data/railways/loot_tables/ в loot_table/ (путь 1.21) — дроп снова выпадает.

## Обновлённые компоненты

Внешних игровых зависимостей у форка нет; изменения внутренние.

## Совместимость и необходимые действия

Пак закрепляет jar командой scripts/bump-fork.sh create; обновить пин пака (side=both).

## Известные проблемы

Неизвестны (на момент подготовки описания сведения отсутствуют).

## Источники и ограничения полноты

- Коммиты bf3..bf4: 97f64ea6a (entity-only collision fake_track), b1ed82c54 (теги wide + loot_table), eb1a8aeb7 (механизм release-notes — на игру не влияет).
- Проверено чтением кода форка и распаковкой TACZ bf7 (data/tacz/tags/block/bullet_ignore.json: только iron_bars/fences/fence_gates/leaves); живых замеров стрельбы по путям на стенде нет.
