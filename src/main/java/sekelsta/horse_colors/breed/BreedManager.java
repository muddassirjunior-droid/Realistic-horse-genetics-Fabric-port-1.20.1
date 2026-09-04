package sekelsta.horse_colors.breed;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.*;
import net.minecraft.resource.JsonDataLoader;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.util.Identifier;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.resource.ResourceType;

import sekelsta.horse_colors.HorseColors;
import sekelsta.horse_colors.entity.genetics.EquineGenome.Gene;
import sekelsta.horse_colors.breed.horse.*;

public class BreedManager extends JsonDataLoader implements IdentifiableResourceReloadListener {
    private static final Gson GSON = (new GsonBuilder()).setPrettyPrinting().disableHtmlEscaping().create();

    private Map<Identifier, Breed<Gene>> breeds;
    private final Identifier fabricId;

    public static final BreedManager HORSE = new BreedManager("horse");
    public static final BreedManager DONKEY = new BreedManager("donkey");

    protected BreedManager(String path) {
        super(GSON, "breeds/" + path);
        this.fabricId = new Identifier(HorseColors.MOD_ID, "breeds/" + path);
    }

    @Override
    protected void apply(Map<Identifier, JsonElement> mapIn, ResourceManager resourceManagerIn, Profiler profilerIn) {
        breeds = new HashMap<>();
        for (Identifier key : mapIn.keySet()) {
            // Forge uses names starting with _ for metadata
            if (key.getPath().startsWith("_")) {
                continue;
            }
            try {
                // Possible IllegalStateException will be caught
                JsonObject json = mapIn.get(key).getAsJsonObject();
                Breed<Gene> b = deserializeBreed(json, key.getPath());
                if (b != null) {
                    breeds.put(key, b);
                }
            }
            catch (IllegalStateException e) {
                HorseColors.LOGGER.error("Could not parse json: " + key);
            }
            catch (ClassCastException e) {
                HorseColors.LOGGER.error("Unexpected data type in json: " + key);
            }
            HorseColors.LOGGER.debug("Loaded " + breeds.size()
                + " breed data files");
        }
        postProcess();
    }

    @Override
    public Identifier getFabricId() {
        return fabricId;
    }

    public static void register() {
        ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(HORSE);
        ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(DONKEY);
    }

    private static Breed<Gene> deserializeBreed(JsonObject json, String name)
        throws ClassCastException, IllegalStateException
    {
        Breed<Gene> breed = new Breed<>(Gene.class);
        breed.name = name;
        if (json.has("genes")) {
            JsonObject genesJson = (JsonObject)json.get("genes");
            for (Map.Entry<String, JsonElement> entry : genesJson.entrySet()) {
                JsonArray jarray = entry.getValue().getAsJsonArray();
                ArrayList<Float> frequencies = new ArrayList<>();
                for (int i = 0; i < jarray.size(); ++i) {
                    frequencies.add(jarray.get(i).getAsFloat());
                }
                breed.genes.put(Gene.valueOf(entry.getKey()), frequencies);
            }
        }
        if (json.has("population")) {
            breed.population = json.get("population").getAsInt();
        }
        return breed;
    }

    private void postProcess() {
        // TODO: find a less ugly way
        if (this == HORSE) {
            Breed<Gene> mongolianHorse = getBreed("mongolian_horse");
            mongolianHorse.parent = Tarpan.breed;

            Breed<Gene> hucul = getBreed("hucul");
            hucul.parent = mongolianHorse;

            Breed<Gene> quarterHorse = getBreed("quarter_horse");
            quarterHorse.parent = mongolianHorse;

            Breed<Gene> defaultHorse = getBreed("default_horse");
            defaultHorse.parent = mongolianHorse;

            Breed<Gene> clevelandBay = getBreed("cleveland_bay");
            clevelandBay.parent = quarterHorse;

            Breed<Gene> appaloosa = getBreed("appaloosa");
            appaloosa.parent = quarterHorse;

            Breed<Gene> fjord = getBreed("fjord");
            fjord.parent = quarterHorse;

            Breed<Gene> friesian = getBreed("friesian");
            friesian.parent = mongolianHorse;
        }
        else if (this == DONKEY) {
            Breed<Gene> defaultDonkey = getBreed("default_donkey");
            defaultDonkey.parent = BaseDonkey.breed;

            Breed<Gene> large = getBreed("large_donkey");
            large.parent = defaultDonkey;

            Breed<Gene> mini = getBreed("miniature_donkey");
            mini.parent = defaultDonkey;
        }

    }

    public Breed<Gene> getBreed(Identifier name) {
        return breeds.get(name);
    }

    public Breed<Gene> getBreed(String name) {
        return getBreed(new Identifier(HorseColors.MOD_ID, name));
    }

    public Collection<Breed<Gene>> getAllBreeds() {
        return breeds.values();
    }
}
