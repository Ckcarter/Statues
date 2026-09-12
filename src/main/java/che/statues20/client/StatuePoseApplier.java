package che.statues20.client;

import che.statues20.pose.StatuePose;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.LivingEntity;

/** Exact normalized-pose math ported from the original RenderPlayerStatue.setupModel. */
public final class StatuePoseApplier {
    private StatuePoseApplier() {}

    private static float deg(float degrees) {
        return degrees * ((float)Math.PI / 180.0f);
    }

    public static void apply(PlayerModel<LivingEntity> model, StatuePose pose) {
        model.rightArm.xRot = deg(0 - 180 * pose.armRightB);
        model.rightArm.yRot = deg(90 - 135 * pose.armRightA);
        model.rightArm.zRot = 0;

        model.leftArm.xRot = deg(180 * (1.0f - pose.armLeftB));
        model.leftArm.yRot = deg(270 - 135 * pose.armLeftA);
        model.leftArm.zRot = deg(180);

        model.rightLeg.xRot = deg(120 - 240 * pose.legRightB);
        model.rightLeg.yRot = deg(90 - 90 * pose.legRightA);
        model.rightLeg.zRot = 0;

        model.leftLeg.xRot = deg(120 - 240 * pose.legLeftB);
        model.leftLeg.yRot = deg(-90 + 90 * pose.legLeftA);
        model.leftLeg.zRot = 0;

        model.head.xRot = deg(-45 + 90 * pose.headB);
        model.head.yRot = deg(45 - 90 * pose.headA);
        model.head.zRot = 0;
        model.hat.copyFrom(model.head);

        // Body A/B are whole-statue rotations in the original renderer, not torso bone rotations.
        model.body.xRot = 0;
        model.body.yRot = 0;
        model.body.zRot = 0;

        // Keep all outer skin layers glued to their matching body parts.
        model.rightSleeve.copyFrom(model.rightArm);
        model.leftSleeve.copyFrom(model.leftArm);
        model.rightPants.copyFrom(model.rightLeg);
        model.leftPants.copyFrom(model.leftLeg);
        model.jacket.copyFrom(model.body);
    }

    public static void copyToArmor(PlayerModel<LivingEntity> source, HumanoidModel<LivingEntity> armor) {
        armor.head.copyFrom(source.head);
        armor.hat.copyFrom(source.hat);
        armor.body.copyFrom(source.body);
        armor.rightArm.copyFrom(source.rightArm);
        armor.leftArm.copyFrom(source.leftArm);
        armor.rightLeg.copyFrom(source.rightLeg);
        armor.leftLeg.copyFrom(source.leftLeg);
    }

    /** Old calcHeight() result used to lower strongly bent leg poses toward the floor. */
    public static float legHeightOffset(PlayerModel<LivingEntity> model) {
        return Math.min(calcHeight(model.leftLeg), calcHeight(model.rightLeg));
    }

    private static float calcHeight(ModelPart leg) {
        double cos = Math.cos(leg.xRot);
        if (cos < 0) cos = 0;
        float result = (float)(1.0 - Math.abs(cos * Math.cos(leg.zRot)));
        return Math.max(0, result);
    }

    public static float bodyYawDegrees(StatuePose pose) { return -45.0f + pose.bodyA * 90.0f; }
    public static float bodyPitchDegrees(StatuePose pose) { return -30.0f + pose.bodyB * 60.0f; }
}
