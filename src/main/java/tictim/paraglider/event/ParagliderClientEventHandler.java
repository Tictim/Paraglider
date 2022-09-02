package tictim.paraglider.event;

import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.DrawHighlightEvent;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderHandEvent;
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

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import static tictim.paraglider.ParagliderMod.MODID;
import static tictim.paraglider.client.StaminaWheelConstants.WHEEL_RADIUS;

@Mod.EventBusSubscriber(modid = MODID, value = Dist.CLIENT)
public final class ParagliderClientEventHandler{
	private ParagliderClientEventHandler(){}

	private static KeyBinding paragliderSettingsKey;

	public static KeyBinding paragliderSettingsKey(){
		return paragliderSettingsKey;
	}
	public static void setParagliderSettingsKey(KeyBinding keyBinding){
		if(paragliderSettingsKey!=null) throw new IllegalStateException("no");
		paragliderSettingsKey = keyBinding;
	}

	@SubscribeEvent
	public static void onOffHandRender(RenderHandEvent event){
		if(event.getHand()!=Hand.OFF_HAND) return;
		ClientPlayerEntity player = Minecraft.getInstance().player;
		if(player==null) return;
		PlayerMovement m = PlayerMovement.of(player);
		if(m!=null&&m.isParagliding()) event.setCanceled(true);
	}

	private static final DecimalFormat PERCENTAGE = new DecimalFormat("#.#%");

	@SubscribeEvent
	public static void onGameOverlayTextRender(RenderGameOverlayEvent.Text event){
		if(ModCfg.debugPlayerMovement()){
			PlayerEntity p = Minecraft.getInstance().player;
			if(p!=null){
				PlayerMovement h = PlayerMovement.of(p);
				if(h!=null){
					ArrayList<String> right = event.getRight();
					List<String> arr = new ArrayList<>();

					arr.add("State: "+h.getState());
					arr.add((h.isDepleted() ? TextFormatting.RED : "")+"Stamina: "+h.getStamina()+" / "+h.getMaxStamina());
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
		if(Minecraft.getInstance().currentScreen instanceof DisableStaminaRender||
				event.getType()!=RenderGameOverlayEvent.ElementType.ALL||
				!(ModCfg.paraglidingConsumesStamina()||ModCfg.runningConsumesStamina())) return;
		MainWindow window = event.getWindow();

		int x = MathHelper.clamp((int)Math.round(ModCfg.staminaWheelX()*window.getScaledWidth()), 1+WHEEL_RADIUS, window.getScaledWidth()-2-WHEEL_RADIUS);
		int y = MathHelper.clamp((int)Math.round(ModCfg.staminaWheelY()*window.getScaledHeight()), 1+WHEEL_RADIUS, window.getScaledHeight()-2-WHEEL_RADIUS);

		STAMINA_WHEEL_RENDERER.renderStamina(event.getMatrixStack(), x, y, 25);
	}

	@SubscribeEvent
	public static void beforeGameOverlayRender(RenderGameOverlayEvent.Pre event){
		if(event.getType()==RenderGameOverlayEvent.ElementType.CROSSHAIRS&&Minecraft.getInstance().currentScreen instanceof StatueBargainScreen)
			event.setCanceled(true);
	}

	@SubscribeEvent
	public static void onClientTick(TickEvent.ClientTickEvent event){
		if(event.phase!=TickEvent.Phase.END) return;
		if(Minecraft.getInstance().currentScreen==null&&paragliderSettingsKey().isPressed()){
			Minecraft.getInstance().displayGuiScreen(new ParagliderSettingScreen());
		}
	}

	@SubscribeEvent
	public static void onClickInput(InputEvent.ClickInputEvent event){
		// disables all interactions while paragliding
		// this is necessary in addition to cancelling interactions in ParagliderEventHandler to also prevent the arm swing animation from playing
		PlayerEntity player = Minecraft.getInstance().player;
		PlayerMovement m = PlayerMovement.of(player);
		if(m!=null&&m.isParagliding()){
			event.setSwingHand(false);
			event.setCanceled(true);
		}
	}

	@SubscribeEvent
	public static void onDrawBlockSelection(DrawHighlightEvent.HighlightBlock event){
		// disables drawing block highlights while paragliding (as blocks cannot be interacted with, just a convenience feature to avoid confusing players)
		PlayerEntity player = Minecraft.getInstance().player;
		PlayerMovement m = PlayerMovement.of(player);
		if(m!=null&&m.isParagliding()) event.setCanceled(true);
	}
}
