package sekelsta.horse_colors.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HorseScreen;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.AbstractHorseEntity;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.network.NetworkThreadUtils;
import net.minecraft.network.packet.s2c.play.OpenHorseScreenS2CPacket;
import net.minecraft.screen.HorseScreenHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import sekelsta.horse_colors.client.HorseGui;
import sekelsta.horse_colors.entity.AbstractHorseGenetic;

/**
 * Vanilla has no registry hook for the horse inventory screen (unlike other
 * container screens, it's opened directly by ClientPlayNetworkHandler#onOpenHorseScreen
 * rather than through HandledScreens.register), so swapping in our own screen for
 * genetic horses needs a full replacement of that method rather than a registration call.
 */
@Mixin(ClientPlayNetworkHandler.class)
public class ClientPlayNetworkHandlerMixin {
    @Shadow
    private ClientWorld world;

    @Shadow
    private MinecraftClient client;

    @Inject(method = "onOpenHorseScreen", at = @At("HEAD"), cancellable = true)
    private void horse_colors$openHorseGui(OpenHorseScreenS2CPacket packet, CallbackInfo ci) {
        ClientPlayNetworkHandler self = (ClientPlayNetworkHandler)(Object)this;
        NetworkThreadUtils.forceMainThread(packet, self, this.client);
        Entity entity = this.world.getEntityById(packet.getHorseId());
        if (entity instanceof AbstractHorseGenetic horseGenetic) {
            ClientPlayerEntity clientPlayerEntity = this.client.player;
            SimpleInventory simpleInventory = new SimpleInventory(packet.getSlotCount());
            HorseScreenHandler horseScreenHandler = new HorseScreenHandler(
                packet.getSyncId(), clientPlayerEntity.getInventory(), simpleInventory, horseGenetic
            );
            clientPlayerEntity.currentScreenHandler = horseScreenHandler;
            this.client.setScreen(new HorseGui(horseScreenHandler, clientPlayerEntity.getInventory(), horseGenetic));
            ci.cancel();
        }
    }
}
