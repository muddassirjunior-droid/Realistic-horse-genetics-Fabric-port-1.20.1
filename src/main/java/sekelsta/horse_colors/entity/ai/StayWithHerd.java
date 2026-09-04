package sekelsta.horse_colors.entity.ai;

import java.util.*;
import java.util.stream.Stream;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.passive.AbstractHorseEntity;

import sekelsta.horse_colors.HorseColors;
import sekelsta.horse_colors.HorseConfig;
import sekelsta.horse_colors.entity.AbstractHorseGenetic;

public class StayWithHerd extends Goal {
    protected final AbstractHorseGenetic horse;
    protected AbstractHorseEntity target;
    protected float distanceModifier = 1;

    protected double walkSpeed = 1;
    protected double runSpeed = 1.4;
    protected int timeUntilRecalculatePath = 0;

    protected int lastSearchTick = 0;
    protected int acceptableDelay = 50;

    public StayWithHerd(AbstractHorseGenetic horse) {
        this.horse = horse;
        setControls(EnumSet.of(Goal.Control.MOVE));
    }

    protected double closeEnoughDistance() {
        return 2 + 10 * horse.getFractionGrown();
    }

    protected double tooFarDistance() {
        return 16 + 8 * horse.getFractionGrown();
    }

    @Override
    public boolean canStart() {
        if (horse.hasPassengers() || horse.isLeashed() || horse.isGroundTied()) {
            return false;
        }

        distanceModifier = 1;
        if (horse.isBaby() && target != null && target.isAlive() && target.getUuid().equals(horse.getMotherUUID())) {
            double distSq = target.squaredDistanceTo(horse);
            double min = closeEnoughDistance();
            if (distSq < min * min) {
                return false;
            }
            double max = tooFarDistance();
            if (distSq < max * max) {
                return true;
            }
        }

        float age = horse.getFractionGrown();
        if (horse.age - lastSearchTick > acceptableDelay * age) {
            lastSearchTick = horse.age + horse.getRandom().nextInt(8);
            double horizontalSearch = 4 + 16 * age;
            double verticalSearch = 4 + 8 * age;
            List<AbstractHorseEntity> equines = horse.getWorld().getEntitiesByClass(AbstractHorseEntity.class, horse.getBoundingBox().expand(horizontalSearch, verticalSearch, horizontalSearch), (e) -> true);
            target = getBestTarget(equines.stream().filter((h) -> h != horse).toList());
        }
        return shouldContinue();
    }

    @Override
    public boolean shouldContinue() {
        if (target == null || !target.isAlive()) {
            return false;
        }
        if (target instanceof AbstractHorseGenetic && ((AbstractHorseGenetic)target).isDrivingAwayCompetitor()) {
            return false;
        }
        if (!HorseConfig.COMMON.herdsFollowRidden.get() && !horse.isBaby() && (target.hasPassengers() || target.isLeashed())) {
            return false;
        }
        if (!HorseConfig.COMMON.herdingWhenTame.get() && !horse.isBaby() && horse.isTame()) {
            return false;
        }
        double distSq = target.squaredDistanceTo(horse);
        double max = tooFarDistance() * distanceModifier;
        double min = closeEnoughDistance() * distanceModifier;
        return (distSq < max * max || horse.isDrivingAwayCompetitor()) && distSq > min * min;
    }

    public int bestMother(AbstractHorseEntity h1, AbstractHorseEntity h2) {
        if (h1.getUuid().equals(horse.getMotherUUID())) {
            return -1;
        }
        else if (h2.getUuid().equals(horse.getMotherUUID())) {
            return 1;
        }
        boolean h1b = h1.isBaby();
        boolean h2b = h2.isBaby();
        if (h1b != h2b) {
            return Boolean.compare(h2b, h1b);
        }
        return nearestIdeallyMatchingClass(h1, h2);
    }

    public int nearestIdeallyMatchingClass(AbstractHorseEntity h1, AbstractHorseEntity h2) {
        boolean h1c = h1.getClass().equals(horse.getClass());
        boolean h2c = h2.getClass().equals(horse.getClass());
        if (h1c != h2c) {
            return Boolean.compare(h2c, h1c);
        }
        return Double.compare(h1.squaredDistanceTo(horse), h2.squaredDistanceTo(horse));
    }

    public int strongestIdeallyMatchingClass(AbstractHorseEntity h1, AbstractHorseEntity h2) {
        boolean h1c = h1.getClass().equals(horse.getClass());
        boolean h2c = h2.getClass().equals(horse.getClass());
        if (h1c != h2c) {
            return Boolean.compare(h2c, h1c);
        }
        return Float.compare(h2.getMaxHealth(), h1.getMaxHealth());
    }

    public int highestHealth(AbstractHorseEntity h1, AbstractHorseEntity h2) {
        return Float.compare(h2.getMaxHealth(), h1.getMaxHealth());
    }

    public AbstractHorseEntity getBestTarget(List<AbstractHorseEntity> equines) {
        if (horse.isBaby()) {
            return equines.stream().sorted(this::bestMother).findFirst().orElse(null);
        }

        if (horse.isDrivingAwayCompetitor() && horse.oustGoal.target == null && horse.oustGoal.stayNear != null) {
            return horse.oustGoal.stayNear;
        }

        List<AbstractHorseGenetic> geneticEquines = new ArrayList<>();
        List<AbstractHorseEntity> vanillaEquines = new ArrayList<>();
        for (AbstractHorseEntity h : equines) {
            if (!HorseConfig.COMMON.herdsFollowRidden.get() && (h.hasPassengers() || h.isLeashed())) {
                continue;
            }
            if (h instanceof AbstractHorseGenetic) {
                geneticEquines.add((AbstractHorseGenetic)h);
            }
            else {
                vanillaEquines.add(h);
            }
        }

        if (!horse.isMale() || !HorseConfig.BREEDING.enableGenders.get()) {
            AbstractHorseGenetic foal = null;
            for (AbstractHorseGenetic h : geneticEquines) {
                if (h.isBaby() && horse.getUuid().equals(h.getMotherUUID())) {
                    if (foal == null || foal.squaredDistanceTo(horse) < h.squaredDistanceTo(horse)) {
                        foal = h;
                    }
                }
            }
            if (foal != null && foal.squaredDistanceTo(horse) > 12 * 12) {
                distanceModifier = 0.1f * foal.getFractionGrown();
                return foal;
            }
        }
        else if (horse.isFertile()) {
            AbstractHorseGenetic mare = geneticEquines.stream()
                .filter((h) -> isFertileMare(h) && h.getClass().equals(horse.getClass()))
                .sorted((h1, h2) -> Double.compare(h1.squaredDistanceTo(horse), h2.squaredDistanceTo(horse)))
                .findFirst().orElse(null);
            if (mare != null) {
                if (!HorseConfig.COMMON.jealousStallions.get()) {
                    return mare;
                }
                AbstractHorseGenetic competitor = geneticEquines.stream()
                    .filter((h) -> isFertileStallion(h) && !h.isLeashed() && !h.hasPassengers())
                    .sorted(this::highestHealth)
                    .findFirst().orElse(null);
                if (competitor == null) {
                    return mare;
                }
                if (competitor.getMaxHealth() < horse.getMaxHealth()) {
                    horse.oust(competitor, mare);
                    return null;
                }
            }
        }

        AbstractHorseEntity t = geneticEquines.stream()
            .sorted(horse.isMale() ? this::nearestIdeallyMatchingClass : this::strongestIdeallyMatchingClass)
            .findFirst().orElse(null);
        if (t == null) {
            t = vanillaEquines.stream().sorted(this::highestHealth).findFirst().orElse(null);
        }
        if (t != null && t.getMaxHealth() <= horse.getMaxHealth()) {
            return null;
        }
        return t;
    }

    public boolean isFertileMare(AbstractHorseGenetic h) {
        return !h.isBaby() && !h.isMale() && h.isFertile();
    }

    public boolean isFertileStallion(AbstractHorseGenetic h) {
        return !h.isBaby() && h.isMale() && h.isFertile();
    }

    @Override
    public void start() {
        timeUntilRecalculatePath = 0;
    }

    @Override
    public void tick() {
        timeUntilRecalculatePath -= 1;
        if (timeUntilRecalculatePath < 0) {
            timeUntilRecalculatePath = getTickCount(10);
            double speed = horse.isBaby() || horse.squaredDistanceTo(target) > 16 * 16 ? runSpeed : walkSpeed;
            horse.getNavigation().startMovingTo(target, speed);
        }
    }
}
