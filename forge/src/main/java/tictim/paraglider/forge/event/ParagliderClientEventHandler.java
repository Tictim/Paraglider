package tictim.paraglider.forge.event;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.CustomizeGuiOverlayEvent;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.client.event.RenderHighlightEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import tictim.paraglider.ParagliderMod;
import tictim.paraglider.ParagliderUtils;
import tictim.paraglider.api.movement.Movement;
import tictim.paraglider.client.screen.BargainScreen;
import tictim.paraglider.client.screen.ParagliderSettingScreen;
import tictim.paraglider.config.DebugCfg;

import static tictim.paraglider.api.ParagliderAPI.MODID;
import static tictim.paraglider.api.movement.ParagliderPlayerStates.Flags.FLAG_PARAGLIDING;

@Mod.EventBusSubscriber(modid = MODID, value = Dist.CLIENT)
public final class ParagliderClientEventHandler{
	private ParagliderClientEventHandler(){}

	@SubscribeEvent
	public static void onOffHandRender(RenderHandEvent event){
		if(event.getHand()!=InteractionHand.OFF_HAND) return;
		LocalPlayer player = Minecraft.getInstance().player;
		if(player==null) return;
		Movement m = Movement.get(player);
		if(m.state().has(FLAG_PARAGLIDING)) event.setCanceled(true);
	}

	@SubscribeEvent
	public static void customizeDebugText(CustomizeGuiOverlayEvent.DebugText event){
		if(!DebugCfg.get().debugPlayerMovement()) return;
		Player p = Minecraft.getInstance().player;
		if(p==null) return;
		ParagliderUtils.addDebugText(p, event.getRight());
	}

	@SubscribeEvent
	public static void beforeCrosshairRender(RenderGuiOverlayEvent.Pre event){
		if(event.getOverlay().id().equals(VanillaGuiOverlay.CROSSHAIR.id())&&
				Minecraft.getInstance().screen instanceof BargainScreen)
			event.setCanceled(true);
	}

	@SubscribeEvent
	public static void onClientTick(TickEvent.ClientTickEvent event){
		if(event.phase!=TickEvent.Phase.END) return;
		if(Minecraft.getInstance().screen==null&&ParagliderMod.instance().getParagliderSettingsKey().consumeClick()){
			Minecraft.getInstance().setScreen(new ParagliderSettingScreen());
		}
	}

	// disables all interactions while paragliding
	// this is necessary in addition to cancelling interactions in ParagliderEventHandler
	// to also prevent the arm swing animation from playing
	@SubscribeEvent
	public static void onClickInput(InputEvent.InteractionKeyMappingTriggered event){
		if(event.isPickBlock()) return; // do not prevent block picking
		Player player = Minecraft.getInstance().player;
		if(player==null) return;
		Movement movement = Movement.get(player);
		if(movement.state().has(FLAG_PARAGLIDING)){
			event.setSwingHand(false);
			event.setCanceled(true);
		}
	}

	// disables drawing block highlights while paragliding
	// (as blocks cannot be interacted with, just a convenience feature to avoid confusing players)
	@SubscribeEvent
	public static void onDrawBlockSelection(RenderHighlightEvent.Block event){
		Player player = Minecraft.getInstance().player;
		if(player==null) return;
		Movement movement = Movement.get(player);
		if(movement.state().has(FLAG_PARAGLIDING)) event.setCanceled(true);
	}
}
