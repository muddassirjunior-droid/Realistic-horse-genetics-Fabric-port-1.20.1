package sekelsta.horse_colors;

import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.item.ItemStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import sekelsta.horse_colors.entity.AbstractHorseGenetic;

public class ContainerEventHandler {
    // Replace the saddle slot with one that accepts alternate saddles.
    // Called from HorseScreenHandlerMixin, which covers both the server-side
    // ScreenHandler and the client-side one this mod's ClientPlayNetworkHandlerMixin
    // builds for the HorseGui screen.
    public static void replaceSaddleSlot(AbstractHorseGenetic horse, ScreenHandler container) {
        Slot saddleSlot = new Slot(horse.getHorseChest(), 0, 8, 18) {
            @Override
            public boolean canInsert(ItemStack stack) {
                return horse.isSaddle(stack) && !this.hasStack() && horse.canBeSaddled();
            }

            @Override
            @Environment(EnvType.CLIENT)
            public boolean isEnabled() {
                return horse.canBeSaddled();
            }
        };
        container.slots.set(0, saddleSlot);
    }
}
