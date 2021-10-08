package tictim.paraglider.mixin;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tictim.paraglider.item.ParagliderItem;

@Mixin(PlayerModel.class)
public abstract class MixinPlayerModel extends HumanoidModel<LivingEntity>{
	private static final float ARM_ROTATION = (float)(Math.PI*2-2.9);

	public MixinPlayerModel(ModelPart part){
		super(part);
	}

	@Inject(
			method = "setupAnim(Lnet/minecraft/world/entity/LivingEntity;FFFFF)V",
			at = {
					@At(shift = Shift.AFTER, value = "INVOKE", target = "Lnet/minecraft/client/model/HumanoidModel;setupAnim(Lnet/minecraft/world/entity/LivingEntity;FFFFF)V")
			}
	)
	public void onSetRotationAngles(LivingEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch,
	                                CallbackInfo ci){
		ItemStack stack = entity.getMainHandItem();
		if(ParagliderItem.isItemParagliding(stack)){
			leftArm.xRot = ARM_ROTATION;
			leftArm.zRot = 0;
			rightArm.xRot = ARM_ROTATION;
			rightArm.zRot = 0;
			leftLeg.xRot = 0f;
			rightLeg.xRot = 0f;
		}
	}
}
