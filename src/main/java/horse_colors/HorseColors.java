package horse_colors;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HorseColors implements ModInitializer {
    public static final String MOD_ID = "horse_colors";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("Realistic Horse Genetics: initializing Fabric port");
    }
}
