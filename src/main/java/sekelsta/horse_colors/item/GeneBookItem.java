package sekelsta.horse_colors.item;

import java.util.List;
import java.util.UUID;
import org.jetbrains.annotations.Nullable;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.StringHelper;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import sekelsta.horse_colors.HorseColors;
import sekelsta.horse_colors.client.GeneBookScreen;
import sekelsta.horse_colors.entity.*;
import sekelsta.horse_colors.entity.genetics.Genome;
import sekelsta.horse_colors.entity.genetics.EquineGenome;
import sekelsta.horse_colors.entity.genetics.Species;

public class GeneBookItem extends Item {
    public GeneBookItem(Item.Settings properties) {
        super(properties);
    }

    public static boolean validBookTagContents(NbtCompound nbt) {
        if (nbt == null) {
            return false;
        }
        // 8 is string type
        if (!nbt.contains("species", 8)) {
            return false;
        }
        if (!nbt.contains("genes", 8)) {
            return false;
        }
        try {
            Species sp = Species.valueOf(nbt.getString("species"));
        }
        catch (IllegalArgumentException e) {
            return false;
        }
        return true;
    }

    public static Species getSpecies(NbtCompound compoundnbt) {
        String s = compoundnbt.getString("species");
        if (!StringHelper.isEmpty(s)) {
            return Species.valueOf(s);
        }
        return null;
    }

    /**
     * allows items to add custom lines of information to the mouseover description
     */
    @Override
    @Environment(EnvType.CLIENT)
    public void appendTooltip(ItemStack stack, @Nullable World worldIn, List<Text> tooltip, TooltipContext flagIn) {
        if (stack.hasNbt()) {
            NbtCompound compoundnbt = stack.getNbt();
            Species species = getSpecies(compoundnbt);
            if (species != null) {
                String translation = null;
                switch (species) {
                    case HORSE:
                        translation = ModEntities.HORSE_GENETIC.getTranslationKey();
                        break;
                    case DONKEY:
                        translation = ModEntities.DONKEY_GENETIC.getTranslationKey();
                        break;
                    case MULE:
                        translation = ModEntities.MULE_GENETIC.getTranslationKey();
                        break;
                }
                if (translation != null) {
                    // Compare to the author name on written books
                    tooltip.add(Text.translatable(translation).formatted(Formatting.GRAY));
                }
            }
        }
    }

    /**
     * Called to trigger the item's "innate" right click behavior. To handle when this item is used on a Block, see
     * {@link #onItemUse}.
     */
    @Override
    public TypedActionResult<ItemStack> use(World worldIn, PlayerEntity playerIn, Hand handIn) {
        ItemStack itemstack = playerIn.getStackInHand(handIn);
        if (validBookTagContents(itemstack.getNbt())) {
            if (worldIn.isClient()) {
                openGeneBook(itemstack.getNbt());
            }
            return TypedActionResult.success(itemstack);
        }
        HorseColors.LOGGER.error("Gene book has invalid NBT");
        return TypedActionResult.fail(itemstack);
    }

    @Override
    public ActionResult useOnEntity(ItemStack stack, PlayerEntity player, LivingEntity target, Hand hand) {
        if (stack.getNbt() == null) {
            // Most likely someone summoned this item by command without data
            HorseColors.LOGGER.error("Gene book has no NBT data");
            return ActionResult.FAIL;
        }
        // Check that this itemstack has a UUID
        if (!stack.getNbt().containsUuid("EntityUUID")) {
            return ActionResult.PASS;
        }
        // Get this itemstack's entity's UUID
        UUID entityUUID = stack.getNbt().getUuid("EntityUUID");
        // Null check that probably shouldn't be needed
        if (entityUUID == null) {
            return ActionResult.PASS;
        }
        // Check that the entity matches
        if (!(entityUUID.equals(target.getUuid()))) {
            return ActionResult.PASS;
        }
        // Make changes based on the entity
        if (target.hasCustomName()) {
            stack.setCustomName(target.getCustomName());
        }
        else {
            stack.removeCustomName();
        }
        return ActionResult.success(player.getWorld().isClient);
    }


    @Environment(EnvType.CLIENT)
    public void openGeneBook(NbtCompound nbt) {
        MinecraftClient mc = MinecraftClient.getInstance();
        Genome genome = new EquineGenome(getSpecies(nbt));
        genome.genesFromString(nbt.getString("genes"));
        mc.setScreen(new GeneBookScreen(genome));
    }
}
