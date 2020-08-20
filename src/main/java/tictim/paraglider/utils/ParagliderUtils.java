package tictim.paraglider.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import tictim.paraglider.capabilities.PlayerMovement;
import tictim.paraglider.capabilities.RemotePlayerMovement;

import java.rmi.Remote;

public final class ParagliderUtils{
	private ParagliderUtils(){}

	public static void resetMainHandItemEquipProgress(){
		DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> Client::resetMainHandItemEquipProgress);
	}

	public static final class Client{
		private Client(){}

		private static final float ARM_ROTATION = (float)(Math.PI*2-2.9);

		@SuppressWarnings("unused") public static void setParagliderRotationAngles(BipedModel<?> biped, PlayerEntity player){
			PlayerMovement h = player.getCapability(PlayerMovement.CAP).orElse(null);
			if(h instanceof RemotePlayerMovement&&h.isParagliding()){
				biped.bipedLeftArm.rotateAngleX = ARM_ROTATION;
				biped.bipedLeftArm.rotateAngleZ = 0;
				biped.bipedRightArm.rotateAngleX = ARM_ROTATION;
				biped.bipedRightArm.rotateAngleZ = 0;
				biped.bipedLeftLeg.rotateAngleX = 0f;
				biped.bipedRightLeg.rotateAngleX = 0f;
			}
		}

		public static void resetMainHandItemEquipProgress(){
			Minecraft.getInstance().gameRenderer.itemRenderer.resetEquippedProgress(Hand.MAIN_HAND);
		}
	}
}
