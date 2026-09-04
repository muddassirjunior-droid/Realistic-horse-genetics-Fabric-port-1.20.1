package sekelsta.horse_colors.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.HorseEntity;
import net.minecraft.entity.passive.DonkeyEntity;
import net.minecraft.entity.passive.MuleEntity;
import net.minecraft.entity.passive.AbstractHorseEntity;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import sekelsta.horse_colors.HorseConfig;
import sekelsta.horse_colors.entity.AbstractHorseGenetic;
import sekelsta.horse_colors.entity.ModEntities;

/**
 * Replaces vanilla Horse/Donkey/Mule entities with their genetic equivalents
 * when HorseConfig.SPAWN's convertVanillaHorses/Donkeys/Mules are enabled
 * (all default false). Forge's EntityJoinLevelEvent, which the original mod
 * used, is a very broad cancelable "any entity about to join any level" hook
 * with no Fabric equivalent; ServerWorld#spawnEntity is the entry point
 * natural spawning, spawn eggs, and commands all funnel through, so it
 * covers this opt-in feature's common cases without needing to also chase
 * down chunk-generation-time entity placement (worldgen's own path is a
 * separate, narrower method the original mod special-cased for the same
 * reason).
 */
@Mixin(ServerWorld.class)
public class ServerWorldSpawnEntityMixin {
    @Inject(method = "spawnEntity", at = @At("HEAD"), cancellable = true)
    private void horse_colors$replaceVanillaEquine(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        if (!HorseConfig.shouldConvert(entity)) {
            return;
        }

        ServerWorld self = (ServerWorld)(Object)this;
        AbstractHorseGenetic newHorse = null;
        if (entity.getClass() == HorseEntity.class) {
            newHorse = ModEntities.HORSE_GENETIC.create(self);
        }
        else if (entity.getClass() == DonkeyEntity.class) {
            newHorse = ModEntities.DONKEY_GENETIC.create(self);
        }
        else if (entity.getClass() == MuleEntity.class) {
            newHorse = ModEntities.MULE_GENETIC.create(self);
        }
        if (newHorse == null) {
            return;
        }

        newHorse.copyAbstractHorse((AbstractHorseEntity)entity);
        self.spawnEntity(newHorse);
        cir.setReturnValue(false);
    }
}
