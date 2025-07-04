package tictim.paraglider.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.AnimationUtils;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.fml.common.asm.enumextension.EnumProxy;
import net.neoforged.neoforge.client.IArmPoseTransformer;

import java.util.Objects;

@SuppressWarnings("unused")
public final class ParaglidingArmPose {
	private ParaglidingArmPose() {}

	@SuppressWarnings("unused")
	public static final EnumProxy<HumanoidModel.ArmPose> ENUM = new EnumProxy<>(
			HumanoidModel.ArmPose.class, true, (IArmPoseTransformer)ParaglidingArmPose::applyTransform
	);

	private static final float ARM_ROTATION = (float)(Math.PI * 2 - 2.9);

	private static void applyTransform(HumanoidModel<?> model, LivingEntity entity, HumanoidArm arm) {
		model.leftArm.xRot = ARM_ROTATION;
		model.leftArm.zRot = 0;
		model.rightArm.xRot = ARM_ROTATION;
		model.rightArm.zRot = 0;
		model.leftLeg.xRot = 0f;
		model.rightLeg.xRot = 0f;

		Minecraft mc = Minecraft.getInstance();
		ClientLevel level = Objects.requireNonNull(mc.level);
		float partialTick = mc.getTimer().getGameTimeDeltaPartialTick(!level.tickRateManager().isEntityFrozen(entity));
		float ageInTicks = entity.tickCount + partialTick;

		AnimationUtils.bobModelPart(model.rightArm, ageInTicks, -1f);
		AnimationUtils.bobModelPart(model.leftArm, ageInTicks, 1f);
	}
}
