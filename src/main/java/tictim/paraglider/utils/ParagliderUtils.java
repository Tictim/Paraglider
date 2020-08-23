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

		public static void resetMainHandItemEquipProgress(){
			Minecraft.getInstance().gameRenderer.itemRenderer.resetEquippedProgress(Hand.MAIN_HAND);
		}
	}
}
