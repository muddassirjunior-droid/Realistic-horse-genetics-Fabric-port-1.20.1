package sekelsta.horse_colors.entity;

import java.util.Collection;

import net.minecraft.util.Identifier;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.passive.AbstractHorseEntity;
import net.minecraft.entity.passive.HorseEntity;
import net.minecraft.entity.passive.DonkeyEntity;
import net.minecraft.item.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import sekelsta.horse_colors.HorseConfig;
import sekelsta.horse_colors.breed.Breed;
import sekelsta.horse_colors.breed.BreedManager;
import sekelsta.horse_colors.entity.genetics.EquineGenome.Gene;
import sekelsta.horse_colors.entity.genetics.Species;
import sekelsta.horse_colors.util.Util;

public class DonkeyGeneticEntity extends AbstractHorseGenetic {
    private static final Identifier LOOT_TABLE = new Identifier("minecraft", "entities/donkey");

    public DonkeyGeneticEntity(EntityType<? extends DonkeyGeneticEntity> entityType, World world) {
        super(entityType, world);
    }

    @Override
    protected boolean receiveFood(PlayerEntity player, ItemStack stack) {
        boolean usedAction = false;
        if (stack.isOf(Items.APPLE)) {
            if (!this.getWorld().isClient && this.isTame() && this.getBreedingAge() == 0 && !this.isInLove()) {
                this.lovePlayer(player);
                usedAction = true;
            }
        }
        boolean supFed = super.receiveFood(player, stack);
        return usedAction || supFed;
    }

    @Override
    protected Identifier getLootTableId() {
        return this.LOOT_TABLE;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        super.getAmbientSound();
        return SoundEvents.ENTITY_DONKEY_AMBIENT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        super.getDeathSound();
        return SoundEvents.ENTITY_DONKEY_DEATH;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
        super.getHurtSound(damageSourceIn);
        return SoundEvents.ENTITY_DONKEY_HURT;
    }

    @Override
    protected SoundEvent getEatSound() {
        return SoundEvents.ENTITY_DONKEY_EAT;
    }

    @Override
    protected SoundEvent getAngrySound() {
        super.getAngrySound();
        return SoundEvents.ENTITY_DONKEY_ANGRY;
    }

    @Override
    public boolean fluffyTail() {
        return false;
    }

    @Override
    public boolean longEars() {
        return true;
    }

    @Override
    public boolean thinMane() {
        return true;
    }

    @Override
    public Species getSpecies() {
        return Species.DONKEY;
    }

   /**
    * Returns true if the mob is currently able to mate with the specified mob.
    */
    @Override
    public boolean canBreedWith(AnimalEntity otherAnimal) {
        if (otherAnimal == this)
        {
            return false;
        }
        if (otherAnimal instanceof AbstractHorseGenetic) {
            if (!this.isOppositeGender((AbstractHorseGenetic)otherAnimal)) {
                return false;
            }
        }
        if (otherAnimal instanceof DonkeyGeneticEntity
                || otherAnimal instanceof HorseGeneticEntity
                || otherAnimal instanceof DonkeyEntity
                || otherAnimal instanceof HorseEntity)
        {
            return this.canParent() && Util.horseCanMate((AbstractHorseEntity)otherAnimal);
        }
        else
        {
            return false;
        }
    }

    // Helper function for createChild that creates and spawns an entity of the
    // correct species
    @Override
    public AbstractHorseEntity getChild(ServerWorld world, PassiveEntity ageable)
    {
        if (ageable instanceof AbstractHorseGenetic) {
            AbstractHorseGenetic child = null;
            AbstractHorseGenetic other = (AbstractHorseGenetic)ageable;
            if (ageable instanceof HorseGeneticEntity) {
                child = ModEntities.MULE_GENETIC.create(this.getWorld());
                if (HorseConfig.BREEDING.enableGenders.get()
                        && !this.isMale() && ((HorseGeneticEntity)ageable).isMale()) {
                    ((MuleGeneticEntity)child).setSpecies(Species.HINNY);
                }
            }
            else if (ageable instanceof DonkeyGeneticEntity) {
                child = ModEntities.DONKEY_GENETIC.create(this.getWorld());
            }
            return child;
        }
        else if (ageable instanceof HorseEntity) {
            return EntityType.MULE.create(this.getWorld());
        }
        else if (ageable instanceof DonkeyEntity) {
            return EntityType.DONKEY.create(this.getWorld());
        }
        return null;
    }

    @Override
    public boolean isBreedingFood(ItemStack stack) {
        return HorseConfig.isDonkeyBreedingFood(stack);
    }

    @Override
    public Breed getDefaultBreed() {
        return BreedManager.DONKEY.getBreed("default_donkey");
    }

    @Override
    public Collection<Breed<Gene>> getBreeds() {
        return BreedManager.DONKEY.getAllBreeds();
    }

    @Override
    public int getPopulation() {
        return 40000000;
    }
}
