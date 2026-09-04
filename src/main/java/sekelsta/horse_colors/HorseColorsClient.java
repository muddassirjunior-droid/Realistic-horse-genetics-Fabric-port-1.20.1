package sekelsta.horse_colors;

import net.fabricmc.api.ClientModInitializer;

import sekelsta.horse_colors.entity.ModEntities;

public class HorseColorsClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ModEntities.registerRenderers();
    }
}
