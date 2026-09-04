package sekelsta.horse_colors.world;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.tag.BiomeTags;
import net.minecraft.util.Identifier;

import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.fabricmc.fabric.api.biome.v1.ModificationPhase;
import net.fabricmc.fabric.api.tag.convention.v1.ConventionalBiomeTags;

import sekelsta.horse_colors.HorseColors;
import sekelsta.horse_colors.entity.ModEntities;

/**
 * Replaces horse_colors:horse_spawn.json / donkey_spawn.json, which relied on
 * NeoForge's data-driven BiomeModifier - Fabric's biome-modification API
 * (fabric-biome-api-v1) is code-driven instead, so the two datapack files'
 * plains/savanna spawner entries and the "remove vanilla horse/donkey spawns"
 * logic they both carried are reimplemented here directly.
 */
public class Spawns {
    public static void registerBiomeModifiers() {
        // horse_spawn.json: horse_felinoid in plains (weight 5) and savanna (weight 1)
        BiomeModifications.addSpawn(BiomeSelectors.tag(ConventionalBiomeTags.PLAINS), SpawnGroup.CREATURE, ModEntities.HORSE_GENETIC, 5, 2, 6);
        BiomeModifications.addSpawn(BiomeSelectors.tag(BiomeTags.IS_SAVANNA), SpawnGroup.CREATURE, ModEntities.HORSE_GENETIC, 1, 2, 6);

        // donkey_spawn.json: donkey in plains and savanna, both weight 1
        BiomeModifications.addSpawn(BiomeSelectors.tag(ConventionalBiomeTags.PLAINS), SpawnGroup.CREATURE, ModEntities.DONKEY_GENETIC, 1, 1, 3);
        BiomeModifications.addSpawn(BiomeSelectors.tag(BiomeTags.IS_SAVANNA), SpawnGroup.CREATURE, ModEntities.DONKEY_GENETIC, 1, 1, 3);

        // Both original biome modifier files also removed vanilla horse/donkey
        // spawns everywhere, unconditionally (not gated on HorseConfig.SPAWN's
        // blockVanillaHorseSpawns/blockVanillaDonkeySpawns - those are unused by
        // the original mod too, kept as dead config for fidelity).
        BiomeModifications.create(new Identifier(HorseColors.MOD_ID, "remove_vanilla_equine_spawns"))
            .add(ModificationPhase.REMOVALS, BiomeSelectors.all(), context -> {
                context.getSpawnSettings().removeSpawnsOfEntityType(EntityType.HORSE);
                context.getSpawnSettings().removeSpawnsOfEntityType(EntityType.DONKEY);
            });
    }
}
