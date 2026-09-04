package sekelsta.horse_colors.entity.ai;

import java.util.function.Predicate;
import net.minecraft.entity.ai.goal.FleeEntityGoal;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.AbstractHorseEntity;

public class SpookGoal<T extends LivingEntity> extends FleeEntityGoal<T> {

    public SpookGoal(AbstractHorseEntity entityIn, Class<T> classToAvoidIn, float avoidDistanceIn, double farSpeedIn, double nearSpeedIn) {
        super(entityIn, classToAvoidIn, avoidDistanceIn, farSpeedIn, nearSpeedIn);
    }

    public SpookGoal(AbstractHorseEntity entityIn, Class<T> avoidClass, Predicate<LivingEntity> targetPredicate, float distance, double nearSpeedIn, double farSpeedIn, Predicate<LivingEntity> predicate) {
        super(entityIn, avoidClass, targetPredicate, distance, nearSpeedIn, farSpeedIn, predicate);
    }

    public SpookGoal(AbstractHorseEntity entityIn, Class<T> avoidClass, float distance, double nearSpeedIn, double farSpeedIn, Predicate<LivingEntity> targetPredicate) {
        super(entityIn, avoidClass, distance, nearSpeedIn, farSpeedIn, targetPredicate);
    }

    @Override
    public void start() {
        AbstractHorseEntity horse = (AbstractHorseEntity)this.mob;
        if (horse.isVehicle()) {
            horse.removeAllPassengers();
            horse.updateAnger();
        }
        super.start();
    }


}
