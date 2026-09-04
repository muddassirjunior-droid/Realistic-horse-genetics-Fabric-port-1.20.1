package sekelsta.horse_colors.mixin;

import net.minecraft.block.entity.BrewingStandBlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import sekelsta.horse_colors.item.InfertilityPotionBrewingRecipe;
import sekelsta.horse_colors.item.ModItems;

/**
 * Adds the Awkward Potion + Poisonous Potato -> Infertility Potion recipe.
 * Vanilla's BrewingRecipeRegistry only supports potion-to-potion transforms (same
 * PotionItem container, different Potion/Item type) - it has no way to express
 * "consume the bottle entirely and produce an unrelated item", which is what this
 * recipe needs, so it can't be registered through the vanilla registry the way
 * Forge's IBrewingRecipe extension point allowed.
 */
@Mixin(BrewingStandBlockEntity.class)
public class BrewingStandBlockEntityMixin {

    @Inject(method = "canCraft", at = @At("HEAD"), cancellable = true)
    private static void horse_colors$canCraftInfertility(DefaultedList<ItemStack> slots, CallbackInfoReturnable<Boolean> cir) {
        if (!slots.get(3).isEmpty() && InfertilityPotionBrewingRecipe.isIngredient(slots.get(3))) {
            for (int i = 0; i < 3; i++) {
                if (!slots.get(i).isEmpty() && InfertilityPotionBrewingRecipe.isInput(slots.get(i))) {
                    cir.setReturnValue(true);
                    return;
                }
            }
        }
    }

    @Inject(method = "craft", at = @At("HEAD"))
    private static void horse_colors$craftInfertility(World world, BlockPos pos, DefaultedList<ItemStack> slots, CallbackInfo ci) {
        if (slots.get(3).isEmpty() || !InfertilityPotionBrewingRecipe.isIngredient(slots.get(3))) {
            return;
        }

        // Replace matching slots before vanilla's craft() body runs; BrewingRecipeRegistry.craft
        // finds no recipe for an Infertility Potion stack and returns it unchanged, so this is
        // safe to let vanilla's per-slot loop, ingredient decrement, and brew-event sound run as-is.
        for (int i = 0; i < 3; i++) {
            ItemStack potionStack = slots.get(i);
            if (!potionStack.isEmpty() && InfertilityPotionBrewingRecipe.isInput(potionStack)) {
                slots.set(i, new ItemStack(ModItems.infertilityPotion));
            }
        }
    }

    @Inject(method = "isValid", at = @At("HEAD"), cancellable = true)
    private void horse_colors$isValidInfertilityIngredient(int slot, ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (slot == 3 && InfertilityPotionBrewingRecipe.isIngredient(stack)) {
            cir.setReturnValue(true);
        }
    }
}
