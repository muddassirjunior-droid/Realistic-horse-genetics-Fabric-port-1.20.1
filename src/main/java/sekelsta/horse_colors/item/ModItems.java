package sekelsta.horse_colors.item;

import net.minecraft.block.dispenser.FallibleItemDispenserBehavior;
import net.minecraft.block.DispenserBlock;
import net.minecraft.entity.passive.AbstractHorseEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPointer;
import net.minecraft.util.math.Box;

import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;

import sekelsta.horse_colors.HorseColors;
import sekelsta.horse_colors.entity.ModEntities;

public class ModItems {
    public static final GeneBookItem geneBookItem = register("gene_book",
        new GeneBookItem((new Item.Settings()).maxCount(1))
    );
    public static final GenderChangeItem genderChangePotion = register("gender_change_item",
        new GenderChangeItem((new Item.Settings()).maxCount(64))
    );
    public static final FertilityPotion fertilityPotion = register("fertility_potion",
        new FertilityPotion((new Item.Settings()).maxCount(64), true)
    );
    public static final FertilityPotion infertilityPotion = register("infertility_potion",
        new FertilityPotion((new Item.Settings()).maxCount(64), false)
    );
    public static final CompatibleHorseArmor netheriteHorseArmor = register("netherite_horse_armor",
        new CompatibleHorseArmor(13, "netherite", (new Item.Settings()).maxCount(1).fireproof())
    );

    private static <T extends Item> T register(String name, T item) {
        return Registry.register(Registries.ITEM, new Identifier(HorseColors.MOD_ID, name), item);
    }

    public static void registerDispenseBehaviour() {
        FallibleItemDispenserBehavior dispenseHorseArmor = new FallibleItemDispenserBehavior() {
            /**
             * Dispense the specified stack, play the dispense sound and spawn particles.
             */
            @Override
            protected ItemStack dispenseSilently(BlockPointer source, ItemStack stack) {
                net.minecraft.util.math.BlockPos blockpos = source.getPos().offset(source.getBlockState().get(DispenserBlock.FACING));

                for (AbstractHorseEntity abstracthorseentity : source.getWorld().getEntitiesByClass(AbstractHorseEntity.class, new Box(blockpos), (horse) -> {
                    return horse.isAlive();
                })) {
                    // stack is always this same netherite horse armor item here, so a plain type
                    // check stands in for the Forge-only AbstractHorse.isArmor(stack) hook
                    if (stack.getItem() instanceof net.minecraft.item.HorseArmorItem && !abstracthorseentity.hasArmorInSlot() && abstracthorseentity.isTame()) {
                        abstracthorseentity.getStackReference(401).set(stack.split(1));
                        this.setSuccess(true);
                        return stack;
                    }
                }

                return super.dispenseSilently(source, stack);
            }
        };
        DispenserBlock.registerBehavior(netheriteHorseArmor, dispenseHorseArmor);
    }

    public static void register() {
        registerDispenseBehaviour();

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.TOOLS).register(entries -> {
            entries.add(new ItemStack(genderChangePotion), ItemGroup.StackVisibility.PARENT_AND_SEARCH_TABS);
            entries.add(new ItemStack(fertilityPotion), ItemGroup.StackVisibility.PARENT_AND_SEARCH_TABS);
            entries.add(new ItemStack(infertilityPotion), ItemGroup.StackVisibility.PARENT_AND_SEARCH_TABS);
        });
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.COMBAT).register(entries -> {
            entries.add(new ItemStack(netheriteHorseArmor), ItemGroup.StackVisibility.PARENT_AND_SEARCH_TABS);
        });
    }
}
