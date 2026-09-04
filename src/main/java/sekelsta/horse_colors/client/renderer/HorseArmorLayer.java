package sekelsta.horse_colors.client.renderer;

import sekelsta.horse_colors.entity.AbstractHorseGenetic;
import sekelsta.horse_colors.entity.HorseGeneticEntity;
import sekelsta.horse_colors.util.HorseArmorer;

import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumers;
import net.minecraft.block.DyedCarpetBlock;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.client.render.entity.model.EntityModelLoader;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.item.BlockItem;
import net.minecraft.item.DyeableHorseArmorItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.feature.FeatureRenderer;

import sekelsta.horse_colors.HorseColors;

@Environment(EnvType.CLIENT)
public class HorseArmorLayer extends FeatureRenderer<AbstractHorseGenetic, HorseGeneticModel<AbstractHorseGenetic>> {
    public static final EntityModelLayer HORSE_ARMOR_LAYER = new EntityModelLayer(new Identifier(HorseColors.MOD_ID, "horse_armor"), "horse_armor");
    private final HorseGeneticModel<AbstractHorseGenetic> horseModel;

    public HorseArmorLayer(FeatureRendererContext<AbstractHorseGenetic, HorseGeneticModel<AbstractHorseGenetic>> model, EntityModelLoader modelLoader) {
        super(model);
        this.horseModel = new HorseGeneticModel<>(modelLoader.getModelPart(HORSE_ARMOR_LAYER));
    }

    @Override
    public void render(MatrixStack matrixStack, VertexConsumerProvider renderTypeBuffer, int packedLight, AbstractHorseGenetic entityIn, float f1, float f2, float f3, float f4, float f5, float f6) {
        ItemStack itemstack = entityIn.getArmorItem();
        Item armor = itemstack.getItem();
        Identifier textureLocation = HorseArmorer.getTexture(armor);
        if (textureLocation != null) {
            this.getContextModel().copyStateTo(this.horseModel);
            this.horseModel.animateModel(entityIn, f1, f2, f3);
            this.horseModel.setAngles(entityIn, f1, f2, f4, f5, f6);
            float r = 1.0F;
            float g = 1.0F;
            float b = 1.0F;
            if (armor instanceof DyeableHorseArmorItem) {
                int color = ((DyeableHorseArmorItem)armor).getColor(itemstack);
                r = (float)(color >> 16 & 255) / 255.0F;
                g = (float)(color >> 8 & 255) / 255.0F;
                b = (float)(color & 255) / 255.0F;
            }
            else if (armor instanceof BlockItem) {
                BlockItem blockItem = (BlockItem)armor;
                if (blockItem.getBlock() instanceof DyedCarpetBlock) {
                    float[] colors = ((DyedCarpetBlock)(blockItem.getBlock())).getDyeColor().getColorComponents();
                    r = colors[0];
                    g = colors[1];
                    b = colors[2];
                }
            }

            VertexConsumer ivertexbuilder;
            if (itemstack.hasGlint()) {
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
