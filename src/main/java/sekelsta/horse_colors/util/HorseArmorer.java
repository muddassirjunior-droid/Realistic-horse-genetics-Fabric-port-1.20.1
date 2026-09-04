package sekelsta.horse_colors.util;

import net.minecraft.block.DyedCarpetBlock;
import net.minecraft.item.BlockItem;
import net.minecraft.item.HorseArmorItem;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import sekelsta.horse_colors.HorseColors;
import sekelsta.horse_colors.entity.AbstractHorseGenetic;
import sekelsta.horse_colors.entity.genetics.HorseColorCalculator;
import sekelsta.horse_colors.item.CompatibleHorseArmor;

public class HorseArmorer
{
    private static boolean shouldOverwriteResource(Identifier location) {
        if (location == null) {
            return false;
        }
        String namespace = location.getNamespace();
        return namespace.equals("minecraft") || namespace.equals("byg");
    }

    @Environment(EnvType.CLIENT)
    public static Identifier getTexture(Item armor)
    {
        if (armor instanceof CompatibleHorseArmor) {
            return ((CompatibleHorseArmor)armor).getAlternateTexture();
        }
        if (armor instanceof HorseArmorItem) {
            Identifier textureLocation = ((HorseArmorItem)armor).getEntityTexture();
            // Only use my own version of textures in the minecraft namespace
            if (shouldOverwriteResource(textureLocation)) {
                return new Identifier(HorseColors.MOD_ID, textureLocation.getPath());
            }

            return textureLocation;
        }
        if (armor instanceof BlockItem) {
            if (((BlockItem)armor).getBlock() instanceof DyedCarpetBlock) {
                return new Identifier(HorseColorCalculator.fixPath("armor/carpet"));
            }
        }
        return null;
    }

    @Environment(EnvType.CLIENT)
    public static Identifier getSaddleTexture(AbstractHorseGenetic horse) {
        if (!horse.isSaddled()) {
            return null;
        }
        return new Identifier(HorseColorCalculator.fixPath("saddle"));
    }

    @Environment(EnvType.CLIENT)
    public static int getSaddleTint(AbstractHorseGenetic horse) {
        return 0xffffff;
    }

    @Environment(EnvType.CLIENT)
    public static boolean isSaddleEnchanted(AbstractHorseGenetic horse) {
        return false;
    }
}
