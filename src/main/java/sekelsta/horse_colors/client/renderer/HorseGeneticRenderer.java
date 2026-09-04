package sekelsta.horse_colors.client.renderer;

import net.minecraft.client.util.math.MatrixStack;
import com.google.common.collect.Maps;
import java.util.Map;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.util.Identifier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import sekelsta.horse_colors.HorseColors;
import sekelsta.horse_colors.entity.AbstractHorseGenetic;
import sekelsta.horse_colors.entity.genetics.IGeneticEntity;
import sekelsta.horse_colors.entity.genetics.HorseColorCalculator;

// Can't inherit from AbstractHorseRenderer because that uses HorseModel
@Environment(EnvType.CLIENT)
public class HorseGeneticRenderer extends MobEntityRenderer<AbstractHorseGenetic, HorseGeneticModel<AbstractHorseGenetic>>
{
    private static final Map<TextureLayer, Identifier> LAYERED_LOCATION_CACHE = Maps.newHashMap();
    public static final EntityModelLayer EQUINE_LAYER = new EntityModelLayer(new Identifier(HorseColors.MOD_ID, "equine"), "equine");

    public HorseGeneticRenderer(EntityRendererFactory.Context renderManager)
    {
        super(renderManager, new HorseGeneticModel<AbstractHorseGenetic>(renderManager.getPart(EQUINE_LAYER)), 0.75F);
        this.addFeature(new HorseSaddleLayer(this, renderManager.getModelLoader()));
        this.addFeature(new HorseArmorLayer(this, renderManager.getModelLoader()));
    }

    @Override
    protected void scale(AbstractHorseGenetic horse, MatrixStack matrixStackIn, float partialTickTime) {
        float scale = horse.getProportionalAgeScale();
        matrixStackIn.scale(scale, scale, scale);
        this.shadowRadius = 0.75F * scale;
        super.scale(horse, matrixStackIn, partialTickTime);
    }

    /**
     * Returns the location of an entity's texture. Doesn't seem to be called unless you call EntityRenderer.bindEntityTexture.
     */
    @Override
    public Identifier getTexture(AbstractHorseGenetic entity)
    {
        if (entity instanceof IGeneticEntity) {
            TextureLayer l = ((IGeneticEntity)entity).getGenome().getTexturePaths();
            Identifier resourcelocation = LAYERED_LOCATION_CACHE.get(l);

            if (resourcelocation == null)
            {
                resourcelocation = new Identifier(l.getUniqueName());
                MinecraftClient.getInstance().getTextureManager().registerTexture(
                    resourcelocation,
                    new CustomLayeredTexture(((IGeneticEntity)entity).getGenome().getTexturePaths())
                );
                LAYERED_LOCATION_CACHE.put(l, resourcelocation);
            }

            return resourcelocation;
        }
        System.out.println("Trying to render an ineligible entity");
        return null;
    }
}
