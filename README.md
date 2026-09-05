# Realistic Horse Genetics (Fabric)

An unofficial **Fabric port** of [Realistic Horse Genetics](https://www.curseforge.com/minecraft/mc-mods/realistic-horse-genetics) by [sekelsta](https://github.com/sekelsta) ([original source](https://github.com/sekelsta/horse-colors)), which was written for Forge/NeoForge and has no official Fabric release.

All credit for the genetics design, coat color research, and original implementation belongs to sekelsta. This repository is an independent, from-scratch translation of that mod's logic onto the Fabric toolchain — not a fork sharing commit history with the original, since a line-by-line port meant rewriting against a different modding API throughout.

Adds biologically realistic genetics for horses, donkeys, and mules: coat colors and patterns with real genetic linkage, genes affecting speed/jump/health, variable size, a gender and pregnancy system, and a genetic-testing book — same feature set as the original mod.

## Requirements

- Minecraft 1.20.1
- Fabric Loader ≥0.15.0
- [Fabric API](https://modrinth.com/mod/fabric-api)
- Java 17+

## Building

```
./gradlew build
```

The built jar is written to `build/libs/`.

## What differs from the original Forge/NeoForge version

Gameplay and content are unchanged. A few pieces of infrastructure needed different plumbing because Fabric doesn't expose the same modding hooks Forge/NeoForge do:

- **Custom brewing recipe** (Awkward Potion + Poisonous Potato → Infertility Potion): Forge's `IBrewingRecipe` has no Fabric equivalent, so this is a Mixin into `BrewingStandBlockEntity` instead.
- **Horse inventory screen replacement**: vanilla has no registry hook for the horse GUI at all (`HorseScreenHandler`/`HorseScreen` are opened directly by `ClientPlayNetworkHandler`, not through the normal screen-handler registry), so swapping in the mod's custom screen needed a full-method mixin rather than a registration call.
- **Biome spawn placement**: the original's data-driven Forge `BiomeModifier` JSON has no Fabric counterpart; this is reimplemented in code via Fabric API's `BiomeModifications`, preserving the same spawn weights and group sizes.
- **Vanilla-horse auto-conversion** (optional, off by default): hooks `ServerWorld#spawnEntity`, covering natural spawns, spawn eggs, and commands, but not the separate world-generation entity-placement path the original specially handled.
- A handful of NeoForge-injected extension hooks (`isFood`, `isArmor`, `canFallInLove`, `canWearArmor`) don't exist on Fabric/vanilla and were dropped or kept as plain (non-override) methods where nothing else called them polymorphically.

## License

- Code: LGPL-3.0-or-later, same as the original mod. See [LICENSE.md](LICENSE.md).
- Assets (textures, translations): CC-BY-4.0-or-later, same as the original. See [CREDITS.txt](CREDITS.txt) for texture/translation attributions carried over from upstream.

## Links

- Original mod (CurseForge): https://www.curseforge.com/minecraft/mc-mods/realistic-horse-genetics
- Original source (Forge/NeoForge): https://github.com/sekelsta/horse-colors
