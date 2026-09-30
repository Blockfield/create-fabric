# Порт Create 6 на Fabric 1.21.1 — брифинг для агентов

Репозиторий: `create-fabric/` (ветка `mc1.21.1/fabric/dev` Fabricators-of-Create, брошена в марте 2025
на полпути; коммит 7ffc819 — наша база). Minecraft 1.21.1, маппинги officialMojang + parchment,
Fabric API 0.115, Porting Lib 3.1.0-beta.54+1.21.1 (модули blocks, accessors, entity, items,
client_events, level_events, attributes, extensions, brewing, config, chunk_loading, obj_loader,
mixin_extensions, loot, item_abilities, gui_utils, render_types, base, common, core, data, fluids,
gametest, lazy_registration, model_loader, models, tags, transfer), Flywheel fabric 1.0.1-11,
Registrate 1.3.77-MC1.21.1, Ponder 1.0.44, Forge Config API Port 21.1.3.
Эталон для сверки — апстрим NeoForge `mc1.21.1/dev` в `/tmp/create-plan/Create` и
Fabric-версия 1.20.1 (`git log` этого же репозитория, ветка `mc1.20.1/dev` на GitHub) — как
Fabricators решали ту же проблему на 1.20.1.

## Железные правила

1. **Не выдумывай API.** Перед использованием метода/класса из Fabric API, Porting Lib, Flywheel,
   Registrate — проверь его `javap -p` по jar из `~/.gradle/caches/` (найти: `find ~/.gradle/caches -name '*porting*blocks*1.21.1*.jar'`)
   или прочитай исходники в `.gradle/loom-cache/`/sources-jar. Ванильные имена не меняются.
2. **Не запускай gradle.** 13 ГБ RAM на всех, сборку гоняет координатор и присылает лог ошибок.
   Твой список ошибок — `port/errors-<группа>.txt` (только первые 4 строки каждой ошибки;
   вторичные «cannot find symbol» часто уходят вместе с первичной).
3. Трогай только файлы своей группы. Общие шимы (например, замена `net.neoforged.neoforge.common.Tags`
   на `io.github.fabricators_of_create.porting_lib.tags.Tags`, замена `Capabilities` на Fabric lookup API,
   `ResourceLocation.fromNamespaceAndPath`) — используй то, что уже есть в репозитории
   (`grep -rn "porting_lib.tags.Tags" src` покажет образец), новые общие классы клади в
   `src/main/java/com/simibubi/create/foundation/fabric/` и опиши в отчёте.
4. Compat с модами, которых нет в паке Blockfield (EMI, REI, FTB Chunks, Computercraft, Sodium-specific
   миксины, Reach Entity Attributes) — удалять целиком, а не чинить (и вычищать из `create.mixins.json`,
   `fabric.mod.json` entrypoints). JEI остаётся (он в паке).
5. Не переписывай `create.mixins.json` и `fabric.mod.json` целиком — только точечные правки.
6. Отчёт в конце: что исправил, что удалил, какие общие шимы нужны, какие ошибки не смог закрыть и почему.
   Коротко, по-русски.

## Частые замены (уже применённые в других файлах ветки — ищи образцы grep'ом)

- `new ResourceLocation(ns, path)` → `ResourceLocation.fromNamespaceAndPath(ns, path)`; `new ResourceLocation(s)` → `ResourceLocation.parse(s)`.
- `net.neoforged.neoforge.common.Tags.Items.*` → `io.github.fabricators_of_create.porting_lib.tags.Tags.Items.*` (проверь наличие тега javap'ом; нет — `ConventionalItemTags` из fabric-convention-tags-v2).
- `Capabilities.ItemHandler/FluidHandler` → `ItemStorage.SIDED` / `FluidStorage.SIDED` (Fabric transfer API) через уже существующие обёртки Porting Lib `transfer`.
- События NeoForge (`LivingEntityEvents`, `EntityEvents`, `ClientWorldEvents`) → Porting Lib `*_events` модули или Fabric API events — смотри, как сделано в 1.20.1 ветке.
- `player.openMenu(provider, buf -> ...)` → `player.openMenu(new ExtendedScreenHandlerFactory<>(...))` (fabric-screen-handler-api-v1) — образец в репозитории есть.
