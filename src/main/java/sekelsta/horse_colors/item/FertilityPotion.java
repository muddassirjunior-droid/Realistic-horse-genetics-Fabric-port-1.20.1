package sekelsta.horse_colors.item;

import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsage;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;

import sekelsta.horse_colors.entity.genetics.IGeneticEntity;

public class FertilityPotion extends Item {
    protected final boolean value;

    public FertilityPotion(Item.Settings builder, boolean value) {
        super(builder);
        this.value = value;
    }

    @Override
    public ActionResult useOnEntity(ItemStack stack, PlayerEntity player, LivingEntity target, Hand hand) {
        if (target instanceof IGeneticEntity) {
            IGeneticEntity g = (IGeneticEntity)target;
            if (g.isFertile() == value) {
                return ActionResult.PASS;
            }
            g.setFertile(value);
            if (g.isFertile() != value) {
                // Don't use on mules
                return ActionResult.PASS;
            }
            if (player != null) {
                target.getWorld().playSound((PlayerEntity)null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENTITY_SPLASH_POTION_THROW, SoundCategory.PLAYERS, 0.5F, 0.4F / (target.getWorld().getRandom().nextFloat() * 0.4F + 0.8F));
            }
            if (player == null || !player.getAbilities().creativeMode) {
                player.setStackInHand(hand, ItemUsage.exchangeStack(stack, player, new ItemStack(Items.GLASS_BOTTLE)));
            }
            return ActionResult.success(player.getWorld().isClient);
        }
        return ActionResult.PASS;
    }

  /**
    * Returns true if this item has an enchantment glint. By default, this returns <code>stack.isItemEnchanted()</code>,
    * but other items can override it (for instance, written books always return true).
    *
    * Note that if you override this method, you generally want to also call the super version (on {@link Item}) to get
    * the glint for enchanted items. Of course, that is unnecessary if the overwritten version always returns true.
    */
    @Override
    public boolean hasGlint(ItemStack stack) {
        return true;
    }
}
