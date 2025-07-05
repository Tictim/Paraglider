package tictim.paraglider.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.AnimationUtils;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.fml.common.asm.enumextension.EnumProxy;
import net.neoforged.neoforge.client.IArmPoseTransformer;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDate;
import java.time.Month;
import java.util.Objects;

@SuppressWarnings("unused")
public final class ParaglidingArmPose {
	private ParaglidingArmPose() {}

	@SuppressWarnings("unused")
	public static final EnumProxy<HumanoidModel.ArmPose> ENUM = new EnumProxy<>(
			HumanoidModel.ArmPose.class, true, (IArmPoseTransformer)ParaglidingArmPose::applyTransform
	);

	private static final float ARM_ROTATION = (float)(Math.PI * 2 - 2.9);

	private static @Nullable Boolean IS_CHRISTMAS;

	private static void applyTransform(HumanoidModel<?> model, LivingEntity entity, HumanoidArm arm) {
		Boolean theDay = IS_CHRISTMAS;
		if (theDay == null) {
			LocalDate now = LocalDate.now();
			IS_CHRISTMAS = theDay = now.getMonth() == Month.APRIL && now.getDayOfMonth() == 1;
		}

		if (theDay) {
			model.head.xRot = 0;
			model.head.yRot = 0;
			model.head.zRot = 0;
			model.leftArm.xRot = 0;
			model.leftArm.yRot = 0;
			model.leftArm.zRot = (float)(-Math.PI / 2);
			model.rightArm.xRot = 0;
			model.rightArm.yRot = 0;
			model.rightArm.zRot = (float)(Math.PI / 2);
			model.leftLeg.xRot = 0;
			model.leftLeg.yRot = 0;
			model.leftLeg.zRot = 0;
			model.rightLeg.xRot = 0;
			model.rightLeg.yRot = 0;
			model.rightLeg.zRot = 0;
		} else {
			model.leftArm.xRot = ARM_ROTATION;
			model.leftArm.yRot = 0;
			model.leftArm.zRot = 0;
			model.rightArm.xRot = ARM_ROTATION;
			model.rightArm.yRot = 0;
			model.rightArm.zRot = 0;
			model.leftLeg.xRot = 0;
			model.leftLeg.yRot = 0;
			model.leftLeg.zRot = 0;
			model.rightLeg.xRot = 0;
			model.rightLeg.yRot = 0;
			model.rightLeg.zRot = 0;
		}

		Minecraft mc = Minecraft.getInstance();
		ClientLevel level = Objects.requireNonNull(mc.level);
		float partialTick = mc.getTimer().getGameTimeDeltaPartialTick(!level.tickRateManager().isEntityFrozen(entity));
		float ageInTicks = entity.tickCount + partialTick;

		AnimationUtils.bobModelPart(model.rightArm, ageInTicks, -1f);
		AnimationUtils.bobModelPart(model.leftArm, ageInTicks, 1f);
	}
}
