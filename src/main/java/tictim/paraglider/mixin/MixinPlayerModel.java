package tictim.paraglider.mixin;

import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.client.renderer.entity.model.PlayerModel;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tictim.paraglider.item.ParagliderItem;

@Mixin(PlayerModel.class)
public abstract class MixinPlayerModel extends BipedModel<LivingEntity>{
	private static final float ARM_ROTATION = (float)(Math.PI*2-2.9);

	public MixinPlayerModel(float modelSize){
		super(modelSize);
	}

	@Inject(
			method = "setRotationAngles(Lnet/minecraft/entity/LivingEntity;FFFFF)V",
			at = {
					@At(shift = Shift.AFTER, value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/model/BipedModel;setRotationAngles(Lnet/minecraft/entity/LivingEntity;FFFFF)V")
			}
	)
	public void onSetRotationAngles(LivingEntity entity,
	                                float limbSwing,
	                                float limbSwingAmount,
	                                float ageInTicks,
	                                float netHeadYaw,
	                                float headPitch,
	                                CallbackInfo ci){
		ItemStack stack = entity.getHeldItemMainhand();
		if(ParagliderItem.isItemParagliding(stack)){
			bipedLeftArm.rotateAngleX = ARM_ROTATION;
			bipedLeftArm.rotateAngleZ = 0;
			bipedRightArm.rotateAngleX = ARM_ROTATION;
			bipedRightArm.rotateAngleZ = 0;
			bipedLeftLeg.rotateAngleX = 0f;
			bipedRightLeg.rotateAngleX = 0f;
		}
	}
}
