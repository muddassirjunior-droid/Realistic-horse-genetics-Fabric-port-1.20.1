package sekelsta.horse_colors.item;

import net.minecraft.item.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potions;
import net.minecraft.potion.PotionUtil;

/**
 * Recipe logic used by BrewingStandBlockEntityMixin. Not a vanilla/Fabric
 * interface - there's no built-in "custom item output" brewing hook to
 * implement, see the mixin's comment.
 */
public class InfertilityPotionBrewingRecipe {
    /**
     * Returns true is the passed ItemStack is an input for this recipe. "Input"
     * being the item that goes in one of the three bottom slots of the brewing
     * stand (e.g: water bottle)
     */
    public static boolean isInput(ItemStack input) {
        return PotionUtil.getPotion(input) == Potions.AWKWARD;
    }

    /**
     * Returns true if the passed ItemStack is an ingredient for this recipe.
     * "Ingredient" being the item that goes in the top slot of the brewing
     * stand (e.g: nether wart)
     */
    public static boolean isIngredient(ItemStack ingredient) {
        return ingredient.getItem() == Items.POISONOUS_POTATO;
    }
}
