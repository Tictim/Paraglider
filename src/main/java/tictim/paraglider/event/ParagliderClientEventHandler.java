package tictim.paraglider.event;

import com.mojang.blaze3d.platform.Window;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.DrawSelectionEvent;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.client.gui.ForgeIngameGui;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import tictim.paraglider.ModCfg;
import tictim.paraglider.capabilities.PlayerMovement;
import tictim.paraglider.client.DisableStaminaRender;
import tictim.paraglider.client.InGameStaminaWheelRenderer;
import tictim.paraglider.client.StaminaWheelRenderer;
import tictim.paraglider.client.screen.ParagliderSettingScreen;
import tictim.paraglider.client.screen.StatueBargainScreen;
import tictim.paraglider.item.ParagliderItem;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import static tictim.paraglider.ParagliderMod.MODID;
import static tictim.paraglider.client.StaminaWheelConstants.WHEEL_RADIUS;

@Mod.EventBusSubscriber(modid = MODID, value = Dist.CLIENT)
public final class ParagliderClientEventHandler{
	private ParagliderClientEventHandler(){}

	private static KeyMapping paragliderSettingsKey;

	public static KeyMapping paragliderSettingsKey(){
		return paragliderSettingsKey;
	}
	public static void setParagliderSettingsKey(KeyMapping keyBinding){
		if(paragliderSettingsKey!=null) throw new IllegalStateException("no");
		paragliderSettingsKey = keyBinding;
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
	public static void onGameOverlayTextRender(RenderGameOverlayEvent.Text event){
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

	private static final StaminaWheelRenderer STAMINA_WHEEL_RENDERER = new InGameStaminaWheelRenderer();

	@SubscribeEvent
	public static void afterGameOverlayRender(RenderGameOverlayEvent.Post event){
		if(Minecraft.getInstance().screen instanceof DisableStaminaRender||
				event.getType()!=RenderGameOverlayEvent.ElementType.ALL||
				!(ModCfg.paraglidingConsumesStamina()||ModCfg.runningConsumesStamina())) return;
		Window window = event.getWindow();

		int x = Mth.clamp((int)Math.round(ModCfg.staminaWheelX()*window.getGuiScaledWidth()), 1+WHEEL_RADIUS, window.getGuiScaledWidth()-2-WHEEL_RADIUS);
		int y = Mth.clamp((int)Math.round(ModCfg.staminaWheelY()*window.getGuiScaledHeight()), 1+WHEEL_RADIUS, window.getGuiScaledHeight()-2-WHEEL_RADIUS);

		STAMINA_WHEEL_RENDERER.renderStamina(event.getMatrixStack(), x, y, 25);
	}

	@SubscribeEvent
	public static void beforeGameOverlayLayerRender(RenderGameOverlayEvent.PreLayer event){
		if(event.getOverlay()==ForgeIngameGui.CROSSHAIR_ELEMENT&&Minecraft.getInstance().screen instanceof StatueBargainScreen)
			event.setCanceled(true);
	}

	@SubscribeEvent
	public static void onClientTick(TickEvent.ClientTickEvent event){
		if(event.phase!=TickEvent.Phase.END) return;
		if(Minecraft.getInstance().screen==null&&paragliderSettingsKey().consumeClick()){
			Minecraft.getInstance().setScreen(new ParagliderSettingScreen());
		}
	}

	@SubscribeEvent
	public static void onClickInput(InputEvent.ClickInputEvent event) {
		// disables all interactions while paragliding
		// this is necessary in addition to cancelling interactions in ParagliderEventHandler to also prevent the arm swing animation from playing
		Player player = Minecraft.getInstance().player;
		PlayerMovement m = PlayerMovement.of(player);
		if(m!=null&&m.isParagliding()) {
			event.setSwingHand(false);
			event.setCanceled(true);
		}
	}

	@SubscribeEvent
	public static void onDrawBlockSelection(DrawSelectionEvent.HighlightBlock event) {
		// disables drawing block highlights while paragliding (as blocks cannot be interacted with, just a convenience feature to avoid confusing players)
		Player player = Minecraft.getInstance().player;
		PlayerMovement m = PlayerMovement.of(player);
		if(m!=null&&m.isParagliding()) event.setCanceled(true);
	}
}
