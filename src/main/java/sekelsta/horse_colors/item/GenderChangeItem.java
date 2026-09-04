package sekelsta.horse_colors.item;

import java.util.List;
import org.jetbrains.annotations.Nullable;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import sekelsta.horse_colors.HorseConfig;
import sekelsta.horse_colors.entity.genetics.IGeneticEntity;
import sekelsta.horse_colors.HorseColors;

public class GenderChangeItem extends Item {
    public GenderChangeItem(Item.Settings builder) {
        super(builder);
    }

    @Override
    public ActionResult useOnEntity(ItemStack stack, PlayerEntity player, LivingEntity target, Hand hand) {
        if (target instanceof IGeneticEntity) {
            IGeneticEntity g = (IGeneticEntity)target;
            g.setMale(!g.isMale());
            if (player != null) {
                target.getWorld().playSound((PlayerEntity)null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENTITY_SPLASH_POTION_THROW, SoundCategory.PLAYERS, 0.5F, 0.4F / (target.getWorld().getRandom().nextFloat() * 0.4F + 0.8F));
            }
            if (player == null || !player.getAbilities().creativeMode) {
                stack.decrement(1);
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

    /**
     * allows items to add custom lines of information to the mouseover description
     */
    @Override
    @Environment(EnvType.CLIENT)
    public void appendTooltip(ItemStack stack, @Nullable World worldIn, List<Text> tooltip, TooltipContext flagIn) {
        if (!HorseConfig.isGenderEnabled()) {
            String translation = HorseColors.MOD_ID + ".gender_change_item.gender_disabled_warning";
            tooltip.add(Text.translatable(translation).formatted(Formatting.GRAY));
        }
    }

}
