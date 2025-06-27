package tictim.paraglider.client;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.world.entity.HumanoidArm;
import net.neoforged.fml.common.asm.enumextension.EnumProxy;
import net.neoforged.neoforge.client.IArmPoseTransformer;

@SuppressWarnings("unused")
public final class ParaglidingArmPose {
	private ParaglidingArmPose() {}

	@SuppressWarnings("unused")
	public static final EnumProxy<HumanoidModel.ArmPose> ENUM = new EnumProxy<>(
			HumanoidModel.ArmPose.class, true, (IArmPoseTransformer)ParaglidingArmPose::applyTransform
	);

	private static final float ARM_ROTATION = (float)(Math.PI * 2 - 2.9);

	private static void applyTransform(HumanoidModel<?> model, HumanoidRenderState entity, HumanoidArm arm) {
		model.leftArm.xRot = ARM_ROTATION;
		model.leftArm.zRot = 0;
		model.rightArm.xRot = ARM_ROTATION;
		model.rightArm.zRot = 0;
		model.leftLeg.xRot = 0f;
		model.rightLeg.xRot = 0f;
	}
}
