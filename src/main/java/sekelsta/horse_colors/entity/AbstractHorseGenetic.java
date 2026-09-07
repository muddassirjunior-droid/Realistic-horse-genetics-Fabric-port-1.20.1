package sekelsta.horse_colors.entity;

import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.jetbrains.annotations.Nullable;

import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.registry.Registries;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.nbt.*;
import net.minecraft.entity.data.*;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.*;
import net.minecraft.entity.damage.*;
import net.minecraft.entity.effect.*;
import net.minecraft.entity.*;
import net.minecraft.entity.attribute.*;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.ai.goal.TemptGoal;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.passive.AbstractDonkeyEntity;
import net.minecraft.entity.passive.AbstractHorseEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.recipe.Ingredient;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.event.GameEvent;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.inventory.StackReference;
import net.minecraft.util.Hand;
import net.minecraft.util.ActionResult;

import sekelsta.horse_colors.HorseColors;
import sekelsta.horse_colors.HorseConfig;
import sekelsta.horse_colors.breed.*;
import sekelsta.horse_colors.entity.ai.*;
import sekelsta.horse_colors.entity.genetics.*;
import sekelsta.horse_colors.entity.genetics.EquineGenome.Gene;
import sekelsta.horse_colors.item.*;
import sekelsta.horse_colors.network.*;
import sekelsta.horse_colors.util.Util;

public abstract class AbstractHorseGenetic extends AbstractDonkeyEntity implements IGeneticEntity<Gene> {
    protected EquineGenome genes = new EquineGenome(this.getSpecies(), this);
    protected UUID motherUUID = null;
    protected UUID fatherUUID = null;
    protected static final TrackedData<String> GENES = DataTracker.registerData(AbstractHorseGenetic.class, TrackedDataHandlerRegistry.STRING);

    protected static final TrackedData<Integer> HORSE_RANDOM = DataTracker.registerData(AbstractHorseGenetic.class, TrackedDataHandlerRegistry.INTEGER);
    protected static final TrackedData<Integer> DISPLAY_AGE = DataTracker.registerData(AbstractHorseGenetic.class, TrackedDataHandlerRegistry.INTEGER);
    protected static final TrackedData<Boolean> GENDER = DataTracker.registerData(AbstractHorseGenetic.class, TrackedDataHandlerRegistry.BOOLEAN);
    protected static final TrackedData<Boolean> FERTILE = DataTracker.registerData(AbstractHorseGenetic.class, TrackedDataHandlerRegistry.BOOLEAN);
    protected static final TrackedData<Integer> PREGNANT_SINCE = DataTracker.registerData(AbstractHorseGenetic.class, TrackedDataHandlerRegistry.INTEGER);
    protected static final TrackedData<Float> MOTHER_SIZE = DataTracker.registerData(AbstractHorseGenetic.class, TrackedDataHandlerRegistry.FLOAT);
    protected static final TrackedData<Boolean> AUTOBREEDABLE = DataTracker.registerData(AbstractHorseGenetic.class, TrackedDataHandlerRegistry.BOOLEAN);
    protected int trueAge;
    protected FleeGoal fleeGoal;
    public OustGoal oustGoal;
    protected long lastOustTime;


    protected static final UUID ARMOR_MODIFIER_UUID = UUID.fromString("556E1665-8B10-40C8-8F9D-CF9B1667F295");
    protected static final UUID CSNB_SPEED_UUID = UUID.fromString("84ca527a-5c70-4336-a737-ae3f6d40ef45");
    protected static final UUID CSNB_JUMP_UUID = UUID.fromString("72323326-888b-4e46-bf52-f669600642f7");
    protected static final EntityAttributeModifier CSNB_SPEED_MODIFIER = new EntityAttributeModifier(CSNB_SPEED_UUID, "CSNB speed penalty", -0.6, EntityAttributeModifier.Operation.MULTIPLY_TOTAL);
    protected static final EntityAttributeModifier CSNB_JUMP_MODIFIER = new EntityAttributeModifier(CSNB_JUMP_UUID, "CSNB jump penalty", -0.6, EntityAttributeModifier.Operation.MULTIPLY_TOTAL);

    protected List<AbstractHorseGenetic> unbornChildren = new ArrayList<>();

    // AbstractHorseEntity.eatingTicks (counts down the "eating" animation) is private.
    // Reflection using the Yarn name only works in the dev environment, where Loom
    // remaps the whole game jar to Yarn names - in a real player's game the field has
    // a different runtime name and a literal "eatingTicks" string lookup throws
    // NoSuchFieldException. An access widener is the correct fix: it grants access
    // while still going through Loom/Fabric Loader's real name remapping, so plain
    // field access below resolves correctly in both environments.

    public AbstractHorseGenetic(EntityType<? extends AbstractHorseGenetic> entityType, World worldIn)
    {
        super(entityType, worldIn);
    }

    public EquineGenome getGenome() {
        return genes;
    }

    public abstract boolean fluffyTail();
    public abstract boolean longEars();
    public abstract boolean thinMane();
    public abstract Species getSpecies();

    public boolean canEquipChest() {
        return true;
    }

    @Override
    public int getSeed() {
        return this.dataTracker.get(HORSE_RANDOM).intValue();
    }

    @Override
    public void setSeed(int seed) {
        this.dataTracker.set(HORSE_RANDOM, seed);
    }

    @Override
    public Random getRand() {
        return this.getRandom();
    }

    // AbstractHorseEntity's implementation of this Tameable method is a synthetic
    // bridge method (covariant override of AnimalEntity.getWorld(), confirmed by
    // decompiling its bytecode), which javac won't accept as satisfying the
    // interface for a freshly-compiled subclass - re-declaring it here makes every
    // concrete subclass compile. super.method_48926() isn't callable directly since
    // javac resolves it back to Tameable's abstract declaration, not the bridge.
    @Override
    public EntityView method_48926() {
        return this.getWorld();
    }

    @Override
    protected void initGoals() {
        this.goalSelector.add(0, new SwimGoal(this));
        this.goalSelector.add(1, new EscapeDangerGoal(this, 1.2D));
        this.goalSelector.add(2, new HorseBondWithPlayerGoal(this, 1.2D));
        if (HorseConfig.COMMON.spookyHorses.get()) {
            this.goalSelector.add(3, new SpookGoal(this, HostileEntity.class, 8.0F, 1.5, 1.5));
        }
        this.goalSelector.add(4, fleeGoal = new FleeGoal(this));
        this.goalSelector.add(5, new AnimalMateGoal(this, 1.0D, AbstractHorseEntity.class));
        this.goalSelector.add(6, new TemptGoal(this, 1.25,
            Ingredient.ofItems(Items.GOLDEN_CARROT, Items.GOLDEN_APPLE, Items.ENCHANTED_GOLDEN_APPLE, Items.APPLE, Blocks.HAY_BLOCK.asItem()), false));
        this.goalSelector.add(7, new StayWithHerd(this));
        this.goalSelector.add(8, oustGoal = new OustGoal(this));
        this.goalSelector.add(9, new RandomWalkGroundTie(this, 0.7D));
        this.goalSelector.add(10, new LookAtEntityGoal(this, PlayerEntity.class, 6.0F));
        this.goalSelector.add(11, new LookAroundGoal(this));
    }

    @Override
    protected void initDataTracker()
    {
        super.initDataTracker();
        this.dataTracker.startTracking(GENES, "");
        this.dataTracker.startTracking(HORSE_RANDOM, 0);
        this.dataTracker.startTracking(DISPLAY_AGE, 0);
        this.dataTracker.startTracking(GENDER, false);
        this.dataTracker.startTracking(FERTILE, true);
        this.dataTracker.startTracking(AUTOBREEDABLE, false);
        this.dataTracker.startTracking(PREGNANT_SINCE, -1);
        this.dataTracker.startTracking(MOTHER_SIZE, 1f);
    }

    /**
     * (abstract) Protected helper method to write subclass entity data to NBT.
     */
    @Override
    public void writeCustomDataToNbt(NbtCompound compound)
    {
        super.writeCustomDataToNbt(compound);
        writeGeneticData(compound);
        if (!this.items.getStack(1).isEmpty()) {
            compound.put("ArmorItem", this.items.getStack(1).writeNbt(new NbtCompound()));
        }
        compound.putBoolean("autobreedable", isAutobreedable());
    }

    private void writeGeneticData(NbtCompound compound) {
        compound.putString("Genes", this.getGenome().getBase64());
        compound.putInt("Random", this.getSeed());
        compound.putInt("true_age", this.trueAge);
        compound.putBoolean("gender", this.isMale());
        compound.putBoolean("fertile", this.isFertile());
        compound.putInt("pregnant_since", this.getPregnancyStart());
        if (this.unbornChildren != null) {
            NbtList unbornChildrenTag = new NbtList();
            for (AbstractHorseGenetic child : this.unbornChildren) {
                NbtCompound childNBT = new NbtCompound();
                childNBT.putString("species", child.getSpecies().toString());
                childNBT.putString("genes", child.getGenome().genesToString());
                childNBT.putFloat("mother_size", child.getMotherSize());
                if (child.motherUUID != null) {
                    childNBT.putUuid("MotherUUID", child.motherUUID);
                }
                if (child.fatherUUID != null) {
                    childNBT.putUuid("FatherUUID", child.fatherUUID);
                }
                unbornChildrenTag.add(childNBT);
            }
            compound.put("unborn_children", unbornChildrenTag);
        }
        compound.putFloat("mother_size", this.getMotherSize());
        if (motherUUID != null) {
            compound.putUuid("MotherUUID", this.motherUUID);
        }
        if (fatherUUID != null) {
            compound.putUuid("FatherUUID", this.fatherUUID);
        }
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound compound)
    {
        super.readCustomDataFromNbt(compound);
        if (compound.containsUuid("MotherUUID")) {
            this.motherUUID = compound.getUuid("MotherUUID");
        }
        if (compound.containsUuid("FatherUUID")) {
            this.fatherUUID = compound.getUuid("FatherUUID");
        }
        // Read the main part of the data
        readGeneticData(compound);
        // Ensure the true age matches the age
        if (this.trueAge < 0 != this.breedingAge < 0) {
            this.trueAge = this.breedingAge;
        }

        // Set any genes that were specified in a human-readable format
        readExtraGenes(compound);

        this.useGeneticAttributes();
        this.updateSaddle();

        if (this instanceof HorseGeneticEntity) {
            int spawndata = compound.getInt("VillageSpawn");
            if (spawndata != 0) {
                this.initFromVillageSpawn();
            }
        }

        if (getSpecies() == Species.MULE || getSpecies() == Species.HINNY) {
            setFertile(false);
        }

        if (compound.contains("ArmorItem", 10)) {
            ItemStack itemstack = ItemStack.fromNbt(compound.getCompound("ArmorItem"));
            if (!itemstack.isEmpty() && this.isArmor(itemstack)) {
                this.items.setStack(1, itemstack);
            }
        }
        this.updateSaddle();

        if (compound.contains("autobreedable")) {
            setAutobreedable(compound.getBoolean("autobreedable"));
        }
    }

    // A helper function for reading the data
    private void readGeneticData(NbtCompound compound) {
        // Set genes if they exist
        if (compound.contains("Genes")) {
            String genes = compound.getString("Genes");
            if (genes.length() > 0 && genes.charAt(0) < 48) {
                // Read genes using the old 1:1 conversion of chars to nums
                this.setGeneData(compound.getString("Genes"));
            }
            else {
                this.getGenome().setFromBase64(compound.getString("Genes"));
            }
        }
        // Otherwise, use a breed for a base if given one
        else if (compound.contains("Breed")) {
            this.randomizeGenes(getBreed(compound.getString("Breed")));
        }
        else {
            randomizeGenes(getRandomBreed());
        }

        // Replace saddle reading functionality from AbstractHorseEntity with
        // one that accepts alternate saddles
        if (compound.contains("SaddleItem", 10)) {
            ItemStack itemstack = ItemStack.fromNbt(compound.getCompound("SaddleItem"));
            if (isSaddle(itemstack)) {
                this.items.setStack(0, itemstack);
            }
        }

        if (compound.contains("Random")) {
            this.setSeed(compound.getInt("Random"));
        }
        if (compound.contains("true_age")) {
            this.trueAge = compound.getInt("true_age");
        }
        if (compound.contains("gender")) {
            this.setMale(compound.getBoolean("gender"));
        }
        else {
            this.setMale(this.random.nextBoolean());
        }
        if (compound.contains("fertile")) {
            this.setFertile(compound.getBoolean("fertile"));
        }

        int pregnantSince = -1;
        if (compound.contains("pregnant_since")) {
            pregnantSince = compound.getInt("pregnant_since");
        }
        this.dataTracker.set(PREGNANT_SINCE, pregnantSince);
        if (compound.contains("unborn_children")) {
            NbtElement nbt = compound.get("unborn_children");
            if (nbt instanceof NbtList) {
                NbtList childListTag = (NbtList)nbt;
                for (int i = 0; i < childListTag.size(); ++i) {
                    NbtElement cnbt = childListTag.get(i);
                    if (!(cnbt instanceof NbtCompound)) {
                        continue;
                    }
                    NbtCompound childNBT = (NbtCompound)cnbt;
                    Species species = Species.valueOf(childNBT.getString("species"));
                    AbstractHorseGenetic child = null;
                    switch(species) {
                        case HORSE:
                            child = ModEntities.HORSE_GENETIC.create(this.getWorld());
                            break;
                        case DONKEY:
                            child = ModEntities.DONKEY_GENETIC.create(this.getWorld());
                            break;
                        case MULE:
                        case HINNY:
                            child = ModEntities.MULE_GENETIC.create(this.getWorld());
                            ((MuleGeneticEntity)child).setSpecies(species);
                            break;
                    }
                    if (child != null) {
                        EquineGenome genome = new EquineGenome(child.getSpecies(), child);
                        genome.genesFromString(childNBT.getString("genes"));
                        if (childNBT.containsUuid("MotherUUID")) {
                            child.motherUUID = childNBT.getUuid("MotherUUID");
                        }
                        else {
                            child.motherUUID = this.uuid;
                        }
                        if (childNBT.containsUuid("FatherUUID")) {
                            child.fatherUUID = childNBT.getUuid("FatherUUID");
                        }
                        if (childNBT.contains("mother_size")) {
                            child.setMotherSize(childNBT.getFloat("mother_size"));
                        }
                        else {
                            child.setMotherSize(this.getGenome().getAdultScale());
                        }
                        this.unbornChildren.add(child);
                    }
                }
            }
        }

        float motherSize = 1f;
        if (compound.contains("mother_size")) {
            motherSize = compound.getFloat("mother_size");
        }
        setMotherSize(motherSize);
    }

    protected void readExtraGenes(NbtCompound compound) {
        boolean changed = false;
        for (Enum gene : this.getGenome().listGenes()) {
            if (compound.contains(gene.toString())) {
                int alleles[] = compound.getIntArray(gene.toString());
                List<Integer> allowedAlleles = getGenome().getAllowedAlleles(gene, getDefaultBreed());
                for (int i = 0; i < 2; ++i) {
                    if (allowedAlleles.contains(alleles[i])) {
                        getGenome().setAllele(gene, i, alleles[i]);
                    }
                }
                changed = true;
            }
        }
        if (changed) {
            getGenome().finalizeGenes();
        }
    }

    public ItemStack getArmorItem() {
        return this.getEquippedStack(EquipmentSlot.CHEST);
    }

    private void setArmor(ItemStack itemstack) {
        this.equipStack(EquipmentSlot.CHEST, itemstack);
        this.setEquipmentDropChance(EquipmentSlot.CHEST, 0.0F);
    }

   /**
    * Updates the items in the saddle and armor slots of the horse's inventory.
    */
    @Override
    protected void updateSaddle() {
        if (!this.getWorld().isClient()) {
            super.updateSaddle();
            this.setArmorStack(this.items.getStack(1));
            this.setEquipmentDropChance(EquipmentSlot.CHEST, 0.0F);
        }
    }

    private void setArmorStack(ItemStack itemstack) {
        this.setArmor(itemstack);
        if (!this.getWorld().isClient) {
            this.getAttributeInstance(EntityAttributes.GENERIC_ARMOR).removeModifier(ARMOR_MODIFIER_UUID);
            // Do not use this.isArmor(itemstack)) because that can return true for things which
            // can't be cast to HorseArmorItem
            if (itemstack.getItem() instanceof HorseArmorItem) {
                int i = ((HorseArmorItem)itemstack.getItem()).getBonus();
                if (i != 0) {
                    this.getAttributeInstance(EntityAttributes.GENERIC_ARMOR).addTemporaryModifier((new EntityAttributeModifier(ARMOR_MODIFIER_UUID, "Horse armor bonus", (double)i, EntityAttributeModifier.Operation.ADDITION)));
                }
            }
        }
    }

   /**
    * Called by InventoryBasic.containerChanged() on a array that is never filled.
    */
    @Override
    public void onInventoryChanged(Inventory invBasic) {
        ItemStack itemstack = this.getArmorItem();
        super.onInventoryChanged(invBasic);
        ItemStack itemstack1 = this.getArmorItem();
        if (this.age > 20 && this.isArmor(itemstack1) && itemstack != itemstack1) {
            this.playSound(SoundEvents.ENTITY_HORSE_ARMOR, 0.5F, 1.0F);
        }
    }

    public void copyAbstractHorse(AbstractHorseEntity horse)
    {
        // Copy NBT data (initialize from horse's NBT)
        NbtCompound vanilla = horse.writeNbt(new NbtCompound());
        // Don't try to read Minecraft's variant as legacy gene data
        if (vanilla.contains("Variant")) {
            vanilla.remove("Variant");
        }
        this.readNbt(vanilla);
        this.useGeneticAttributes();
    }

    public int getDisplayAge() {
        return this.dataTracker.get(DISPLAY_AGE);
    }

    public void setDisplayAge(int age) {
        this.dataTracker.set(DISPLAY_AGE, age);
    }

    @Override
    public int getTrueAge() {
        if (isBaby() && getDisplayAge() >= 0) {
            return getBirthAge();
        }
        return getDisplayAge();
    }

    public UUID getMotherUUID() {
        return motherUUID;
    }

    public UUID getFatherUUID() {
        return fatherUUID;
    }

    public void setGeneData(String genes) {
        this.dataTracker.set(GENES, genes);
    }

    public String getGeneData() {
        return (String)this.dataTracker.get(GENES);
    }

    public void setMotherSize(float size) {
        this.dataTracker.set(MOTHER_SIZE, size);
    }

    public float getMotherSize() {
        return ((Float)this.dataTracker.get(MOTHER_SIZE)).floatValue();
    }

    @Override
    public void onTrackedDataSet(TrackedData<?> key) {
        if (GENES.equals(key)) {
            this.getGenome().resetTexture();
            this.useGeneticAttributes();
            this.calculateDimensions();
        }
        else if (HORSE_RANDOM.equals(key)
            || GENDER.equals(key)
            || MOTHER_SIZE.equals(key)) {
            this.calculateDimensions();
        }
        else if (DISPLAY_AGE.equals(key)) {
            this.getGenome().resetTexture();
        }

        super.onTrackedDataSet(key);
    }

    @Override
    public boolean isMale() {
        return ((Boolean)this.dataTracker.get(GENDER)).booleanValue();
    }

    @Override
    public void setMale(boolean gender) {
        if (gender) {
            // Prepare to become male
            this.unbornChildren = new ArrayList<>();
            this.dataTracker.set(PREGNANT_SINCE, -1);
        }
        this.dataTracker.set(GENDER, gender);
    }

    public boolean isFertile() {
        return ((Boolean)this.dataTracker.get(FERTILE)).booleanValue();
    }

    public void setFertile(boolean fertile) {
        if (isPregnant()) {
            fertile = true;
        }
        this.dataTracker.set(FERTILE, fertile);
    }

    public boolean isAutobreedable() {
        return ((Boolean)this.dataTracker.get(AUTOBREEDABLE)).booleanValue();
    }

    public void setAutobreedable(boolean allowed) {
        if (getWorld().isClient) {
            HorsePacketHandler.sendToServer(new CAutobreedPacket(getId(), allowed));
        }
        else {
            dataTracker.set(AUTOBREEDABLE, allowed);
        }
    }

    // Not a real vanilla override on Fabric - the NeoForge build this mod was
    // originally written against patches AnimalEntity#canBreedWith with an
    // extra "canParent" style hook. Kept as a plain method other code in this
    // mod calls directly.
    protected boolean canParent() {
        return Util.horseCanMate(this) && isFertile();
    }

    @Override
    public void lovePlayer(@Nullable PlayerEntity loveCause) {
        if (!isFertile()) {
            return;
        }
        super.lovePlayer(loveCause);
    }

    public boolean isPregnant() {
        return this.getPregnancyStart() >= 0;
    }

    public int getPregnancyStart() {
        return this.dataTracker.get(PREGNANT_SINCE);
    }

    public float getPregnancyProgress() {
        int passed = getDisplayAge() - getPregnancyStart();
        int total = HorseConfig.getHorsePregnancyLength();
        return (float)passed / (float)total;
    }

    public int getRebreedTicks() {
        return HorseConfig.getHorseRebreedTicks(this.isMale());
    }

    public int getBirthAge() {
        return HorseConfig.getHorseBirthAge();
    }

    // Since miniature horses are too small to ride, they can't be tamed the usual way
    @Override
    public boolean isTame() {
        return isTooSmallForPlayerToRide() || super.isTame();
    }

    protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
        // Don't stop and rear in response to suffocation or cactus damage
        DamageSources damageSources = getWorld().getDamageSources();
        if (damageSourceIn != damageSources.inWall() && damageSourceIn != damageSources.cactus()) {
            // Chance to rear up
            super.getHurtSound(damageSourceIn);
        }
        return null;
    }

    /**
     * Set whether this creature is a child.
     */
    @Override
    public void setBaby(boolean isBaby) {
        this.setBreedingAge(isBaby ? this.getBirthAge() : 0);
    }

    @Override
    protected boolean canAddPassenger(Entity passenger) {
        return this.getPassengerList().size() < 2;
    }

    // This should err on the side of too exclusive, while canAddPassenger
    // should err towards more inclusive. Other mods can bypass this by simply
    // instructing the entity to mount the horse.
    private boolean canFitRider(PlayerEntity rider) {
        return canAddPassenger(rider)
            && !isTooSmallForPlayerToRide()
            && (this.getGenome().isLarge() || this.getPassengerList().size() < 1);
    }

    protected void doPlayerRide(PlayerEntity player) {
        if (!canFitRider(player)) {
            return;
        }
        this.putPlayerOnBack(player);
    }

    public SimpleInventory getHorseChest() {
        return this.items;
    }

    protected boolean canTestGenetics() {
        return true;
    }

    // Not a real vanilla override on Fabric - see canParent() above.
    public boolean isArmor(ItemStack stack) {
        if (stack.isIn(ItemTags.WOOL_CARPETS)) {
            return true;
        }
        if (stack.getItem() instanceof HorseArmorItem) {
            HorseArmorItem armor = (HorseArmorItem)(stack.getItem());
            return armor.getBonus() == 0;
        }
        return false;
    }

    private boolean itemInteract(PlayerEntity player, ItemStack itemstack, Hand hand) {
        // Enter genetic test results
        if (itemstack.getItem() == Items.BOOK
                && (HorseConfig.GENETICS.bookShowsGenes.get()
                    || HorseConfig.GENETICS.bookShowsTraits.get())
                && (this.isTame() || player.getAbilities().creativeMode)
                && this.canTestGenetics()) {
            ItemStack book = new ItemStack(ModItems.geneBookItem);
            if (book.getNbt() == null) {
                book.setNbt(new NbtCompound());
            }
            book.getNbt().putString("species", this.getSpecies().name());
            book.getNbt().putString("genes", this.getGenome().genesToString());
            book.getNbt().putUuid("EntityUUID", this.getUuid());
            if (this.hasCustomName()) {
                book.setCustomName(this.getCustomName());
            }
            if (!player.giveItemStack(book)) {
                this.dropStack(book);
            }
            if (!player.getAbilities().creativeMode) {
                itemstack.decrement(1);
            }
            return true;
        }
        // Unequip a chest
        if (hasChest() && itemstack.getItem() instanceof AxeItem) {
            if (!getWorld().isClient) {
                dropItem(Blocks.CHEST);
                if (items != null) {
                    for (int i = 2; i < items.size(); ++i) {
                        ItemStack istack = items.getStack(i);
                        if (!istack.isEmpty() && !EnchantmentHelper.hasVanishingCurse(istack)) {
                            dropStack(istack);
                        }
                    }
                }
            }
            setHasChest(false);
            onChestedStatusChanged();
            return true;
        }

        // Only allow taming with an empty hand
        if (!this.isTame()) {
            this.updateAnger();
            return true;
        }
        // If tame, equip chest
        if (!this.hasChest() && itemstack.getItem() == Blocks.CHEST.asItem()
                && this.canEquipChest()) {
            this.setHasChest(true);
            this.playAddChestSound();
            if (!player.getAbilities().creativeMode) {
                itemstack.decrement(1);
            }

            this.onChestedStatusChanged();
            return true;
        }
        // If tame, equip saddle
        if (!this.isSaddled() && isSaddle(itemstack) && this.canBeSaddled()) {
            if (!this.getWorld().isClient) {
                ItemStack saddle = itemstack.split(1);
                this.items.setStack(0, saddle);
            }
            return true;
        }
        // If tame, equip armor
        if (this.isArmor(itemstack)) {
             if (this.items.getStack(1).isEmpty()) {
                if (!this.getWorld().isClient) {
                    ItemStack armor = itemstack.split(1);
                    this.items.setStack(1, armor);
                }
            }
            else {
                this.openInventory(player);
            }
            return true;
        }
        // Nothing left
        return false;
    }

    @Override
    public ActionResult interactMob(PlayerEntity player, Hand hand) {
        ItemStack itemstack = player.getStackInHand(hand);
        if (!this.isBaby()) {
            if (this.isTame() && player.shouldCancelInteraction()) {
                this.openInventory(player);
                return ActionResult.success(this.getWorld().isClient);
            }
        }

        // Only interact items with horses that aren't being ridden by another player
        if (!itemstack.isEmpty() && !this.hasPassengers()) {
            // Try to eat it
            if (this.isFood(itemstack)) {
                // Eat the item
                return this.interactHorse(player, itemstack);
            }
            // See if the item interacts with us
            ActionResult actionresulttype = itemstack.useOnEntity(player, this, hand);
            if (actionresulttype.isAccepted()) {
                return actionresulttype;
            }
            // See if we interact with the item
            if (itemInteract(player, itemstack, hand)) {
                return ActionResult.success(this.getWorld().isClient);
            }
        }

        if (!this.isBaby() && canFitRider(player)) {
            this.doPlayerRide(player);
            return ActionResult.success(this.getWorld().isClient);
        }
        // else
        return super.interactMob(player, hand);
    }

    // Not a real vanilla override on Fabric - see canParent() above.
    public boolean isFood(ItemStack stack) {
        return HorseConfig.isEquineFood(stack) || isBreedingFood(stack);
    }

    public boolean isBreedingFood(ItemStack stack) {
        return false;
    }

    protected int getGrowthBonus(ItemStack stack) {
        int growth = 20;
        if (stack.isOf(Blocks.HAY_BLOCK.asItem())) {
            growth *= 9;
        }
        else if (stack.isOf(Blocks.GRASS.asItem())) {
            return 4;
        }
        else if (stack.isOf(Blocks.TALL_GRASS.asItem())) {
            return 8;
        }
        else if (stack.isOf(Items.BROWN_MUSHROOM) || stack.isOf(Items.RED_MUSHROOM) || stack.isOf(Items.DRIED_KELP)) {
            return 2;
        }
        else if (stack.isOf(Items.SUGAR) || stack.isOf(Items.SWEET_BERRIES)) {
            return 10;
        }
        return growth;
    }

    protected int getTemperBonus(ItemStack stack) {
        if (stack.isOf(Items.GOLDEN_CARROT) || stack.isOf(Items.SUGAR) || stack.isOf(Items.SWEET_BERRIES)) {
            return 5;
        }
        else if (stack.isOf(Items.GOLDEN_APPLE) || stack.isOf(Items.ENCHANTED_GOLDEN_APPLE)) {
            return 10;
        }
        else if (stack.isOf(Items.BROWN_MUSHROOM) || stack.isOf(Items.RED_MUSHROOM) || stack.isOf(Items.DRIED_KELP)
                || stack.isOf(Blocks.GRASS.asItem()) || stack.isOf(Blocks.TALL_GRASS.asItem())) {
            return 1;
        }
        return 3;
    }

    protected float getHealthRegained(ItemStack stack) {
        if (stack.isOf(Blocks.HAY_BLOCK.asItem())) {
            return 20;
        }
        return 2;
    }

    @Override
    protected boolean receiveFood(PlayerEntity player, ItemStack stack) {
        boolean fed = false;

        if (isBaby()) {
            fed = true;
            if (!getWorld().isClient()) {
                growUp(getGrowthBonus(stack));
            }
            getWorld().addParticle(ParticleTypes.HAPPY_VILLAGER, getParticleX(1), getRandomBodyY() + 0.5, getParticleZ(1), 0, 0, 0);
        }
        else if (isTame() && isFertile() && !isInLove() && isBreedingFood(stack)) {
            fed = true;
            lovePlayer(player);
        }

        if (getHealth() < getMaxHealth()) {
            fed = true;
            heal(getHealthRegained(stack));
        }

        if (!isTame() && getTemper() < getMaxTemper()) {
            fed = true;
            if (!getWorld().isClient()) {
                addTemper(getTemperBonus(stack));
            }
        }

        if (fed) {
            this.setHorseFlag(64, true);
            this.eatingTicks = 1;
            emitGameEvent(GameEvent.EAT);
        }
        return fed;
    }

    protected void useGeneticAttributes()
    {
        if (HorseConfig.GENETICS.useGeneticStats.get())
        {
            EquineGenome genes = this.getGenome();
            float maxHealth = this.getGenome().getHealth();
            float athletics = genes.sumGenes(Gene.class, "athletics", 0, 4) / 2f
                                + genes.sumGenes(Gene.class, "athletics", 4, 8) / 2f;
            // Vanilla horse speed ranges from 0.1125 to 0.3375, as does ours
            float speedStat = genes.sumGenes(Gene.class, "speed", 0, 4)
                                + genes.sumGenes(Gene.class, "speed", 4, 8)
                                + genes.sumGenes(Gene.class, "speed", 8, 12)
                                + athletics;
            double movementSpeed = 0.1125D + speedStat * (0.225D / 32.0D);
            // Vanilla horse jump strength ranges from 0.4 to 1.0, as does ours
            float jumpStat = genes.sumGenes(Gene.class, "jump", 0, 4)
                                + genes.sumGenes(Gene.class, "jump", 4, 8)
                                + genes.sumGenes(Gene.class, "jump", 8, 12)
                                + athletics;
            double jumpStrength = 0.4D + jumpStat * (0.6D / 32.0D);

            this.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH).setBaseValue(maxHealth);
            this.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED).setBaseValue(movementSpeed);
            this.getAttributeInstance(EntityAttributes.HORSE_JUMP_STRENGTH).setBaseValue(jumpStrength);
        }
    }

    @Override
    public void breed(ServerWorld world, AnimalEntity mate) {
        // If vanilla mate, handle the vanilla way
        if (!(mate instanceof IGeneticEntity)) {
            super.breed(world, mate);
            return;
        }
        // If two animals go for the same mate at the same time this function
        // can be called twice. This check makes sure it only works the first time.
        if (!(this.isInLove() && mate.isInLove())) {
            return;
        }

        IGeneticEntity geneticMate = (IGeneticEntity)mate;

        // Call this on the female's side, if possible
        if (this.isMale() && !geneticMate.isMale()) {
            mate.breed(world, this);
            return;
        }

        // For use later in triggering the achievement
        ServerPlayerEntity serverplayerentity = this.getLovingPlayer();
        if (serverplayerentity == null && mate.getLovingPlayer() != null) {
            serverplayerentity = mate.getLovingPlayer();
        }

        int numFoals = this.getRandomLitterSize();
        List<PassiveEntity> foals = new ArrayList<>();
        for (int i = 0; i < numFoals; ++i) {

            PassiveEntity ageableentity = this.createChild(world, mate);
            // If ageableentity is null, leave this and the mate in love mode to try again
            if (ageableentity == null) {
                continue;
            }

            foals.add(ageableentity);

            if (serverplayerentity != null) {
                serverplayerentity.incrementStat(Stats.ANIMALS_BRED);
                Criteria.BRED_ANIMALS.trigger(serverplayerentity, this, mate, ageableentity);
            }
        }
        // Reset love state
        this.setBreedingAge(this.getRebreedTicks());
        mate.setBreedingAge(geneticMate.getRebreedTicks());
        this.resetLoveTicks();
        mate.resetLoveTicks();


        if (foals.size() <= 0) {
            // Spawn smoke particles to indicate failure
            this.getWorld().sendEntityStatus(this, (byte)6);
            // Only spawn XP and grant the achievement for successful births
            return;
        }
        // Make twins and triplets smaller as there is less space for them in the womb
        float multiplier = (float)Math.pow(1./foals.size(), 1./3.);
        for (PassiveEntity foal : foals) {
            if (foal instanceof IGeneticEntity) {
                IGeneticEntity gFoal = (IGeneticEntity)foal;
                gFoal.setMotherSize(gFoal.getMotherSize() * multiplier);
            }

            // Set pregnant or spawn into world directly
            if (!HorseConfig.isPregnancyEnabled() || !setPregnantWith(foal, mate)) {
                spawnChild(foal, world);
            }
        }

        if (HorseConfig.isPregnancyEnabled()) {
            // Spawn heart particles
            this.getWorld().sendEntityStatus(this, (byte)18);
        }

        // Spawn XP orbs
        if (world.getGameRules().getBoolean(GameRules.DO_MOB_LOOT)) {
            int xp = this.getRandom().nextInt(7) + 1;
            world.spawnEntity(new ExperienceOrbEntity(world, this.getX(), this.getY(), this.getZ(), xp));
        }
    }

    private void spawnChild(PassiveEntity child, ServerWorld world) {
        child.setBaby(true);
        child.refreshPositionAndAngles(this.getX(), this.getY(), this.getZ(), 0.0F, 0.0F);
        if (child instanceof AbstractHorseGenetic) {
            ((AbstractHorseGenetic)child).setSeed(getRandom().nextInt());
        }
        world.spawnEntityAndPassengers(child);
        // Spawn heart particles
        world.sendEntityStatus(this, (byte)18);
    }

    // Helper function for createChild that creates and spawns an entity of the
    // correct species
    abstract AbstractHorseEntity getChild(ServerWorld world, PassiveEntity otherparent);

    // Returns the number of conceptions that survive pregnancy
    // This can return different numbers when called on the same animal at
    // different times.
    protected int getRandomLitterSize() {
        // Since this is Minecraft, skip twin births where one or both twins die,
        // and make twin births much more rare
        // If the frequency of double_ovulation is 0.2, the probability of triplets
        // works out to within an order of magnitude of the expected 1 in 300,000.
        double chance = 1 / 10000;
        if (getGenome().countAlleles(Gene.double_ovulation, 1) == 1) {
            chance = 1 / 5000;
        }
        else if (getGenome().isHomozygous(Gene.double_ovulation, 1)) {
            chance = 1 / 1000;
        }

        int litterSize = 1;
        if (getRandom().nextDouble() < chance) {
            litterSize += 1;
        }
        if (getRandom().nextDouble() < chance) {
            litterSize += 1;
        }
        return litterSize;
    }

    public boolean isOppositeGender(AbstractHorseGenetic other) {
        if (!HorseConfig.isGenderEnabled()) {
            return true;
        }
        return this.isMale() != other.isMale();
    }

    public boolean isDirectRelative(AbstractHorseGenetic other) {
        boolean isParent = this.uuid != null && (this.uuid == other.motherUUID || this.uuid == other.fatherUUID);
        boolean isChild = other.uuid != null && (this.motherUUID == other.uuid || this.fatherUUID == other.uuid);
        boolean sharesMother = this.motherUUID != null && (this.motherUUID == other.motherUUID || this.motherUUID == other.fatherUUID);
        boolean sharesFather = this.fatherUUID != null && (this.fatherUUID == other.fatherUUID || this.fatherUUID == other.motherUUID);
        return isParent || isChild || sharesMother || sharesFather;
    }

    public boolean isGroundTied() {
        return HorseConfig.COMMON.enableGroundTie.get() && this.isSaddled();
    }

    public boolean canAutobreed() {
        // Check elsewhere if autobreeding is allowed in the config
        boolean notArmored = this.items.getStack(1).isEmpty();
        return !hasPassengers() && !isLeashed() && !isSaddled() && !hasChest() && notArmored
            && (!isTame() || isAutobreedable()) && isFertile() && getBreedingAge() == 0;
    }

    @Override
    public PassiveEntity createChild(ServerWorld world, PassiveEntity ageable)
    {
        if (!(ageable instanceof AnimalEntity)) {
            return null;
        }
        AnimalEntity otherAnimal = (AnimalEntity)ageable;
        // Have the female create the child if possible
        if (this.isMale()
                && ageable instanceof AbstractHorseGenetic
                && !((AbstractHorseGenetic)ageable).isMale()) {
            return ageable.createChild(world, this);
        }
        AbstractHorseEntity child = this.getChild(world, ageable);
        if (child != null) {
            this.setChildAttributes(ageable, child);
        }
        if (child instanceof AbstractHorseGenetic) {
            AbstractHorseGenetic foal = (AbstractHorseGenetic)child;
            if (ageable instanceof AbstractHorseGenetic) {
                AbstractHorseGenetic other = (AbstractHorseGenetic)ageable;
                foal.getGenome().inheritGenes(this.getGenome(), other.getGenome());
                // No child is born for genotypes strongly suspected to be embryonic lethal, or re-roll for those
                // that we're not sure about
                while (foal.getGenome().isEmbryonicLethal() || foal.getGenome().isMaybeEmbryonicLethal())
                {
                    if (foal.getGenome().isEmbryonicLethal()) {
                        return null;
                    }
                    else if (foal.getGenome().isMaybeEmbryonicLethal()) {
                        foal.getGenome().inheritGenes(this.getGenome(), other.getGenome());
                    }
                }
            }
            foal.setMotherSize(this.getGenome().getAdultScale());
            foal.setMale(this.random.nextBoolean());
            foal.useGeneticAttributes();
            foal.setBreedingAge(HorseConfig.GROWTH.getMinAge());
            foal.motherUUID = this.uuid;
            foal.fatherUUID = ageable.getUuid();
        }
        return child;
    }

    @Override
    public boolean setPregnantWith(PassiveEntity child, PassiveEntity otherParent) {
        if (otherParent instanceof IGeneticEntity) {
            IGeneticEntity otherGenetic = (IGeneticEntity)otherParent;
            if (this.isMale() == otherGenetic.isMale()) {
                return false;
            }
            else if (this.isMale() && !otherGenetic.isMale()) {
                return otherGenetic.setPregnantWith(child, this);
            }
        }
        if (this.isMale()) {
            return false;
        }

        if (child instanceof AbstractHorseGenetic) {
            unbornChildren.add((AbstractHorseGenetic)child);
            if (!this.getWorld().isClient) {
                // Can't be a child
                this.trueAge = Math.max(0, this.trueAge);
                this.dataTracker.set(PREGNANT_SINCE, this.trueAge);
            }
            return true;
        }
        return false;
    }

    public void fleeFrom(Entity entity) {
        fleeGoal.toAvoid = entity;
    }

    public void oust(AbstractHorseGenetic competitor, AbstractHorseGenetic mare) {
        oustGoal.target = competitor;
        oustGoal.stayNear = mare;
    }

    public boolean isDrivingAwayCompetitor() {
        if (oustGoal.target != null) {
            lastOustTime = age;
        }
        return age - lastOustTime < 400;
    }

    /**
     * Called to update the entity's position/logic.
     */
    @Override
    public void tick()
    {
        super.tick();
        // Keep track of age
        if (!this.getWorld().isClient) {
            // For children, align with growing age in case they have been fed
            if (this.breedingAge < 0) {
                this.trueAge = this.breedingAge;
            }
            else {
                this.trueAge = Math.max(0, Math.max(trueAge, trueAge + 1));
            }
            // Allow imprecision
            final int c = 400;
            if (this.trueAge / c != this.getDisplayAge() / c
                    || (this.trueAge < 0 != this.getDisplayAge() < 0)) {
                this.setDisplayAge(this.trueAge);
            }
        }

        // Pregnancy
        if (!this.getWorld().isClient && this.isPregnant()) {
            // Check pregnancy
            if (this.unbornChildren == null
                    || this.unbornChildren.size() == 0) {
                this.dataTracker.set(PREGNANT_SINCE, -1);
            }
            // Handle birth
            int totalLength = HorseConfig.getHorsePregnancyLength();
            int currentLength = this.trueAge - this.getPregnancyStart();
            if (currentLength >= totalLength) {
                for (AbstractHorseGenetic child : unbornChildren) {
                    if (this.getWorld() instanceof ServerWorld) {
                        this.spawnChild(child, (ServerWorld)this.getWorld());
                    }
                }
                this.unbornChildren = new ArrayList<>();
                this.dataTracker.set(PREGNANT_SINCE, -1);
            }
        }

         else if (HorseConfig.BREEDING.autobreeding.get()
                 && !this.getWorld().isClient
                 && age % 800 == 0
                 && (!isMale() || !HorseConfig.isGenderEnabled())
                 && canAutobreed()
                 && random.nextFloat() < 0.05f) {
            List<AbstractHorseGenetic> equines = getWorld().getEntitiesByClass(AbstractHorseGenetic.class, getBoundingBox().expand(16, 12, 16), (e) -> true);
            if (equines.size() < 16) {
                lovePlayer(null);
                AbstractHorseGenetic stallion = equines
                    .stream()
                    .filter((h) -> isOppositeGender(h) && h.canAutobreed() && !isDirectRelative(h))
                    .sorted((h1, h2) -> Double.compare(h1.squaredDistanceTo(this), h2.squaredDistanceTo(this)))
                    .findFirst()
                    .orElse(null);
                if (stallion != null) {
                    stallion.lovePlayer(null);
                }
            }
        }

        // Overo lethal white syndrome
        if (this.getGenome().isLethalWhite()
            && this.age > 80)
        {
            if (!this.hasStatusEffect(StatusEffects.WITHER))
            {
                this.addStatusEffect(new StatusEffectInstance(StatusEffects.WITHER, 100, 3));
            }
        }

        ItemStack stack = this.items.getStack(1);
    }

    @Override
    public void tickMovement() {
        if (this.unbornChildren != null && this.unbornChildren.size() > 0
                && this.getPregnancyStart() < 0) {
            this.dataTracker.set(PREGNANT_SINCE, 0);
        }

        if (this.getGenome().isHomozygous(Gene.leopard, HorseAlleles.LEOPARD) && !this.getWorld().isClient()) {
            EntityAttributeInstance speedAttribute = this.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED);
            EntityAttributeInstance jumpAttribute = this.getAttributeInstance(EntityAttributes.HORSE_JUMP_STRENGTH);
            float brightness = this.getWorld().getLightLevel(this.getSteppingPos());
            if (brightness > 0.5f) {
                if (speedAttribute.getModifier(CSNB_SPEED_UUID) != null) {
                    speedAttribute.removeModifier(CSNB_SPEED_MODIFIER);
                }
                if (jumpAttribute.getModifier(CSNB_JUMP_UUID) != null) {
                    jumpAttribute.removeModifier(CSNB_JUMP_MODIFIER);
                }
            }
            else {
                if (speedAttribute.getModifier(CSNB_SPEED_UUID) == null) {
                    speedAttribute.addTemporaryModifier(CSNB_SPEED_MODIFIER);
                }
                if (jumpAttribute.getModifier(CSNB_JUMP_UUID) == null) {
                    jumpAttribute.addTemporaryModifier(CSNB_JUMP_MODIFIER);
                }
            }
        }

        super.tickMovement();
    }

    // Returns the Y offset from the entity's position for any entity riding this one.
    @Override
    public double getMountedHeightOffset() {
        return (double)this.getHeight() * 0.833 - 0.295;
    }

    @Override
    @Nullable
    public LivingEntity getControllingPassenger() {
        if (this.isSaddled()) {
            Entity entity = this.getFirstPassenger();
            if (entity instanceof LivingEntity) {
                LivingEntity rider = (LivingEntity)entity;
                if (!(rider instanceof AnimalEntity) && (rider instanceof PlayerEntity || !this.isLeashed())) {
                    return rider;
                }
            }
        }

        return null;
    }

    @Override
    // Overriden so passenger position while rearing depends on the horse's size,
    // also to support multiple passengers.
    protected void updatePassengerPosition(Entity passenger, Entity.PositionUpdater positionUpdater) {
        if (!this.hasPassenger(passenger)) {
            return;
        }
        // Do not call super.updatePassengerPosition. AbstractHorseEntity's implementation
        // sets this's bodyYaw to that of the passenger without
        // checking that the passenger can steer.
        // Setting yaw happens in AbstractHorseEntity.travel

        float xzOffset = -0.1f;
        if (this.getPassengerList().size() > 1) {
            int i = this.getPassengerList().indexOf(passenger);
            if (i == 0) {
                xzOffset = 0.1f;
            } else {
                xzOffset = -0.5f;
            }
        }
        xzOffset *= this.getGenome().getAdultScale();

        double yOffset = this.getMountedHeightOffset() + passenger.getHeightOffset();
        // Compensate for saddle for players
        if (passenger instanceof PlayerEntity && this.isSaddled()) {
            yOffset += 0.04 * this.getGenome().getAdultScale();
        }
        float standAnim0 = this.getAngryAnimationProgress(0f);
        if (standAnim0 > 0.0F) {
            float xLoc = this.getWidth() + xzOffset;
            float facingX = MathHelper.sin(this.bodyYaw * ((float)Math.PI / 180F));
            float facingZ = MathHelper.cos(this.bodyYaw * ((float)Math.PI / 180F));
            // A rearing amount of 1 corresponds to 45 degrees up
            float rearAngle = standAnim0 * (float)Math.PI / 4F;
            // The y distance from the top of the back to the bottom of the belly (10 pixels)
            float bodyHeight = 10F / 16F * this.getGenome().getAdultScale();
            float rearXZ = xLoc * (MathHelper.cos(rearAngle) - 1F) - bodyHeight * MathHelper.sin(rearAngle);
            float rearY = MathHelper.sin(rearAngle) * xLoc / 2F;
            xzOffset += rearXZ;
            yOffset += rearY;
            if (passenger instanceof LivingEntity) {
                ((LivingEntity)passenger).bodyYaw = this.bodyYaw;
            }
        }

        // Here boats use this.yaw, but we use this.bodyYaw,
        // because this.yaw doesn't change when the unsaddled horse moves around
        Vec3d vector3d = new Vec3d((double)xzOffset, 0.0D, 0.0D);
        vector3d = vector3d.rotateY(-this.bodyYaw * ((float)Math.PI / 180F) - ((float)Math.PI / 2F));
        positionUpdater.accept(passenger, this.getX() + vector3d.x, this.getY() + yOffset, this.getZ() + vector3d.z);
        if (!(passenger instanceof PlayerEntity)) {
            passenger.setBodyYaw(this.bodyYaw);
            passenger.setYaw(this.bodyYaw);
            passenger.setHeadYaw(this.bodyYaw);
        }
        if (passenger instanceof AnimalEntity && this.getPassengerList().size() > 1) {
            int degrees = passenger.getId() % 2 == 0 ? 90 : 270;
            passenger.setBodyYaw(((AnimalEntity)passenger).bodyYaw + (float)degrees);
            passenger.setHeadYaw(passenger.getHeadYaw() + (float)degrees);
        }
    }

    @Override
    protected Text getDefaultName() {
        String species = this.getSpecies().toString().toLowerCase();
        String s = "entity." + HorseColors.MOD_ID + "." + species + ".";
        if (this.isBaby()) {
            // Foal
            if (!HorseConfig.BREEDING.enableGenders.get()) {
                return Text.translatable(s + "foal");
            }
            // Colt
            if (this.isMale()) {
                return Text.translatable(s + "colt");
            }
            // Filly
            return Text.translatable(s + "filly");
        }

        // Horse
        if (!HorseConfig.BREEDING.enableGenders.get()) {
            return super.getDefaultName();
        }
        // Stallion
        if (this.isMale()) {
            return Text.translatable(s + (this.isFertile() ? "male" : "neuter"));
        }
        // Mare
        return Text.translatable(s + "female");
    }

    public boolean isSaddle(ItemStack stack) {
        if (stack.isEmpty() || stack.isOf(Items.SADDLE)) {
            return true;
        }
        Identifier name = Registries.ITEM.getId(stack.getItem());
        return name.getNamespace().equals("eanimod") && name.getPath().startsWith("saddle");
    }

    // Override to allow alternate saddles to be equipped
    @Override
    public StackReference getStackReference(int slot) {
        int num = slot - 400;
        if (num == 0) {
            return new StackReference() {
                public ItemStack get() {
                    return AbstractHorseGenetic.this.items.getStack(slot);
                }

                public boolean set(ItemStack stack) {
                    if (!isSaddle(stack)) {
                        return false;
                    }
                    else {
                        AbstractHorseGenetic.this.items.setStack(slot, stack);
                        AbstractHorseGenetic.this.updateSaddle();
                        return true;
                    }
                }
            };
        }
        return super.getStackReference(slot);
    }

    @Override
    // This is needed so when the mutation chance is high, mules bred
    // with spawn eggs do not produce all splashed white foals.
    public Breed getDefaultBreed() {
        return BaseEquine.breed;
    }

    // Randomize only health, for mules and donkeys
    @Override
    protected void initAttributes(Random rand) {
        // Set stats for vanilla-like breeding
        if (!HorseConfig.GENETICS.useGeneticStats.get()) {
            float maxHealth = this.getChildHealthBonus(rand::nextInt) + this.getGenome().getBaseHealth();
            this.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH).setBaseValue((double)maxHealth);
        }
    }

    /**
     * Called only once on an entity when first time spawned, via egg, mob spawner, natural spawning etc, but not called
     * when entity is reloaded from nbt. Mainly used for initializing attributes and inventory
     */
    @Nullable
    @Override
    public EntityData initialize(ServerWorldAccess worldIn,
                                            LocalDifficulty difficultyIn,
                                            SpawnReason reason,
                                            @Nullable EntityData spawnData,
                                            @Nullable NbtCompound dataTag)
    {
        this.dataTracker.set(PREGNANT_SINCE, -1);
        if (!(spawnData instanceof GeneticData)) {
            spawnData = new GeneticData(getRandomBreed());
        }
        // super.initialize will call initAttributes
        spawnData = super.initialize(worldIn, difficultyIn, reason, spawnData, dataTag);
        GeneticData geneticData = (GeneticData)spawnData;
        randomizeGenes(geneticData.breed);
        setMale(random.nextBoolean());
        boolean foal = !super.isTame() && random.nextInt(4) == 0;
        if (reason == SpawnReason.NATURAL || reason == SpawnReason.CHUNK_GENERATION) {
            foal = foal && geneticData.getSpawnedCount() > 1;
            if (!foal) {
                setMale(geneticData.getSpawnedCount() <= 1);
            }
        }
        if (foal) {
            // Foals pick a random age within the younger half
            trueAge = getBirthAge() + random.nextInt(-getBirthAge() / 2);
        }
        else {
            trueAge = random.nextInt(HorseConfig.GROWTH.getMaxAge());
        }
        // Don't set the growing age to a positive value, that would be bad
        setBreedingAge(Math.min(0, trueAge));
        this.useGeneticAttributes();
        return spawnData;
    }

    protected void randomizeGenes(Breed breed) {
        setSeed(random.nextInt());
        this.getGenome().randomize(breed);
        this.useGeneticAttributes();
        // Assume mother was the same size
        this.setMotherSize(this.getGenome().getGeneticScale());
        // Size depends on mother size so call again to stabilize somewhat
        this.setMotherSize(this.getGenome().getGeneticScale());
    }

    public void initFromVillageSpawn() {
        randomizeGenes(getRandomBreed());
        setMale(random.nextBoolean());
        trueAge = random.nextInt(HorseConfig.GROWTH.getMaxAge());
        setBreedingAge(Math.min(0, trueAge));
        // All village horses are easier to tame
        this.addTemper(this.getMaxTemper() / 2);
        if (!this.isBaby() && this.random.nextInt(16) == 0) {
            // Tame and saddle
            this.setTame(true);
            ItemStack saddle = new ItemStack(Items.SADDLE);
            this.items.setStack(0, saddle);
        }
    }

    // Total size change based on age that does not change proportions
    public float getProportionalAgeScale() {
        return getGenome().getCurrentScale() / getGangliness();
    }

    // The horse model uses this number to decide how foal-shaped to make the
    // horse. 0.5 is the most foal-shaped and 1 is the most adult-shaped.
    public float getGangliness() {
        return 0.5f + 0.5f * getFractionGrown() * getFractionGrown();
    }

    // Affects hitbox size.
    @Override
    public float getScaleFactor() {
        // This is different from LivingEntity.getScaleFactor which uses
        // 0.5 for children
        float base = isBaby()? 0.6f : 1.0f;
        if (getGenome() == null) {
            // This can happen if getScaleFactor() is indirectly called from
            // Entity's constructor, before our own constructor has run and the
            // genome is still null.
            return base;
        }
        return this.getGenome().getAdultScale() * base;
    }

    public boolean isTooSmallForPlayerToRide() {
        return HorseConfig.COMMON.enableSizes.get() && getGenome().isMiniature()
            && !HorseConfig.COMMON.rideSmallEquines.get();
    }

    @Override
    public boolean canBeSaddled() {
        return !isTooSmallForPlayerToRide() && super.canBeSaddled();
    }

    @Override
    public boolean isPushable() {
        return !(this.hasPassengers()
            && this.getControllingPassenger() instanceof PlayerEntity);
    }

    // For holding spawn data
    public static class GeneticData extends PassiveEntity.PassiveData {
        public final Breed breed;

        public GeneticData(Breed breed) {
            super(true);
            this.breed = breed;
        }
    }
}
