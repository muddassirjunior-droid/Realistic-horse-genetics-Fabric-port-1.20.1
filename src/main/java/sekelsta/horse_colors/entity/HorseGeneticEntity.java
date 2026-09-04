package sekelsta.horse_colors.entity;

import java.util.Collection;

import net.minecraft.util.Identifier;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.util.math.random.Random;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.AbstractHorseEntity;
import net.minecraft.entity.passive.HorseEntity;
import net.minecraft.entity.passive.DonkeyEntity;
import net.minecraft.item.HorseArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import sekelsta.horse_colors.HorseConfig;
import sekelsta.horse_colors.breed.Breed;
import sekelsta.horse_colors.breed.BreedManager;
import sekelsta.horse_colors.entity.genetics.EquineGenome.Gene;
import sekelsta.horse_colors.entity.genetics.Species;
import sekelsta.horse_colors.util.Util;

public class HorseGeneticEntity extends AbstractHorseGenetic
{
    private static final Identifier LOOT_TABLE = new Identifier("minecraft", "entities/horse");

    public HorseGeneticEntity(EntityType<? extends HorseGeneticEntity> entityType, World worldIn)
    {
        super(entityType, worldIn);
    }

    @Override
    protected Identifier getLootTableId() {
        return this.LOOT_TABLE;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        super.getAmbientSound();
        return SoundEvents.ENTITY_HORSE_AMBIENT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        super.getDeathSound();
        return SoundEvents.ENTITY_HORSE_DEATH;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
        super.getHurtSound(damageSourceIn);
        return SoundEvents.ENTITY_HORSE_HURT;
    }

    @Override
    protected SoundEvent getEatSound() {
        return SoundEvents.ENTITY_HORSE_EAT;
    }

    @Override
    protected SoundEvent getAngrySound() {
        super.getAngrySound();
        return SoundEvents.ENTITY_HORSE_ANGRY;
    }

    @Override
    public boolean fluffyTail() {
        return true;
    }
    @Override
    public boolean longEars() {
        return false;
    }

    @Override
    public boolean thinMane() {
        return false;
    }

    @Override
    public boolean canEquipChest() {
        return false;
    }

    @Override
    public Species getSpecies() {
        return Species.HORSE;
    }

    /**
     * Returns true if the mob is currently able to mate with the specified mob.
     */
    @Override
    public boolean canBreedWith(AnimalEntity otherAnimal)
    {
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
                child = ModEntities.HORSE_GENETIC.create(this.getWorld());
            }
            else if (ageable instanceof DonkeyGeneticEntity) {
                child = ModEntities.MULE_GENETIC.create(this.getWorld());
                if (HorseConfig.BREEDING.enableGenders.get()
                        && this.isMale() && !((DonkeyGeneticEntity)ageable).isMale()) {
                    ((MuleGeneticEntity)child).setSpecies(Species.HINNY);
                }
            }
            return child;
        }
        else if (ageable instanceof HorseEntity) {
            // Breed the vanilla horse to itself
            PassiveEntity child = ageable.createChild(world, ageable);
            if (child instanceof AbstractHorseEntity) {
                return (AbstractHorseEntity)child;
            }
            // else
            return null;
        }
        else if (ageable instanceof DonkeyEntity) {
            return EntityType.MULE.create(this.getWorld());
        }
        return null;
    }

    public boolean isArmor(ItemStack stack) {
        return stack.getItem() instanceof HorseArmorItem
                || stack.isIn(ItemTags.WOOL_CARPETS);
    }

    @Override
    public boolean isBreedingFood(ItemStack stack) {
        return HorseConfig.isHorseBreedingFood(stack);
    }

    @Override
    public Breed getDefaultBreed() {
        return BreedManager.HORSE.getBreed("default_horse");
    }

    @Override
    public Collection<Breed<Gene>> getBreeds() {
        return BreedManager.HORSE.getAllBreeds();
    }

    @Override
    public int getPopulation() {
        return 60000000;
    }

    // Set stats for vanilla-like breeding
    @Override
    protected void initAttributes(Random rand) {
        super.initAttributes(rand);
        if (!HorseConfig.GENETICS.useGeneticStats.get()) {
            this.getAttributeInstance(net.minecraft.entity.attribute.EntityAttributes.GENERIC_MOVEMENT_SPEED).setBaseValue(this.getChildMovementSpeedBonus(rand::nextDouble));
            this.getAttributeInstance(net.minecraft.entity.attribute.EntityAttributes.HORSE_JUMP_STRENGTH).setBaseValue(this.getChildJumpStrengthBonus(rand::nextDouble));
        }
    }
}
