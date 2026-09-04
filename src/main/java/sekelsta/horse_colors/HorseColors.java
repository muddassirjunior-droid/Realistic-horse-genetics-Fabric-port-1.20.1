package sekelsta.horse_colors;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sekelsta.horse_colors.breed.BreedManager;
import sekelsta.horse_colors.entity.ModEntities;
import sekelsta.horse_colors.item.ModItems;
import sekelsta.horse_colors.network.HorsePacketHandler;
import sekelsta.horse_colors.world.Spawns;

public class HorseColors implements ModInitializer {
    public static final String MOD_ID = "horse_colors";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("Realistic Horse Genetics: initializing Fabric port");

        // Must run before any other registration: several classes read config values
        // in static initializers, which run the first time those classes are touched.
        HorseConfig.spec.load(FabricLoader.getInstance().getConfigDir().resolve("horse_colors.json"));

        ModEntities.register();
        ModItems.register();
        BreedManager.register();
        HorsePacketHandler.registerPackets();
        Spawns.registerBiomeModifiers();
    }
}
