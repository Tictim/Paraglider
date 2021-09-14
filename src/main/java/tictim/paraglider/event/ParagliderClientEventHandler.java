package tictim.paraglider.event;

import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import tictim.paraglider.ModCfg;
import tictim.paraglider.capabilities.PlayerMovement;
import tictim.paraglider.client.InGameStaminaWheelRenderer;
import tictim.paraglider.client.StaminaWheelRenderer;
import tictim.paraglider.client.StatueBargainScreen;

import java.util.ArrayList;
import java.util.List;

import static tictim.paraglider.ParagliderMod.MODID;

@Mod.EventBusSubscriber(modid = MODID, value = Dist.CLIENT)
public final class ParagliderClientEventHandler{
	private ParagliderClientEventHandler(){}

	@SubscribeEvent
	public static void onOffHandRender(RenderHandEvent event){
		if(event.getHand()!=Hand.OFF_HAND) return;
		ClientPlayerEntity player = Minecraft.getInstance().player;
		if(player==null) return;
		PlayerMovement m = PlayerMovement.of(player);
		if(m!=null&&m.isParagliding()) event.setCanceled(true);
	}

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
					if(!right.isEmpty()) arr.add("");

					right.addAll(0, arr);
				}
			}
		}
	}

	private static final StaminaWheelRenderer STAMINA_WHEEL_RENDERER = new InGameStaminaWheelRenderer();

	@SubscribeEvent
	public static void afterGameOverlayRender(RenderGameOverlayEvent.Post event){
		if(Minecraft.getInstance().currentScreen instanceof StatueBargainScreen||
				event.getType()!=RenderGameOverlayEvent.ElementType.ALL||
				!(ModCfg.paraglidingConsumesStamina()||ModCfg.runningConsumesStamina())) return;
		MainWindow window = event.getWindow();
		//noinspection IntegerDivisionInFloatingPointContext
		STAMINA_WHEEL_RENDERER.renderStamina(event.getMatrixStack(),
				window.getScaledWidth()/2-100,
				window.getScaledHeight()/2-15,
				25);
	}

	@SubscribeEvent
	public static void beforeGameOverlayRender(RenderGameOverlayEvent.Pre event){
		if(event.getType()==RenderGameOverlayEvent.ElementType.CROSSHAIRS&&Minecraft.getInstance().currentScreen instanceof StatueBargainScreen)
			event.setCanceled(true);
	}
}
