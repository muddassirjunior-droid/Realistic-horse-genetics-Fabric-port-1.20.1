package sekelsta.horse_colors.entity.ai;

import java.util.EnumSet;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.NoPenaltyTargeting;
import net.minecraft.entity.ai.FuzzyTargeting;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.pathing.Path;
import net.minecraft.util.math.Vec3d;

import sekelsta.horse_colors.HorseColors;
import sekelsta.horse_colors.entity.AbstractHorseGenetic;

public class FleeGoal extends Goal {
    public final AbstractHorseGenetic entity;
    public Entity toAvoid = null;
    public float maxDist = 24;
    public float runSpeed = 1.6f;
    public float walkSpeed = 1f;
    protected Path path = null;

    public FleeGoal(AbstractHorseGenetic entityIn) {
        this.entity = entityIn;
        setControls(EnumSet.of(Goal.Control.MOVE));
    }

    @Override
    public boolean canStart() {
        if (toAvoid == null) {
            return false;
        }
        if (!shouldContinue()) {
            return false;
        }
        setPath();
        return path != null;
    }

    private void setPath() {
        path = null;
        Vec3d loc = NoPenaltyTargeting.findFrom(entity, 16, 7, toAvoid.getPos());
        if (loc == null) {
            loc = FuzzyTargeting.findFrom(entity, 12, 7, toAvoid.getPos());
        }
        if (loc != null) {
            path = entity.getNavigation().findPathTo(loc.x, loc.y, loc.z, 0);
        }
    }

    @Override
    public boolean shouldContinue() {
        if (entity.isGroundTied() || entity.isLeashed() || entity.isVehicle()) {
            return false;
        }
        return entity.squaredDistanceTo(toAvoid) < maxDist * maxDist;
    }

    @Override
    public void start() {
        entity.getNavigation().startMovingAlong(path, walkSpeed);
    }

    @Override
    public void stop() {
        toAvoid = null;
    }

    @Override
    public void tick() {
        if (entity.getNavigation().isIdle()) {
            setPath();
            entity.getNavigation().startMovingAlong(path, walkSpeed);
        }
        if (entity.squaredDistanceTo(toAvoid) < 49.0) {
            entity.getNavigation().setSpeed(runSpeed);
        }
        else {
            entity.getNavigation().setSpeed(walkSpeed);
        }
    }
}
