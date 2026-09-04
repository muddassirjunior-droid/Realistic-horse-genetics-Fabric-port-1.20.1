package sekelsta.horse_colors.entity.ai;

import java.util.EnumSet;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.pathing.Path;
import net.minecraft.util.math.Vec3d;

import sekelsta.horse_colors.HorseColors;
import sekelsta.horse_colors.entity.AbstractHorseGenetic;

public class OustGoal extends Goal {
    public final AbstractHorseGenetic entity;
    public AbstractHorseGenetic target = null;
    public AbstractHorseGenetic stayNear = null;
    public float stayNearDistance = 16;
    public float maxDist = 18;
    public float runSpeed = 1.2f;
    public float walkSpeed = 0.9f;
    protected Path path = null;

    public OustGoal(AbstractHorseGenetic entityIn) {
        this.entity = entityIn;
        setControls(EnumSet.of(Goal.Control.MOVE));
    }

    @Override
    public boolean canStart() {
        if (target == null) {
            return false;
        }
        if (stayNear == null) {
            return true;
        }
        if (entity.squaredDistanceTo(stayNear) > stayNearDistance * stayNearDistance) {
            return false;
        }
        if (!shouldContinue()) {
            return false;
        }
        return true;
    }

    @Override
    public boolean shouldContinue() {
        if (entity.isGroundTied() || entity.isLeashed() || entity.isVehicle()
                || target.isLeashed() || target.isVehicle()) {
            return false;
        }
        return entity.squaredDistanceTo(target) < maxDist * maxDist && !entity.getNavigation().isIdle()
            && (stayNear == null || entity.squaredDistanceTo(stayNear) < stayNearDistance * stayNearDistance);
    }

    @Override
    public void start() {
        entity.getNavigation().startMovingTo(target, walkSpeed);
        target.fleeFrom(entity);
    }

    @Override
    public void stop() {
        target = null;
    }

    @Override
    public void tick() {
        if (entity.squaredDistanceTo(target) < 49.0) {
            entity.getNavigation().setSpeed(walkSpeed);
        }
        else {
            entity.getNavigation().setSpeed(runSpeed);
        }
    }
}
