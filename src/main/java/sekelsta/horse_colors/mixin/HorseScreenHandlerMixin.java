package sekelsta.horse_colors.mixin;

import net.minecraft.entity.passive.AbstractHorseEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.HorseScreenHandler;
import net.minecraft.screen.ScreenHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import sekelsta.horse_colors.ContainerEventHandler;
import sekelsta.horse_colors.entity.AbstractHorseGenetic;

/**
 * Replaces the saddle slot so it accepts this mod's alternate saddle items too.
 * Runs on both sides: HorseScreenHandler is constructed by vanilla on the server
 * when a player opens a horse's inventory, and by this mod's
 * ClientPlayNetworkHandlerMixin on the client when building the HorseGui screen.
 */
@Mixin(HorseScreenHandler.class)
public class HorseScreenHandlerMixin {
    @Inject(method = "<init>", at = @At("TAIL"))
    private void horse_colors$replaceSaddleSlot(int syncId, PlayerInventory playerInventory, Inventory inventory, AbstractHorseEntity entity, CallbackInfo ci) {
        if (entity instanceof AbstractHorseGenetic horseGenetic) {
            ContainerEventHandler.replaceSaddleSlot(horseGenetic, (ScreenHandler)(Object)this);
        }
    }
}
