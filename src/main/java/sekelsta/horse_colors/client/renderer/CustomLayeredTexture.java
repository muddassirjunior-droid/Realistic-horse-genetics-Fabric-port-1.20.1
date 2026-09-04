package sekelsta.horse_colors.client.renderer;

import net.minecraft.client.texture.AbstractTexture;
import com.mojang.blaze3d.systems.RenderSystem;
import java.io.IOException;
import net.minecraft.resource.ResourceManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import net.minecraft.client.texture.NativeImage;
import com.mojang.blaze3d.platform.TextureUtil;

@Environment(EnvType.CLIENT)
public class CustomLayeredTexture extends AbstractTexture {
    public final TextureLayerGroup layerGroup;

    public CustomLayeredTexture(TextureLayerGroup layers) {
        this.layerGroup = layers;
        if (this.layerGroup.layers.isEmpty()) {
            throw new IllegalStateException("Layered texture with no layers.");
        }
    }

    @Override
    public void load(ResourceManager manager) throws IOException {
        NativeImage image = layerGroup.getImage(manager);

        if (!RenderSystem.isOnRenderThreadOrInit()) {
            RenderSystem.recordRenderCall(() -> {
                this.loadImage(image);
            });
        } else {
            this.loadImage(image);
        }
   }

   private void loadImage(NativeImage imageIn) {
      TextureUtil.prepareImage(this.getGlId(), imageIn.getWidth(), imageIn.getHeight());
      imageIn.upload(0, 0, 0, true);
   }


}
