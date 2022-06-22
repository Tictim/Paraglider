package tictim.paraglider.event;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.CustomizeGuiOverlayEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.client.settings.KeyModifier;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import org.lwjgl.glfw.GLFW;
import tictim.paraglider.ModCfg;
import tictim.paraglider.capabilities.PlayerMovement;
import tictim.paraglider.client.screen.ParagliderSettingScreen;
import tictim.paraglider.client.screen.StatueBargainScreen;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import static tictim.paraglider.ParagliderMod.MODID;

@Mod.EventBusSubscriber(modid = MODID, value = Dist.CLIENT)
public final class ParagliderClientEventHandler{
	private ParagliderClientEventHandler(){}

	private static KeyMapping paragliderSettingsKey;

	public static KeyMapping paragliderSettingsKey(){
		return paragliderSettingsKey;
	}

	@SubscribeEvent
	public static void onOffHandRender(RenderHandEvent event){
		if(event.getHand()!=InteractionHand.OFF_HAND) return;
		LocalPlayer player = Minecraft.getInstance().player;
		if(player==null) return;
		PlayerMovement m = PlayerMovement.of(player);
		if(m!=null&&m.isParagliding()) event.setCanceled(true);
	}

	private static final DecimalFormat PERCENTAGE = new DecimalFormat("#.#%");

	@SubscribeEvent
	public static void customizeDebugText(CustomizeGuiOverlayEvent.DebugText event){
		if(ModCfg.debugPlayerMovement()){
			Player p = Minecraft.getInstance().player;
			if(p!=null){
				PlayerMovement h = PlayerMovement.of(p);
				if(h!=null){
					ArrayList<String> right = event.getRight();
					List<String> arr = new ArrayList<>();

					arr.add("State: "+h.getState());
					arr.add((h.isDepleted() ? ChatFormatting.RED : "")+"Stamina: "+h.getStamina()+" / "+h.getMaxStamina());
					arr.add(h.getStaminaVessels()+" Stamina Vessels, "+h.getHeartContainers()+" Heart Containers");
					arr.add(h.getRecoveryDelay()+" Recovery Delay");
					arr.add("Paragliding: "+h.isParagliding());
					arr.add("Stamina Wheel X: "+PERCENTAGE.format(ModCfg.staminaWheelX())+", Stamina Wheel Y: "+PERCENTAGE.format(ModCfg.staminaWheelY()));
					if(!right.isEmpty()) arr.add("");

					right.addAll(0, arr);
				}
			}
		}
	}

	@SubscribeEvent
	public static void beforeCrosshairRender(RenderGuiOverlayEvent.Pre event){
		if(event.getOverlay().id().equals(VanillaGuiOverlay.CROSSHAIR.id())&&
				Minecraft.getInstance().screen instanceof StatueBargainScreen)
			event.setCanceled(true);
	}

	@SubscribeEvent
	public static void onClientTick(TickEvent.ClientTickEvent event){
		if(event.phase!=TickEvent.Phase.END) return;
		if(Minecraft.getInstance().screen==null&&paragliderSettingsKey().consumeClick()){
			Minecraft.getInstance().setScreen(new ParagliderSettingScreen());
		}
	}

	@Mod.EventBusSubscriber(modid = MODID, bus = Bus.MOD, value = Dist.CLIENT)
	private static class ModEvents{
		@SubscribeEvent
		public static void registerKeyMappings(RegisterKeyMappingsEvent event){
			event.register(paragliderSettingsKey = new KeyMapping(
					"key.paraglider.paragliderSettings",
					KeyConflictContext.IN_GAME,
					KeyModifier.CONTROL,
					InputConstants.Type.KEYSYM,
					GLFW.GLFW_KEY_P, "key.categories.misc"));
		}
	}
}
