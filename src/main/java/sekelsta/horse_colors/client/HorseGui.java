package sekelsta.horse_colors.client;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.ingame.HorseScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.util.Identifier;
import net.minecraft.text.Text;
import net.minecraft.entity.passive.AbstractDonkeyEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.HorseScreenHandler;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import sekelsta.horse_colors.HorseConfig;
import sekelsta.horse_colors.entity.AbstractHorseGenetic;
import sekelsta.horse_colors.entity.HorseGeneticEntity;
import sekelsta.horse_colors.HorseColors;


@Environment(EnvType.CLIENT)
public class HorseGui extends HorseScreen {
    private static final Identifier TEXTURE_LOCATION = new Identifier(HorseColors.MOD_ID, "textures/gui/horse.png");
    // The horse whose inventory is currently being accessed.
    // This is a copy of super's private entity field for access without reflection.
    private final AbstractHorseGenetic horseGenetic;
    private final int autobreedIconWidth = 12;
    private final int autobreedIconHeight = 10;
    private int autobreedRenderX;
    private int autobreedRenderY;

    public HorseGui(HorseScreenHandler container, PlayerInventory playerInventory, AbstractHorseGenetic horse) {
        super(container, playerInventory, horse);
        this.horseGenetic = horse;
    }

   /**
    * Draws the background layer of this container (behind the items).
    */
    @Override
    protected void drawBackground(DrawContext guiGraphics, float partialTicks, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexProgram);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, TEXTURE_LOCATION);
        int i = (this.width - this.backgroundWidth) / 2;
        int j = (this.height - this.backgroundHeight) / 2;
        guiGraphics.drawTexture(TEXTURE_LOCATION, i, j, 0, 0, this.backgroundWidth, this.backgroundHeight);
        if (this.horseGenetic instanceof AbstractDonkeyEntity) {
            AbstractDonkeyEntity abstractchestedhorseentity = (AbstractDonkeyEntity)this.horseGenetic;
            if (abstractchestedhorseentity.hasChest()) {
                guiGraphics.drawTexture(TEXTURE_LOCATION, i + 79, j + 17, 0, this.backgroundHeight, abstractchestedhorseentity.getInventoryColumns() * 18, 54);
            }
        }

        if (this.horseGenetic.canBeSaddled()) {
            guiGraphics.drawTexture(TEXTURE_LOCATION, i + 7, j + 35 - 18, 18, this.backgroundHeight + 54, 18, 18);
        }

        if (this.horseGenetic instanceof HorseGeneticEntity) {
            // Draw the armor slot
            guiGraphics.drawTexture(TEXTURE_LOCATION, i + 7, j + 35, 0, this.backgroundHeight + 54, 18, 18);
        }
        else {
            // Draw carpet slot
            guiGraphics.drawTexture(TEXTURE_LOCATION, i + 7, j + 35, 36, this.backgroundHeight + 54, 18, 18);
        }

        int genderIconRenderX = i + 168;
        if (HorseConfig.isGenderEnabled()) {
            int iconWidth = 10;
            int iconHeight = 11;
            int textureX = 176;
            genderIconRenderX -= iconWidth + 1;
            int renderY = j + 4;
            if (this.horseGenetic.isMale()) {
                textureX += iconWidth;
            }
            int textureY = 0;
            boolean grayIcons = HorseConfig.COMMON.useGeneticAnimalsIcons.get();
            if (grayIcons) {
                textureX += 2 * iconWidth;
            }
            if (!horseGenetic.isFertile()) {
                textureY = 11;
            }
            // Render pregnancy progress bar
            if (this.horseGenetic.isPregnant() && !grayIcons) {
                genderIconRenderX -= 2;
                int pregRenderX = genderIconRenderX + iconWidth + 1;
                // Blit pregnancy background
                guiGraphics.drawTexture(TEXTURE_LOCATION, pregRenderX, renderY + 1, 181, 23, 2, 10);
                // Blit pregnancy foreground based on progress
                int pregnantAmount = (int)(11 * horseGenetic.getPregnancyProgress());
                guiGraphics.drawTexture(TEXTURE_LOCATION, pregRenderX, renderY + 11 - pregnantAmount, 177, 33 - pregnantAmount, 2, pregnantAmount);
            }
            // Blit gender icon
            // X, y to render to, x, y to render from, width, height
            guiGraphics.drawTexture(TEXTURE_LOCATION, genderIconRenderX, renderY, textureX, textureY, iconWidth, iconHeight);

            // Render genetic animals pregnancy progress indicator
            if (this.horseGenetic.isPregnant() && grayIcons) {
                // Blit pregnancy foreground based on progress
                int pregnantAmount = (int)(10 * horseGenetic.getPregnancyProgress()) + 1;
                guiGraphics.drawTexture(TEXTURE_LOCATION, genderIconRenderX, renderY + 11 - pregnantAmount, textureX, iconHeight + 22 - pregnantAmount, iconWidth, pregnantAmount);
            }
        }
        if (HorseConfig.BREEDING.autobreeding.get()) {
            int textureX = 177;
            int textureY = 36;
            if (horseGenetic.isAutobreedable()) {
                textureX += autobreedIconWidth + 1;
            }
            int renderX = genderIconRenderX - autobreedIconWidth - 1;
            if (horseGenetic.isMale()) {
                renderX -= 2;
            }
            int renderY = j + 5;
            autobreedRenderX = renderX;
            autobreedRenderY = renderY;
            guiGraphics.drawTexture(TEXTURE_LOCATION, renderX, renderY, textureX, textureY, autobreedIconWidth, autobreedIconHeight);
            if (inAutobreedButton(mouseX, mouseY)) {
                guiGraphics.drawTexture(TEXTURE_LOCATION, renderX - 1, renderY - 1, 203, textureY - 1, autobreedIconWidth + 2, autobreedIconHeight + 2);
            }
        }

        InventoryScreen.drawEntity(guiGraphics, i + 51, j + 60, 17, (float)(i + 51) - mouseX, (float)(j + 75 - 50) - mouseY, this.horseGenetic);
    }

    @Override
    protected void drawForeground(DrawContext guiGraphics, int x, int y) {
        super.drawForeground(guiGraphics, x, y);
        if (!HorseConfig.COMMON.enableSizes.get() || horseGenetic.isBaby() || horseGenetic.hasChest()) {
            // Avoid showing an inaccurate height for foals
            return;
        }
        float height = horseGenetic.getGenome().getGeneticHeightCm();
        int cm = Math.round(height);
        int inches = Math.round(height / 2.54f);
        int hands = inches / 4;
        int point = inches % 4;
        String translationKey = HorseColors.MOD_ID + ".gui.height";
        Text heightText = Text.translatable(translationKey, hands, point, cm);
        String heightString = heightText.getString();
        int yy = 20;

        for (String line : heightString.split("\n")) {
            // matrix stack, text, x, y, color
            guiGraphics.drawText(this.textRenderer, Text.literal(line), 82, yy, 0x404040, false);
            yy += 9;
        }
        if (horseGenetic.isTooSmallForPlayerToRide()) {
            guiGraphics.drawText(this.textRenderer, Text.translatable(HorseColors.MOD_ID + ".gui.miniature"), 82, yy, 0x404040, false);
        }
        else if (horseGenetic.getGenome().isLarge()) {
            guiGraphics.drawText(this.textRenderer, Text.translatable(HorseColors.MOD_ID + ".gui.large"), 82, yy, 0x404040, false);
        }
    }

    @Override
    public boolean mouseClicked(double x, double y, int button) {
        if (button == 0 && HorseConfig.BREEDING.autobreeding.get() && inAutobreedButton(x, y)) {
            horseGenetic.setAutobreedable(!horseGenetic.isAutobreedable());
            return true;
        }
        return super.mouseClicked(x, y, button);
    }

    private final boolean inAutobreedButton(double x, double y) {
        return x >= autobreedRenderX && x < autobreedRenderX + autobreedIconWidth
            && y >= autobreedRenderY && y < autobreedRenderY + autobreedIconHeight;
    }
}
