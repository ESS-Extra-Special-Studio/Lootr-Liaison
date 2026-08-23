# Changelog

## 1.2.2

- Standalone: no ExtraSpecialCore / ExtraSpecialHub / ES Library. Pack-maker diagnostics stay on `/lootr_liason` commands.

## 1.2.1

- **ESS domain swap** — Java packages moved from `uk.creatopia.unbound.lootr_liason…` to `uk.co.extraspecialstudio.lootr_liason…`.
- Fixed a startup crash where the LevelChunk mixin failed to apply (broken remapping / empty refmap after the package move).

## 1.2 (2026-04-14)

- Added explicit Dead Letters (`dead_letters`) detection at common setup; when that mod is present, logs a clear compatibility line so packs know Liaison and Dead Letters are composed together. No change to finalization queue, Lootr adapter behavior, mimic gate, or loot passthrough logic.

## 1.1 (2026-01-27)

- Suppresses the "block entity had its loot table set before its level was set" log spam during worldgen. Harmless warning; filter keeps logs readable.
- Log filter is installed at common setup and fails quietly if Log4j isn't in the expected state.

## 1.0

- Initial release. Structure container finalization, mimic stability, config, commands.
