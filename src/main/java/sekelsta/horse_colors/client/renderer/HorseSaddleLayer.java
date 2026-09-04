package sekelsta.horse_colors.client.renderer;

import sekelsta.horse_colors.entity.AbstractHorseGenetic;
import sekelsta.horse_colors.entity.HorseGeneticEntity;
import sekelsta.horse_colors.util.HorseArmorer;

import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumers;
import net.minecraft.client.render.entity.model.EntityModelLoader;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.util.Identifier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.feature.FeatureRenderer;

import sekelsta.horse_colors.HorseColors;

@Environment(EnvType.CLIENT)
public class HorseSaddleLayer extends FeatureRenderer<AbstractHorseGenetic, HorseGeneticModel<AbstractHorseGenetic>> {
    private final HorseGeneticModel<AbstractHorseGenetic> horseModel;

    public HorseSaddleLayer(FeatureRendererContext<AbstractHorseGenetic, HorseGeneticModel<AbstractHorseGenetic>> model, EntityModelLoader modelLoader) {
        super(model);
        this.horseModel = new HorseGeneticModel<>(modelLoader.getModelPart(HorseGeneticRenderer.EQUINE_LAYER));
    }

    @Override
    public void render(MatrixStack matrixStack, VertexConsumerProvider renderTypeBuffer, int packedLight, AbstractHorseGenetic entityIn, float f1, float f2, float f3, float f4, float f5, float f6) {
        Identifier textureLocation = HorseArmorer.getSaddleTexture(entityIn);
        int color = HorseArmorer.getSaddleTint(entityIn);
        if (textureLocation != null) {
            this.getContextModel().copyStateTo(this.horseModel);
            this.horseModel.animateModel(entityIn, f1, f2, f3);
            this.horseModel.setAngles(entityIn, f1, f2, f4, f5, f6);
            float r = (float)(color >> 16 & 255) / 255.0F;
            float g = (float)(color >> 8 & 255) / 255.0F;
            float b = (float)(color & 255) / 255.0F;

            VertexConsumer ivertexbuilder;
            if (HorseArmorer.isSaddleEnchanted(entityIn)) {
                ivertexbuilder = VertexConsumers.union(renderTypeBuffer.getBuffer(RenderLayer.getArmorEntityGlint()), renderTypeBuffer.getBuffer(RenderLayer.getArmorCutoutNoCull(textureLocation)));
            }
            else {
                ivertexbuilder = renderTypeBuffer.getBuffer(RenderLayer.getEntityCutoutNoCull(textureLocation));
            }
            this.horseModel.render(matrixStack, ivertexbuilder, packedLight, OverlayTexture.DEFAULT_UV, r, g, b, 1.0F);
        }
    }


    public boolean shouldCombineTextures() {
        return false;
    }
}
