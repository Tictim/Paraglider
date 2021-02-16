package tictim.paraglider.event;

import com.mojang.blaze3d.matrix.MatrixStack;
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
import tictim.paraglider.capabilities.Paraglider;
import tictim.paraglider.capabilities.PlayerMovement;
import tictim.paraglider.capabilities.PlayerState;
import tictim.paraglider.client.StaminaWheelRenderer;
import tictim.paraglider.client.StaminaWheelRenderer.Color;
import tictim.paraglider.client.StaminaWheelRenderer.WheelType;
import tictim.paraglider.contents.WindEntity;
import tictim.paraglider.item.ParagliderItem;

import java.util.ArrayList;
import java.util.List;

import static tictim.paraglider.ParagliderMod.MODID;

@Mod.EventBusSubscriber(modid = MODID, value = Dist.CLIENT)
public final class ParagliderClientEventHandler{
	private ParagliderClientEventHandler(){}

	@SubscribeEvent
	public static void onRenderOffHand(RenderHandEvent event){ // It looks so hacky lol
		if(event.getHand()==Hand.MAIN_HAND){
			if(ParagliderItem.hasParaglidingFlag(event.getItemStack())){
				Minecraft.getInstance().gameRenderer.itemRenderer.resetEquippedProgress(Hand.OFF_HAND);
			}
		}
	}

	@SubscribeEvent
	public static void debug(RenderGameOverlayEvent.Text event){
		if(ModCfg.debugPlayerMovement()){
			PlayerEntity p = Minecraft.getInstance().player;
			if(p!=null){
				PlayerMovement h = p.getCapability(PlayerMovement.CAP).orElse(null);
				if(h!=null){
					ArrayList<String> right = event.getRight();
					List<String> arr = new ArrayList<>();

					arr.add("State: "+h.getState());
					arr.add((h.isDepleted() ? TextFormatting.RED : "")+"Stamina: "+h.getStamina()+" / "+h.getMaxStamina());
					arr.add(h.getStaminaVessels()+" Stamina Vessels, "+h.getHeartContainers()+" Heart Containers");
					arr.add(h.getRecoveryDelay()+" Recovery Delay");
					arr.add("Paragliding: "+h.isParagliding());
					if(!right.isEmpty()) arr.add("");

					List<WindEntity> winds = p.world.getEntitiesWithinAABB(WindEntity.class, p.getBoundingBox().grow(5));
					if(!winds.isEmpty()){
						arr.add("Winds: "+winds.size()+" entries");
					}
					right.addAll(0, arr);
				}
			}
		}
	}

	private static final Color STAMINA_COLOR = Color.of(0, 223, 83);
	private static final Color GLOW_STAMINA_COLOR = Color.of(255, 255, 255);
	private static final Color TRANSPARENT_STAMINA_COLOR = Color.of(255, 255, 255, 0);
	private static final Color DEPLETION_ALTERNATION_1 = Color.of(150, 2, 2);
	private static final Color DEPLETION_ALTERNATION_2 = Color.of(255, 150, 2);
	private static final Color EMPTY_STAMINA_COLOR = Color.of(2, 2, 2, 150);

	private static final StaminaWheelRenderer staminaWheelRenderer = new StaminaWheelRenderer();
	private static int prevStamina;
	private static long fullTime;

	private static final long FULL_GLOW_END_START = 100;
	private static final long FULL_GLOW_END_TIME = 250;
	private static final long HIDE_START = 1000;
	private static final long HIDE_TIME = 100;

	private static final long ALTERNATION_CYCLE = 300;
	private static final long ALTERNATION_CYCLE_DEPLETED = 600;

	private static final Color colorCache = new Color(0, 0, 0, 0);

	private static final double WHEEL_SIZE = 10;

	@SubscribeEvent
	public static void renderStamina(RenderGameOverlayEvent.Post event){
		if(event.getType()==RenderGameOverlayEvent.ElementType.ALL){
			ClientPlayerEntity player = Minecraft.getInstance().player;
			if(player==null) return;

			PlayerMovement h = player.getCapability(PlayerMovement.CAP).orElse(null);
			if(h!=null){
				int stamina = h.getStamina();
				int maxStamina = h.getMaxStamina();
				if(stamina>=maxStamina){
					long time = System.currentTimeMillis();
					long timeDiff;
					if(prevStamina!=stamina){
						prevStamina = stamina;
						fullTime = time;
						timeDiff = 0;
					}else timeDiff = time-fullTime;
					if(timeDiff<FULL_GLOW_END_START){
						colorCache.set(GLOW_STAMINA_COLOR);
					}else if(timeDiff<FULL_GLOW_END_START+FULL_GLOW_END_TIME){
						blendColor(GLOW_STAMINA_COLOR, STAMINA_COLOR, (float)(timeDiff-FULL_GLOW_END_START)/(FULL_GLOW_END_TIME), colorCache);
					}else if(timeDiff>=HIDE_START&&timeDiff<HIDE_START+HIDE_TIME){
						blendColor(STAMINA_COLOR, TRANSPARENT_STAMINA_COLOR, (float)(timeDiff-HIDE_START)/HIDE_TIME, colorCache);
					}else if(timeDiff>=HIDE_START+HIDE_TIME) return;
					for(WheelType t : WheelType.values()){
						staminaWheelRenderer.addWheel(t, 0, t.getProportion(stamina), colorCache);
					}
				}else{
					prevStamina = stamina;
					blendColor(DEPLETION_ALTERNATION_1, DEPLETION_ALTERNATION_2,
							cycle(System.currentTimeMillis(), h.isDepleted() ? ALTERNATION_CYCLE_DEPLETED : ALTERNATION_CYCLE), colorCache);
					PlayerState.StaminaAction staminaAction = h.getState().staminaAction;
					for(WheelType t : WheelType.values()){
						staminaWheelRenderer.addWheel(t, 0, t.getProportion(maxStamina), EMPTY_STAMINA_COLOR);
						if(h.isDepleted()){
							staminaWheelRenderer.addWheel(t, 0, t.getProportion(stamina), colorCache);
						}else{
							staminaWheelRenderer.addWheel(t, 0, t.getProportion(stamina), STAMINA_COLOR);
							if(staminaAction.isConsume&&(h.getState().isParagliding() ? ModCfg.paraglidingConsumesStamina() : ModCfg.runningConsumesStamina())){
								staminaWheelRenderer.addWheel(t, t.getProportion(stamina-staminaAction.change*10), t.getProportion(stamina), colorCache);
							}
						}
					}
				}
				MainWindow window = event.getWindow();
				MatrixStack stack = event.getMatrixStack();
				staminaWheelRenderer.render(
						stack,
						window.getScaledWidth()/2-100,
						window.getScaledHeight()/2-15,
						25,
						WHEEL_SIZE,
						ModCfg.debugPlayerMovement()&&player.getHeldItemOffhand().getCapability(Paraglider.CAP).isPresent());
			}
		}
	}

	private static float cycle(long currentTime, long cycleTime){
		return (float)Math.abs(currentTime%cycleTime-cycleTime/2)/(cycleTime/2);
	}

	private static void blendColor(Color color1, Color color2, float blend, Color dest){
		dest.red = color1.red+((color2.red-color1.red)*blend);
		dest.green = color1.green+((color2.green-color1.green)*blend);
		dest.blue = color1.blue+((color2.blue-color1.blue)*blend);
		dest.alpha = color1.alpha+((color2.alpha-color1.alpha)*blend);
	}
}
