package tictim.paraglider.event;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.*;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import tictim.paraglider.ParagliderClientMod;
import tictim.paraglider.api.movement.Movement;
import tictim.paraglider.client.render.InGameStaminaWheelRenderer;
import tictim.paraglider.client.screen.BargainScreen;
import tictim.paraglider.client.screen.ParagliderSettingsScreen;

import static tictim.paraglider.api.ParagliderAPI.MODID;

@EventBusSubscriber(modid = MODID, value = Dist.CLIENT)
public final class ParagliderClientEventHandler {
	private ParagliderClientEventHandler() {}

	@SubscribeEvent
	public static void onOffHandRender(RenderHandEvent event) {
		if (event.getHand() != InteractionHand.OFF_HAND) return;
		LocalPlayer player = Minecraft.getInstance().player;
		if (player == null) return;
		Movement m = Movement.get(player);
		if (m.state().paragliding()) event.setCanceled(true);
	}

	@SubscribeEvent
	public static void beforeCrosshairRender(RenderGuiLayerEvent.Pre event) {
		if (event.getName().equals(VanillaGuiLayers.CROSSHAIR) &&
				Minecraft.getInstance().gui.screen() instanceof BargainScreen)
			event.setCanceled(true);
	}

	@SubscribeEvent
	public static void onClientTick(ClientTickEvent.Post event) {
		if (Minecraft.getInstance().gui.screen() == null && ParagliderClientMod.instance().getParagliderSettingsKey().consumeClick()) {
			Minecraft.getInstance().gui.setScreen(new ParagliderSettingsScreen());
		}
	}

	// disables all interactions while paragliding
	// this is necessary in addition to cancelling interactions in ParagliderEventHandler
	// to also prevent the arm swing animation from playing
	@SubscribeEvent
	public static void onClickInput(InputEvent.InteractionKeyMappingTriggered event) {
		if (event.isPickBlock() || event.getHand() == InteractionHand.MAIN_HAND && event.isUseItem()) return;
		Player player = Minecraft.getInstance().player;
		if (player == null) return;
		Movement movement = Movement.get(player);
		if (movement.state().paragliding()) {
			event.setSwingHand(false);
			event.setCanceled(true);
		}
	}

	// disables drawing block highlights while paragliding
	// (as blocks cannot be interacted with, just a convenience feature to avoid confusing players)
	@SubscribeEvent
	public static void onDrawBlockSelection(ExtractBlockOutlineRenderStateEvent event) {
		Player player = Minecraft.getInstance().player;
		if (player == null) return;
		Movement movement = Movement.get(player);
		if (movement.state().paragliding()) event.setCanceled(true);
	}

	@SubscribeEvent
	public static void onClientLoggingIn(ClientPlayerNetworkEvent.LoggingIn event) {
		InGameStaminaWheelRenderer.get().reset();
	}
}
