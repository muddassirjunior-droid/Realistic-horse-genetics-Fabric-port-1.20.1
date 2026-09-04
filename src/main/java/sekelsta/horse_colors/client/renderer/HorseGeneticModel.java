package sekelsta.horse_colors.client.renderer;

import com.google.common.collect.ImmutableList;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.entity.model.AnimalModel;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.model.Dilation;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.util.math.MathHelper;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import org.jetbrains.annotations.NotNull;
import java.util.function.Consumer;

import sekelsta.horse_colors.entity.AbstractHorseGenetic;

import net.minecraft.entity.passive.AbstractDonkeyEntity;
import net.minecraft.entity.passive.AbstractHorseEntity;

@Environment(EnvType.CLIENT)
public class HorseGeneticModel<T extends AbstractHorseEntity> extends AnimalModel<T>
{
    private static final String BODY = "body";
    private static final String HEAD = "head";
    private static final String NECK = "neck";

    private static final String BACK_LEFT_THIGH = "back_left_thigh";
    private static final String BACK_LEFT_SHIN = "back_left_shin";
    private static final String BACK_LEFT_HOOF = "back_left_hoof";
    private static final String BACK_RIGHT_THIGH = "back_right_thigh";
    private static final String BACK_RIGHT_SHIN = "back_right_shin";
    private static final String BACK_RIGHT_HOOF = "back_right_hoof";
    private static final String FRONT_LEFT_LEG = "front_left_leg";
    private static final String FRONT_LEFT_SHIN = "front_left_shin";
    private static final String FRONT_LEFT_HOOF = "front_left_hoof";
    private static final String FRONT_RIGHT_LEG = "front_right_leg";
    private static final String FRONT_RIGHT_SHIN = "front_right_shin";
    private static final String FRONT_RIGHT_HOOF = "front_right_hoof";
    private static final String UPPER_MOUTH = "upper_mouth";
    private static final String LOWER_MOUTH = "lower_mouth";

    private static final String TAIL_BASE = "tail_base";
    private static final String TAIL_MIDDLE = "tail_middle";
    private static final String TAIL_TIP = "tail_tip";
    private static final String TAIL_THIN = "tail_thin";
    private static final String TAIL_TUFT = "tail_tuft";

    private static final String STIFF_MANE = "stiff_mane";

    private static final String HORSE_LEFT_EAR = "horse_left_ear";
    private static final String HORSE_RIGHT_EAR = "horse_right_ear";
    private static final String MULE_LEFT_EAR = "mule_left_ear";
    private static final String MULE_RIGHT_EAR = "mule_right_ear";

    private static final String HORN = "horn";
    private static final String BABY_HORN = "baby_horn";

    private static final String LEFT_CHEST = "left_chest";
    private static final String RIGHT_CHEST = "right_chest";

    private static final String FACE_ROPES = "face_ropes";
    private static final String LEFT_BIT = "left_bit";
    private static final String RIGHT_BIT = "right_bit";
    private static final String LEFT_REIN = "left_rein";
    private static final String RIGHT_REIN = "right_rein";

    private static final String SADDLE_BASE = "saddle_base";
    private static final String SADDLE_FRONT = "saddle_front";
    private static final String SADDLE_BACK = "saddle_back";
    private static final String LEFT_STIRRUP_LEATHER = "left_stirrup_leather";
    private static final String RIGHT_STIRRUP_LEATHER = "right_stirrup_leather";
    private static final String LEFT_STIRRUP = "left_stirrup";
    private static final String RIGHT_STIRRUP = "right_stirrup";


    private final ModelPart body;
    private final ModelPart head;
    private final ModelPart neck;

    private final ModelPart backLeftThigh;
    private final ModelPart backLeftShin;
    private final ModelPart backLeftHoof;
    private final ModelPart backRightThigh;
    private final ModelPart backRightShin;
    private final ModelPart backRightHoof;
    private final ModelPart frontLeftLeg;
    private final ModelPart frontLeftShin;
    private final ModelPart frontLeftHoof;
    private final ModelPart frontRightLeg;
    private final ModelPart frontRightShin;
    private final ModelPart frontRightHoof;
    private final ModelPart upperMouth;
    private final ModelPart lowerMouth;

    // Horse and mule tail
    private final ModelPart tailBase;
    private final ModelPart tailMiddle;
    private final ModelPart tailTip;
    // Donkey tail
    private final ModelPart tailThin;
    private final ModelPart tailTuft;

    private final ModelPart stiffMane;

    private final ModelPart horseLeftEar;
    private final ModelPart horseRightEar;
    private final ModelPart muleLeftEar;
    private final ModelPart muleRightEar;

    private final ModelPart horn;
    private final ModelPart babyHorn;

    private final ModelPart leftChest;
    private final ModelPart rightChest;

    private final ModelPart faceRopes;
    private final ModelPart leftBit;
    private final ModelPart rightBit;
    private final ModelPart leftRein;
    private final ModelPart rightRein;

    private final ModelPart saddleBase;
    private final ModelPart saddleFront;
    private final ModelPart saddleBack;
    private final ModelPart leftStirrupLeather;
    private final ModelPart rightStirrupLeather;
    private final ModelPart leftStirrup;
    private final ModelPart rightStirrup;

    private final ModelPart[] tackArray;
    private final ModelPart[] extraTackArray;

    private float ageScale = 0.5f;

    public HorseGeneticModel(ModelPart root)
    {
        // Initialize AnimalModel
        super(false, 16.2F, 1.36F, 2.7272F, 2.0F, 20.0F);
        this.body = root.getChild(BODY);
        this.head = root.getChild(HEAD);
        this.neck = root.getChild(NECK);
        this.backLeftThigh = root.getChild(BACK_LEFT_THIGH);
        this.backLeftShin = this.backLeftThigh.getChild(BACK_LEFT_SHIN);
        this.backLeftHoof = this.backLeftShin.getChild(BACK_LEFT_HOOF);
        this.backRightThigh = root.getChild(BACK_RIGHT_THIGH);
        this.backRightShin = this.backRightThigh.getChild(BACK_RIGHT_SHIN);
        this.backRightHoof = this.backRightShin.getChild(BACK_RIGHT_HOOF);
        this.frontLeftLeg = root.getChild(FRONT_LEFT_LEG);
        this.frontLeftShin = this.frontLeftLeg.getChild(FRONT_LEFT_SHIN);
        this.frontLeftHoof = this.frontLeftShin.getChild(FRONT_LEFT_HOOF);
        this.frontRightLeg = root.getChild(FRONT_RIGHT_LEG);
        this.frontRightShin = this.frontRightLeg.getChild(FRONT_RIGHT_SHIN);
        this.frontRightHoof = this.frontRightShin.getChild(FRONT_RIGHT_HOOF);
        this.upperMouth = this.head.getChild(UPPER_MOUTH);
        this.lowerMouth = this.head.getChild(LOWER_MOUTH);

        this.tailBase = this.body.getChild(TAIL_BASE);
        this.tailMiddle = this.tailBase.getChild(TAIL_MIDDLE);
        this.tailTip = this.tailMiddle.getChild(TAIL_TIP);
        this.tailThin = this.body.getChild(TAIL_THIN);
        this.tailTuft = this.tailThin.getChild(TAIL_TUFT);

        this.stiffMane = this.neck.getChild(STIFF_MANE);

        this.horseLeftEar = this.head.getChild(HORSE_LEFT_EAR);
        this.horseRightEar = this.head.getChild(HORSE_RIGHT_EAR);
        this.muleLeftEar = this.head.getChild(MULE_LEFT_EAR);
        this.muleRightEar = this.head.getChild(MULE_RIGHT_EAR);

        this.horn = this.head.getChild(HORN);
        this.babyHorn = this.head.getChild(BABY_HORN);

        this.leftChest = root.getChild(LEFT_CHEST);
        this.rightChest = root.getChild(RIGHT_CHEST);

        this.faceRopes = this.head.getChild(FACE_ROPES);
        this.leftBit = this.head.getChild(LEFT_BIT);
        this.rightBit = this.head.getChild(RIGHT_BIT);
        this.leftRein = this.neck.getChild(LEFT_REIN);
        this.rightRein = this.neck.getChild(RIGHT_REIN);

        this.saddleBase = this.body.getChild(SADDLE_BASE);
        this.saddleFront = this.saddleBase.getChild(SADDLE_FRONT);
        this.saddleBack = this.saddleBase.getChild(SADDLE_BACK);
        this.leftStirrupLeather = this.saddleBase.getChild(LEFT_STIRRUP_LEATHER);
        this.rightStirrupLeather = this.saddleBase.getChild(RIGHT_STIRRUP_LEATHER);
        this.leftStirrup = this.leftStirrupLeather.getChild(LEFT_STIRRUP);
        this.rightStirrup = this.rightStirrupLeather.getChild(RIGHT_STIRRUP);


        this.tackArray = new ModelPart[]{saddleBase, saddleFront, saddleBack, leftStirrup, leftStirrupLeather, rightStirrup, rightStirrupLeather, leftBit, rightBit, faceRopes};
        this.extraTackArray = new ModelPart[]{leftRein, rightRein};
    }

    public static TexturedModelData createBodyLayer() {
        return TexturedModelData.of(createBodyMesh(Dilation.NONE), 128, 128);
    }

    public static TexturedModelData createArmorLayer() {
        return TexturedModelData.of(createBodyMesh(new Dilation(0.1F)), 128, 128);
    }

    public static ModelData createBodyMesh(Dilation dilation) {
        ModelData modelData = new ModelData();
        ModelPartData root = modelData.getRoot();
        ModelPartData bodyDef = root.addChild(
            BODY,
            ModelPartBuilder.create().uv(0, 34).cuboid(-5.0F, -8.0F, -19.0F, 10, 10, 24, new Dilation(0.05F)),
            ModelTransform.pivot(0.0F, 11.0F, 9.0F)
        );
        ModelPartData headDef = root.addChild(
            HEAD,
            ModelPartBuilder.create().uv(0, 0).cuboid(-2.5F, -10.0F, -1.5F, 5, 5, 7),
            ModelTransform.of(0.0F, 4.0F, -10.0F, ((float)Math.PI / 6F), 0.0F, 0.0F)
        );
        ModelPartData neckDef = root.addChild(
            NECK,
            ModelPartBuilder.create().uv(0, 12).cuboid(-2.05F, -9.8F, -2.0F, 4, 14, 8),
            ModelTransform.of(0.0F, 4.0F, -10.0F, ((float)Math.PI / 6F), 0.0F, 0.0F)
        );

        // When making something a child that wasn't before, subtract the
        // parent's pivot (now ModelTransform offset)
        ModelPartData backLeftThighDef = root.addChild(
            BACK_LEFT_THIGH,
            ModelPartBuilder.create().uv(78, 29).cuboid(-2.5F, -2.0F, -2.5F, 4, 9, 5),
            ModelTransform.pivot(4.0F, 9.0F, 11.0F)
        );
        ModelPartData backLeftShinDef = backLeftThighDef.addChild(
            BACK_LEFT_SHIN,
            ModelPartBuilder.create().uv(78, 43).cuboid(-2.0F, 0.0F, -1F, 3, 5, 3),
            ModelTransform.pivot(0.0F, 7.0F, 0.0F)
        );
        ModelPartData backLeftHoofDef = backLeftShinDef.addChild(
            BACK_LEFT_HOOF,
            ModelPartBuilder.create().uv(78, 51).cuboid(-2.5F, 5.0F, -1.5F, 4, 3, 4),
            ModelTransform.NONE
        );

        ModelPartData backRightThighDef = root.addChild(
            BACK_RIGHT_THIGH,
            ModelPartBuilder.create().uv(96, 29).cuboid(-1.5F, -2.0F, -2.5F, 4, 9, 5),
            ModelTransform.pivot(-4.0F, 9.0F, 11.0F)
        );
        ModelPartData backRightShinDef = backRightThighDef.addChild(
            BACK_RIGHT_SHIN,
            ModelPartBuilder.create().uv(96, 43).cuboid(-1.0F, 0.0F, -1F, 3, 5, 3),
            ModelTransform.pivot(0.0F, 7.0F, 0.0F)
        );
        ModelPartData backRightHoofDef = backRightShinDef.addChild(
            BACK_RIGHT_HOOF,
            ModelPartBuilder.create().uv(96, 51).cuboid(-1.5F, 5.0F, -1.5F, 4, 3, 4),
            ModelTransform.NONE
        );

        ModelPartData frontLeftLegDef = root.addChild(
            FRONT_LEFT_LEG,
            ModelPartBuilder.create().uv(44, 29).cuboid(-1.9F, -1.0F, -1.0F, 3, 8, 4),
            ModelTransform.pivot(4.0F, 9.0F, -8.0F)
        );
        ModelPartData frontLeftShinDef = frontLeftLegDef.addChild(
            FRONT_LEFT_SHIN,
            ModelPartBuilder.create().uv(44, 41).cuboid(-1.9F, 0.0F, -0.5F, 3, 5, 3),
            ModelTransform.pivot(0.0F, 7.0F, 0.0F)
        );
        ModelPartData frontLeftHoofDef = frontLeftShinDef.addChild(
            FRONT_LEFT_HOOF,
            ModelPartBuilder.create().uv(44, 51).cuboid(-2.4F, 5.0F, -1.0F, 4, 3, 4),
            ModelTransform.NONE
        );

        ModelPartData frontRightLegDef = root.addChild(
            FRONT_RIGHT_LEG,
            ModelPartBuilder.create().uv(60, 29).cuboid(-1.1F, -1.0F, -1.0F, 3, 8, 4),
            ModelTransform.pivot(-4.0F, 9.0F, -8.0F)
        );
        ModelPartData frontRightShinDef = frontRightLegDef.addChild(
            FRONT_RIGHT_SHIN,
            ModelPartBuilder.create().uv(60, 41).cuboid(-1.1F, 0.0F, -0.5F, 3, 5, 3),
            ModelTransform.pivot(0.0F, 7.0F, 0.0F)
        );
        ModelPartData frontRightHoofDef = frontRightShinDef.addChild(
            FRONT_RIGHT_HOOF,
            ModelPartBuilder.create().uv(60, 51).cuboid(-1.6F, 5.0F, -1.0F, 4, 3, 4),
            ModelTransform.NONE
        );

        ModelPartData upperMouthDef = headDef.addChild(
            UPPER_MOUTH,
            ModelPartBuilder.create().uv(24, 18).cuboid(-2.0F, -10.0F, -7.0F, 4, 3, 6),
            ModelTransform.NONE
        );
        ModelPartData lowerMouthDef = headDef.addChild(
            LOWER_MOUTH,
            ModelPartBuilder.create().uv(24, 27).cuboid(-2.0F, -7.0F, -6.5F, 4, 2, 5),
            ModelTransform.NONE
        );

        ModelPartData tailBaseDef = bodyDef.addChild(
            TAIL_BASE,
            ModelPartBuilder.create().uv(44, 0).cuboid(-1.0F, -1.0F, 0.0F, 2, 2, 3),
            ModelTransform.of(0.0F, -8.0F, 5.0F, -1.134464F, 0F, 0F)
        );
        ModelPartData tailMiddleDef = tailBaseDef.addChild(
            TAIL_MIDDLE,
            ModelPartBuilder.create().uv(38, 7).cuboid(-1.5F, -2.0F, 3.0F, 3, 4, 7),
            ModelTransform.NONE
        );
        ModelPartData tailTipDef = tailMiddleDef.addChild(
            TAIL_TIP,
            ModelPartBuilder.create().uv(24, 3).cuboid(-1.5F, -4.5F, 9.0F, 3, 4, 7),
            ModelTransform.of(0F, 0F, 0F, -0.2618004F, 0F, 0F)
        );
        ModelPartData tailThinDef = bodyDef.addChild(
            TAIL_THIN,
            ModelPartBuilder.create().uv(116, 0).cuboid(-0.5F, 0.0F, 0.5F, 1, 5, 1),
            // Rotation may not actually be needed
            ModelTransform.of(0.0F, -6F, 4.0F, -1.134464F, 0F, 0F)
        );
        ModelPartData tailTuftDef = tailThinDef.addChild(
            TAIL_TUFT,
            ModelPartBuilder.create().uv(120, 0).cuboid(-1.0F, 0F, 0.25F, 2, 6, 2),
            ModelTransform.pivot(0.0F, 5.0F, 0.0F)
        );
            // Rotation amy

        ModelPartData stiffManeDef = neckDef.addChild(
            STIFF_MANE,
            ModelPartBuilder.create().uv(58, 0).cuboid(-1.0F, -11.5F, 5.0F, 2, 16, 4),
            ModelTransform.pivot(0.0F, 0.0F, -1.0F)
        );

        ModelPartData horseLeftEarDef = headDef.addChild(
            HORSE_LEFT_EAR,
            ModelPartBuilder.create().uv(0, 0).cuboid(0.45F, -12.0F, 4.0F, 2, 3, 1),
            ModelTransform.NONE
        );
        ModelPartData horseRightEarDef = headDef.addChild(
            HORSE_RIGHT_EAR,
            ModelPartBuilder.create().uv(0, 0).cuboid(-2.45F, -12.0F, 4.0F, 2, 3, 1),
            ModelTransform.NONE
        );
        ModelPartData muleLeftEarDef = headDef.addChild(
            MULE_LEFT_EAR,
            ModelPartBuilder.create().uv(0, 12).cuboid(-2.0F, -16.0F, 4.0F, 2, 7, 1),
            ModelTransform.of(0F, 0F, 0F, 0F, 0F, 0.2617994F)
        );
        ModelPartData muleRightEarDef = headDef.addChild(
            MULE_RIGHT_EAR,
            ModelPartBuilder.create().uv(0, 12).cuboid(0.0F, -16.0F, 4.0F, 2, 7, 1),
            ModelTransform.of(0F, 0F, 0F, 0F, 0F, -0.2617994F)
        );

        final int hornLength = 7;
        ModelPartData hornDef = headDef.addChild(
            HORN,
            ModelPartBuilder.create().uv(84, 0).cuboid(-0.5F, -10.0F - hornLength, 2.0F, 1, hornLength, 1),
            ModelTransform.NONE
        );
        final int babyHornLength = 3;
        ModelPartData babyHornDef = headDef.addChild(
            BABY_HORN,
            ModelPartBuilder.create().uv(84, 0).cuboid(-0.5F, -10.0F - babyHornLength, 2.0F, 1, babyHornLength, 1),
            ModelTransform.NONE
        );

        ModelPartData leftChestDef = root.addChild(
            LEFT_CHEST,
            ModelPartBuilder.create().uv(0, 34).cuboid(-3.0F, 0.0F, 0.0F, 8, 8, 3),
            ModelTransform.of(-7.5F, 3.0F, 10.0F, 0F, (float)Math.PI / 2F, 0F)
        );
        ModelPartData rightChestDef = root.addChild(
            RIGHT_CHEST,
            ModelPartBuilder.create().uv(0, 47).cuboid(-3.0F, 0.0F, 0.0F, 8, 8, 3),
            ModelTransform.of(4.5F, 3.0F, 10.0F, 0F, (float)Math.PI / 2F, 0F)
        );

        ModelPartData faceRopesDef = headDef.addChild(
            FACE_ROPES,
            ModelPartBuilder.create().uv(80, 12).cuboid(-2.5F, -10.1F, -7.0F, 5, 5, 12, new Dilation(0.2F)),
            ModelTransform.NONE
        );
        ModelPartData leftBitDef = headDef.addChild(
            LEFT_BIT,
            ModelPartBuilder.create().uv(74, 13).cuboid(1.5F, -8.0F, -4.0F, 1, 2, 2),
            ModelTransform.NONE
        );
        ModelPartData rightBitDef = headDef.addChild(
            RIGHT_BIT,
            ModelPartBuilder.create().uv(74, 13).cuboid(-2.5F, -8.0F, -4.0F, 1, 2, 2),
            ModelTransform.NONE
        );
        ModelPartData leftReinDef = neckDef.addChild(
            LEFT_REIN,
            ModelPartBuilder.create().uv(44, 10).cuboid(2.6F, -6.0F, -6.0F, 0, 3, 16),
            ModelTransform.of(0F, 0F, 0F, -0.5235988F, 0F, 0F)
        );
        ModelPartData rightReinDef = neckDef.addChild(
            RIGHT_REIN,
            ModelPartBuilder.create().uv(44, 5).cuboid(-2.6F, -6.0F, -6.0F, 0, 3, 16),
            ModelTransform.of(0F, 0F, 0F, -0.5235988F, 0F, 0F)
        );

        ModelPartData saddleBaseDef = bodyDef.addChild(
            SADDLE_BASE,
            ModelPartBuilder.create().uv(80, 0).cuboid(-5.0F, 0.0F, -3.0F, 10, 1, 8),
            ModelTransform.pivot(0.0F, -9.0F, -7.0F)
        );
        ModelPartData saddleFrontDef = saddleBaseDef.addChild(
            SADDLE_FRONT,
            ModelPartBuilder.create().uv(106, 9).cuboid(-1.5F, -1.0F, -3.0F, 3, 1, 2),
            ModelTransform.NONE
        );
        ModelPartData saddleBackDef = saddleBaseDef.addChild(
            SADDLE_BACK,
            ModelPartBuilder.create().uv(80, 9).cuboid(-4.0F, -1.0F, 3.0F, 8, 1, 2),
            ModelTransform.NONE
        );
        ModelPartData leftStirrupLeatherDef = saddleBaseDef.addChild(
            LEFT_STIRRUP_LEATHER,
            ModelPartBuilder.create().uv(70, 0).cuboid(-0.5F, 0.0F, -0.5F, 1, 6, 1),
            ModelTransform.pivot(5.0F, 1.0F, 0.0F)
        );
        ModelPartData rightStirrupLeatherDef = saddleBaseDef.addChild(
            RIGHT_STIRRUP_LEATHER,
            ModelPartBuilder.create().uv(80, 0).cuboid(-0.5F, 0.0F, -0.5F, 1, 6, 1),
            ModelTransform.pivot(-5.0F, 1.0F, 0.0F)
        );
        ModelPartData leftStirrupDef = leftStirrupLeatherDef.addChild(
            LEFT_STIRRUP,
            ModelPartBuilder.create().uv(74, 0).cuboid(-0.5F, 6.0F, -1.0F, 1, 2, 2),
            ModelTransform.NONE
        );
        ModelPartData rightStirrupDef = rightStirrupLeatherDef.addChild(
            RIGHT_STIRRUP,
            ModelPartBuilder.create().uv(74, 4).cuboid(-0.5F, 6.0F, -1.0F, 1, 2, 2),
            ModelTransform.NONE
        );

        return modelData;
    }

    @Override
    public void setAngles(T entityIn, float p_225597_2_, float p_225597_3_, float p_225597_4_, float limbSwingAmount, float partialTickTime) {
        if (entityIn instanceof AbstractHorseGenetic) {
            AbstractHorseGenetic horse = (AbstractHorseGenetic)entityIn;
            this.muleLeftEar.visible = horse.longEars();
            this.muleRightEar.visible = horse.longEars();
            this.horseLeftEar.visible = !horse.longEars();
            this.horseRightEar.visible = !horse.longEars();
            this.tailBase.visible = horse.fluffyTail();
            this.tailThin.visible = !horse.fluffyTail();
            this.ageScale = horse.getGangliness();
        }
        else {
            System.out.println("Attempting to use HorseGeneticModel on an unsupported entity type");
        }
        this.horn.visible = false;
        this.babyHorn.visible = false;

        boolean hasChest = false;
        if (entityIn instanceof AbstractDonkeyEntity) {
            hasChest = ((AbstractDonkeyEntity)entityIn).hasChest();
        }
        this.leftChest.visible = hasChest;
        this.rightChest.visible = hasChest;

        boolean isSaddled = entityIn.isSaddled();
        boolean showReins = entityIn.getControllingPassenger() != null;

        for(ModelPart tack_piece : this.tackArray) {
            tack_piece.visible = isSaddled;
        }

        for(ModelPart extra_tack : this.extraTackArray) {
            extra_tack.visible = showReins && isSaddled;
        }

        // Probably because the body only rotates for rearing
        this.body.pivotY = 11.0F;
     }

    /**
     * Fixes and offsets a rotation in the ModelHorse class.
     */
    private float updateHorseRotation(float prevRotation, float currentRotation, float partialTickTime)
    {
        float bodyRotation;

        for (bodyRotation = currentRotation - prevRotation; bodyRotation < -180.0F; bodyRotation += 360.0F)
        {
            ;
        }

        while (bodyRotation >= 180.0F)
        {
            bodyRotation -= 360.0F;
        }

        return prevRotation + partialTickTime * bodyRotation;
    }

    // This function renders the list of things given
    // I suspect this is for parts that are proportionally bigger on children
    protected Iterable<ModelPart> getHeadParts() {
        // In vanilla the neck goes here
        return ImmutableList.of();
    }

    // This function renders the list of things given
    // I suspect this is for parts that are always the same proportions
    protected Iterable<ModelPart> getBodyParts() {
        return ImmutableList.of(this.body, this.neck, this.backLeftThigh, this.backRightThigh, this.frontLeftLeg, this.frontRightLeg, this.leftChest, this.rightChest);
    }

    // Copied and modified from the familiar horses mod as allowed by the Unlicense
    /**
     * Sets the models various rotation angles then renders the model.
     */
    @Override
    public void render(@NotNull MatrixStack matrixStackIn, @NotNull VertexConsumer bufferIn, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha) {
        Consumer<ModelPart> render = model -> model.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);

        // ageScale is 0.5f for the smallest foals
        if (this.child) {
            matrixStackIn.push();
            matrixStackIn.scale(ageScale, 0.5F + ageScale * 0.5F, ageScale);
            // Move the foal's legs downward so they reach the ground
            matrixStackIn.translate(0.0F, 0.95F * (1.0F - ageScale), 0.0F);
        }

        ImmutableList.of(this.backLeftThigh, this.backRightThigh,
                         this.frontLeftLeg, this.frontRightLeg).forEach(render);

        if (this.child) {

            matrixStackIn.pop();
            matrixStackIn.push();
            // Move the body downwards but not as far as the legs
            matrixStackIn.translate(0.0F, ageScale * 1.35F * (1.0F - ageScale), 0.0F);
            matrixStackIn.scale(ageScale, ageScale, ageScale);
        }

        ImmutableList.of(this.body, this.neck).forEach(render);

        if (this.child) {
            matrixStackIn.pop();
            matrixStackIn.push();
            float headScale = 0.5F + ageScale * ageScale * 0.5F;
            // Translate to match the body position
            matrixStackIn.translate(0.0F, ageScale * 1.35F * (1.0F - ageScale), 0.0F);
            matrixStackIn.scale(headScale, headScale, headScale);
            float extra = (headScale - ageScale) * 1.35F * (1.0F - ageScale);
            // The head's rest angle is 0.5235988F, 30 degrees
            matrixStackIn.translate(0.0F, extra * Math.cos(this.head.pitch), extra * Math.sin(this.head.pitch));
        }

        ImmutableList.of(this.head).forEach(render);

        if (this.child) {
            matrixStackIn.pop();
        }

        ImmutableList.of(this.leftChest, this.rightChest).forEach(render);
    }

    private void setMouthAnimations(float mouthOpenAmount) {
        this.upperMouth.pivotY = 0.02F;
        this.lowerMouth.pivotY = 0.0F;
        this.upperMouth.pivotZ = 0.02F - mouthOpenAmount;
        this.lowerMouth.pivotZ = mouthOpenAmount;
        this.upperMouth.pitch = -0.09424778F * mouthOpenAmount;
        this.lowerMouth.pitch = 0.15707964F * mouthOpenAmount;
        this.upperMouth.yaw = 0.0F;
        this.lowerMouth.yaw = 0.0F;
    }


    /**
     * Used for easily adding entity-dependent animations. The second and third float params here are the same second
     * and third as in the setAngles method.
     */
    @Override
    public void animateModel(T entityIn, float limbSwing, float limbSwingAmount, float partialTickTime)
    {
        super.animateModel(entityIn, limbSwing, limbSwingAmount, partialTickTime);
        float bodyRotation = this.updateHorseRotation(entityIn.prevBodyYaw, entityIn.bodyYaw, partialTickTime);
        float headRotation = this.updateHorseRotation(entityIn.prevHeadYaw, entityIn.headYaw, partialTickTime);
        float interpolatedPitch = entityIn.prevPitch + (entityIn.getPitch() - entityIn.prevPitch) * partialTickTime;
        float headRelativeRotation = headRotation - bodyRotation;
        float f4 = interpolatedPitch * 0.017453292F;

        if (headRelativeRotation > 20.0F)
        {
            headRelativeRotation = 20.0F;
        }

        if (headRelativeRotation < -20.0F)
        {
            headRelativeRotation = -20.0F;
        }

        if (limbSwingAmount > 0.2F)
        {
            f4 += MathHelper.cos(limbSwing * 0.4F) * 0.15F * limbSwingAmount;
        }

        AbstractHorseEntity abstracthorse = (AbstractHorseEntity)entityIn;
        float grassEatingAmount = abstracthorse.getEatingGrassAnimationProgress(partialTickTime);
        float rearingAmount = abstracthorse.getAngryAnimationProgress(partialTickTime);
        float notRearingAmount = 1.0F - rearingAmount;
        boolean isSwishingTail = abstracthorse.tailWagTicks != 0;
        boolean isSaddled = abstracthorse.isSaddled();
        boolean areStirrupsForward = abstracthorse.isLogicalSideForUpdatingMovement();
        float ticks = (float)entityIn.age + partialTickTime;
        float legRotationBase = MathHelper.cos(limbSwing * 0.6662F + (float)Math.PI);
        float legRotation1 = legRotationBase * 0.8F * limbSwingAmount;
        float neckBend = rearingAmount + 1.0F - Math.max(rearingAmount, grassEatingAmount);


        float mouthOpenAmount = abstracthorse.getEatingAnimationProgress(partialTickTime);
        this.setMouthAnimations(mouthOpenAmount);

        this.neck.setPivot(0.0F, 4.0F, -10.0F);
        this.neck.pitch = rearingAmount * (0.2617994F + f4) + grassEatingAmount * 2.1816616F + (1.0F - Math.max(rearingAmount, grassEatingAmount)) * 0.5235988F + f4;
        this.neck.yaw = neckBend * headRelativeRotation * 0.017453292F;
        this.neck.pivotY = rearingAmount * -6.0F + grassEatingAmount * 11.0F + (1.0F - Math.max(rearingAmount, grassEatingAmount)) * this.neck.pivotY;
        this.neck.pivotZ = rearingAmount * -1.0F + grassEatingAmount * -10.0F + (1.0F - Math.max(rearingAmount, grassEatingAmount)) * this.neck.pivotZ;
        this.body.pitch = rearingAmount * -((float)Math.PI / 4F);
        this.head.pivotX = this.neck.pivotX;
        this.head.pivotY = this.neck.pivotY;
        this.head.pivotZ = this.neck.pivotZ;
        this.head.pitch = this.neck.pitch;
        this.head.yaw = this.neck.yaw;
        float legRotationRearing = 0.2617994F * rearingAmount;
        float legRotationTicks = MathHelper.cos(ticks * 0.6F + (float)Math.PI);
        this.frontLeftLeg.pivotY = -2.0F * rearingAmount + 9.0F * notRearingAmount;
        this.frontLeftLeg.pivotZ = -2.0F * rearingAmount + -8.0F * notRearingAmount;
        this.frontRightLeg.pivotY = this.frontLeftLeg.pivotY;
        this.frontRightLeg.pivotZ = this.frontLeftLeg.pivotZ;
        float legRotation4 = (-1.0471976F + legRotationTicks) * rearingAmount + legRotation1 * notRearingAmount;
        float legRotation5 = (-1.0471976F - legRotationTicks) * rearingAmount + -legRotation1 * notRearingAmount;
        this.backLeftThigh.pitch = legRotationRearing + -legRotationBase * 0.5F * limbSwingAmount * notRearingAmount;
        this.backRightThigh.pitch = legRotationRearing + legRotationBase * 0.5F * limbSwingAmount * notRearingAmount;
        this.frontLeftLeg.pitch = legRotation4;
        this.frontLeftShin.pitch = (this.frontLeftLeg.pitch + (float)Math.PI * Math.max(0.0F, 0.2F + legRotationTicks * 0.2F)) * rearingAmount + (legRotation1 + Math.max(0.0F, legRotationBase * 0.5F * limbSwingAmount)) * notRearingAmount - this.frontLeftLeg.pitch;
        // This might do the same thing
        //this.frontLeftShin.pitch = ((float)Math.PI * Math.max(0.0F, 0.2F + legRotationTicks * 0.2F)) * rearingAmount;
        this.frontRightLeg.pitch = legRotation5;
        this.frontRightShin.pitch = (this.frontRightLeg.pitch + (float)Math.PI * Math.max(0.0F, 0.2F - legRotationTicks * 0.2F)) * rearingAmount + (-legRotation1 + Math.max(0.0F, -legRotationBase * 0.5F * limbSwingAmount)) * notRearingAmount - this.frontRightLeg.pitch;
        //this.frontRightShin.pitch = ((float)Math.PI * Math.max(0.0F, 0.2F - legRotationTicks * 0.2F)) * rearingAmount;

        this.rightChest.pivotY = 3.0F;
        this.rightChest.pivotZ = 10.0F;
        this.rightChest.pivotY = rearingAmount * 5.5F + notRearingAmount * this.rightChest.pivotY;
        this.rightChest.pivotZ = rearingAmount * 15.0F + notRearingAmount * this.rightChest.pivotZ;
        this.leftChest.pitch = legRotation1 / 5.0F;
        this.rightChest.pitch = -legRotation1 / 5.0F;

        if (isSaddled)
        {
            this.leftChest.pivotY = this.rightChest.pivotY;
            this.leftChest.pivotZ = this.rightChest.pivotZ;

            if (areStirrupsForward)
            {
                this.leftStirrupLeather.pitch = -1.0471976F;
                this.rightStirrupLeather.pitch = -1.0471976F;
                this.leftStirrupLeather.roll = 0.0F;
                this.rightStirrupLeather.roll = 0.0F;
            }
            else
            {
                this.leftStirrupLeather.pitch = legRotation1 / 3.0F;
                this.rightStirrupLeather.pitch = legRotation1 / 3.0F;
                this.leftStirrupLeather.roll = legRotation1 / 5.0F;
                this.rightStirrupLeather.roll = -legRotation1 / 5.0F;
            }
        }

        float tailRotation = -1.3089969F + limbSwingAmount * 1.5F;
        float donkeyTailRotate = 0.17F + limbSwingAmount;

        if (tailRotation > 0.0F)
        {
            tailRotation = 0.0F;
        }

        if (isSwishingTail)
        {
            this.tailBase.yaw = MathHelper.cos(ticks * 0.7F);
            this.tailThin.yaw = MathHelper.cos(ticks * 0.7F);
            tailRotation = 0.0F;
            donkeyTailRotate = (float)Math.PI / 2f;
        }
        else
        {
            this.tailBase.yaw = 0.0F;
            this.tailThin.yaw = 0.0F;
        }

        this.tailBase.pitch = tailRotation;
        this.tailThin.pitch = donkeyTailRotate;

        // Make donkeys have a thinner mane
        if (abstracthorse instanceof AbstractHorseGenetic && ((AbstractHorseGenetic)abstracthorse).thinMane()) {
            this.stiffMane.pivotZ = -1.0F;
        }
        else {
            this.stiffMane.pivotZ = 0.0F;
        }
    }
}
