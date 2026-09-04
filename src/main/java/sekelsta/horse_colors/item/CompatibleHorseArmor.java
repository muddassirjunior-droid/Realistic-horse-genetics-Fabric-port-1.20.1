package sekelsta.horse_colors.item;

import net.minecraft.item.HorseArmorItem;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;

import sekelsta.horse_colors.HorseColors;

// Horse armor that has a separate texture for the 1.12 horse model used by
// this mod and the 1.14 horse model
public class CompatibleHorseArmor extends HorseArmorItem {
    private String armorName;
    // This texture fits the 1.12 horse model used by the modded horses
    private Identifier alternateTexture;
    // The vanilla-model texture this item would use without our compat override
    private Identifier vanillaTexture;

    public CompatibleHorseArmor(int armorValue, String armorName, Item.Settings builder) {
        super(armorValue, armorName, builder);
        this.armorName = armorName;
        this.vanillaTexture = new Identifier(HorseColors.MOD_ID, "textures/entity/vanillahorse/armor/horse_armor_" + armorName + ".png");
        this.alternateTexture = new Identifier(HorseColors.MOD_ID, "textures/entity/horse/armor/horse_armor_" + armorName + ".png");
    }

    @Override
    @Environment(EnvType.CLIENT)
    public Identifier getEntityTexture() {
        if (FabricLoader.getInstance().isModLoaded("familiarhorses")) {
            return getAlternateTexture();
        }
        return vanillaTexture;
    }

    @Environment(EnvType.CLIENT)
    public Identifier getAlternateTexture() {
        return alternateTexture;
    }
}
