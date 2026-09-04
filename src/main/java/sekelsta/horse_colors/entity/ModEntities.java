package sekelsta.horse_colors.entity;

import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.SpawnRestriction;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.passive.AbstractHorseEntity;
import net.minecraft.entity.passive.AbstractDonkeyEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.world.Heightmap;

import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;

import sekelsta.horse_colors.HorseColors;
import sekelsta.horse_colors.client.renderer.HorseArmorLayer;
import sekelsta.horse_colors.client.renderer.HorseGeneticModel;
import sekelsta.horse_colors.client.renderer.HorseGeneticRenderer;

public class ModEntities {
    public static final EntityType<HorseGeneticEntity> HORSE_GENETIC
        = registerEntity("horse_felinoid", HorseGeneticEntity::new, 1.2F, 1.6F);
    public static final EntityType<DonkeyGeneticEntity> DONKEY_GENETIC
        = registerEntity("donkey", DonkeyGeneticEntity::new, 1.2F, 1.6F);
    public static final EntityType<MuleGeneticEntity> MULE_GENETIC
        = registerEntity("mule", MuleGeneticEntity::new, 1.2F, 1.6F);

    public static Item HORSE_SPAWN_EGG;
    public static Item DONKEY_SPAWN_EGG;
    public static Item MULE_SPAWN_EGG;

    private static <T extends AnimalEntity> EntityType<T> registerEntity(
            String name, EntityType.EntityFactory<T> factory, float width, float height) {
        Identifier registryName = new Identifier(HorseColors.MOD_ID, name);
        EntityType<T> type = FabricEntityTypeBuilder.<T>create(SpawnGroup.CREATURE, factory)
            .dimensions(EntityDimensions.changing(width, height))
            .build();
        return Registry.register(Registries.ENTITY_TYPE, registryName, type);
    }

    private static Item registerSpawnEgg(String name, EntityType<? extends net.minecraft.entity.mob.MobEntity> type, int primary, int secondary) {
        Identifier registryName = new Identifier(HorseColors.MOD_ID, name);
        return Registry.register(Registries.ITEM, registryName, new SpawnEggItem(type, primary, secondary, new Item.Settings()));
    }

    public static void register() {
        HORSE_SPAWN_EGG = registerSpawnEgg("horse_spawn_egg", HORSE_GENETIC, 0x7F4320, 0x110E0D);
        DONKEY_SPAWN_EGG = registerSpawnEgg("donkey_spawn_egg", DONKEY_GENETIC, 0x726457, 0xcdc0b5);
        MULE_SPAWN_EGG = registerSpawnEgg("mule_spawn_egg", MULE_GENETIC, 0x4b3a30, 0xcdb9a8);

        FabricDefaultAttributeRegistry.register(HORSE_GENETIC, AbstractHorseEntity.createBaseHorseAttributes());
        FabricDefaultAttributeRegistry.register(DONKEY_GENETIC, AbstractDonkeyEntity.createAbstractDonkeyAttributes());
        FabricDefaultAttributeRegistry.register(MULE_GENETIC, AbstractDonkeyEntity.createAbstractDonkeyAttributes());

        SpawnRestriction.register(HORSE_GENETIC, SpawnRestriction.Location.ON_GROUND, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, AnimalEntity::isValidNaturalSpawn);
        SpawnRestriction.register(DONKEY_GENETIC, SpawnRestriction.Location.ON_GROUND, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, AnimalEntity::isValidNaturalSpawn);

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.SPAWN_EGGS).register(entries -> {
            entries.add(new ItemStack(HORSE_SPAWN_EGG), ItemGroup.StackVisibility.PARENT_AND_SEARCH_TABS);
            entries.add(new ItemStack(DONKEY_SPAWN_EGG), ItemGroup.StackVisibility.PARENT_AND_SEARCH_TABS);
            entries.add(new ItemStack(MULE_SPAWN_EGG), ItemGroup.StackVisibility.PARENT_AND_SEARCH_TABS);
        });
    }

    public static void registerRenderers()
    {
        EntityRendererRegistry.register(HORSE_GENETIC, HorseGeneticRenderer::new);
        EntityRendererRegistry.register(DONKEY_GENETIC, HorseGeneticRenderer::new);
        EntityRendererRegistry.register(MULE_GENETIC, HorseGeneticRenderer::new);

        EntityModelLayerRegistry.registerModelLayer(HorseGeneticRenderer.EQUINE_LAYER, HorseGeneticModel::createBodyLayer);
        EntityModelLayerRegistry.registerModelLayer(HorseArmorLayer.HORSE_ARMOR_LAYER, HorseGeneticModel::createArmorLayer);
    }
}
