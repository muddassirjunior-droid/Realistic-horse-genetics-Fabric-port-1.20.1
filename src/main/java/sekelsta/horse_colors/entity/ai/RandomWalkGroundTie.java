package sekelsta.horse_colors.entity.ai;

import net.minecraft.entity.ai.goal.WanderAroundFarGoal;

import sekelsta.horse_colors.entity.AbstractHorseGenetic;

public class RandomWalkGroundTie extends WanderAroundFarGoal {
    protected AbstractHorseGenetic horse = null;

    public RandomWalkGroundTie(AbstractHorseGenetic creature, double speedIn) {
        super(creature, speedIn);
        this.horse = creature;
    }

    public RandomWalkGroundTie(AbstractHorseGenetic creature, double speedIn, float probabilityIn) {
        super(creature, speedIn, probabilityIn);
        this.horse = creature;
    }

    @Override
    public boolean canStart() {
        if (horse.isGroundTied()) {
            return false;
        }
        return super.canStart();
    }
}
