package sekelsta.horse_colors.entity;

import com.google.common.collect.ImmutableList;
import java.util.Collection;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.util.Identifier;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.AbstractHorseEntity;
import net.minecraft.world.World;

import sekelsta.horse_colors.HorseConfig;
import sekelsta.horse_colors.breed.Breed;
import sekelsta.horse_colors.breed.BreedManager;
import sekelsta.horse_colors.entity.genetics.EquineGenome;
import sekelsta.horse_colors.entity.genetics.EquineGenome.Gene;
import sekelsta.horse_colors.entity.genetics.Species;
import sekelsta.horse_colors.HorseColors;

public class MuleGeneticEntity extends AbstractHorseGenetic {
    protected static final TrackedData<Integer> SPECIES = DataTracker.registerData(MuleGeneticEntity.class, TrackedDataHandlerRegistry.INTEGER);

    private static final Identifier LOOT_TABLE = new Identifier("minecraft", "entities/mule");

    public MuleGeneticEntity(EntityType<? extends MuleGeneticEntity> entity, World world) {
        super(entity, world);
    }

    @Override
    protected void initDataTracker()
    {
        super.initDataTracker();
        this.dataTracker.startTracking(SPECIES, Species.MULE.ordinal());
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound compound) {
        super.writeCustomDataToNbt(compound);
        compound.putString("Species", this.getSpecies().toString());
    }

   /**
    * Helper method to read subclass entity data from NBT.
    */
    @Override
    public void readCustomDataFromNbt(NbtCompound compound) {
        super.readCustomDataFromNbt(compound);
        if (compound.contains("Species")) {
            this.setSpecies(Species.valueOf(compound.getString("Species")));
        }
    }

    @Override
    protected void readExtraGenes(NbtCompound compound) {
        // Do nothing
    }

    @Override
    protected Identifier getLootTableId() {
        return this.LOOT_TABLE;
    }

    @Override
    public boolean fluffyTail() {
        return true;
    }

    @Override
    public boolean longEars() {
        return true;
    }

    @Override
    public boolean thinMane() {
        return false;
    }

    public void setSpecies(Species species) {
        this.dataTracker.set(SPECIES, species.ordinal());
    }

    @Override
    public Species getSpecies() {
        return Species.values()[this.dataTracker.get(SPECIES).intValue()];
    }

    @Override
    public boolean isFertile() {
        return false;
    }

    @Override
    public void setFertile(boolean fertile) {
        // Pass
    }

    @Override
    protected boolean canTestGenetics() {
        return false;
    }

    @Override
    // Helper function for createChild that creates and spawns an entity of the
    // correct species
    public AbstractHorseEntity getChild(ServerWorld world, PassiveEntity ageable) {
        MuleGeneticEntity child = ModEntities.MULE_GENETIC.create(this.getWorld());
        return child;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        super.getAmbientSound();
        return SoundEvents.ENTITY_MULE_AMBIENT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        super.getDeathSound();
        return SoundEvents.ENTITY_MULE_DEATH;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
        super.getHurtSound(damageSourceIn);
        return SoundEvents.ENTITY_MULE_HURT;
    }

    @Override
    protected SoundEvent getEatSound() {
        return SoundEvents.ENTITY_MULE_EAT;
    }

    @Override
    protected SoundEvent getAngrySound() {
        super.getAngrySound();
        return SoundEvents.ENTITY_MULE_ANGRY;
    }

    @Override
    protected Text getDefaultName() {
        if (!this.isBaby() && !HorseConfig.BREEDING.enableGenders.get()
            && this.getSpecies() == Species.HINNY) {
            String s = "entity." + HorseColors.MOD_ID + ".hinny";
            return Text.translatable(s);
        }
        return super.getDefaultName();
    }

    @Override
    public Collection<Breed<Gene>> getBreeds() {
        return ImmutableList.of(getDefaultBreed());
    }

    @Override
    protected void randomizeGenes(Breed breed) {
        EquineGenome horse = new EquineGenome(Species.HORSE);
        horse.randomize(BreedManager.HORSE.getBreed("default_horse"));
        EquineGenome donkey = new EquineGenome(Species.DONKEY);
        donkey.randomize(BreedManager.DONKEY.getBreed("default_donkey"));
        genes.inheritGenes(horse, donkey);

        useGeneticAttributes();
        // Assume mother was the same size
        setMotherSize(getGenome().getGeneticScale());
        // Size depends on mother size so call again to stabilize somewhat
        setMotherSize(getGenome().getGeneticScale());
    }
}
