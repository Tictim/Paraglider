package tictim.paraglider;

import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.IDyeableArmorItem;
import net.minecraft.item.IItemPropertyGetter;
import net.minecraft.item.ItemModelsProperties;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import tictim.paraglider.capabilities.Paraglider;
import tictim.paraglider.capabilities.PlayerMovement;
import tictim.paraglider.contents.Contents;
import tictim.paraglider.contents.WindEntity;
import tictim.paraglider.item.ParagliderItem;
import tictim.paraglider.network.ModNet;

import javax.annotation.Nullable;
import javax.naming.OperationNotSupportedException;

@Mod(ParagliderMod.MODID)
@Mod.EventBusSubscriber(modid = ParagliderMod.MODID, bus = Bus.MOD)
public class ParagliderMod{
	public static final String MODID = "paraglider";
	public static final Logger LOGGER = LogManager.getLogger("Paraglider");

	public ParagliderMod(){
		IEventBus eventBus = FMLJavaModLoadingContext.get().getModEventBus();
		Contents.registerEventHandlers(eventBus);
		ModCfg.init();
		ModNet.init();
	}

	@SubscribeEvent
	public static void setup(FMLCommonSetupEvent event){
		registerDefaultCapability(PlayerMovement.class);
		registerDefaultCapability(Paraglider.class);
	}

	private static <T> void registerDefaultCapability(Class<T> classOf){
		CapabilityManager.INSTANCE.register(classOf, new Capability.IStorage<T>(){
			@Nullable @Override public INBT writeNBT(Capability<T> capability, T instance, Direction side){
				return null;
			}
			@Override public void readNBT(Capability<T> capability, T instance, Direction side, INBT nbt){}
		}, () -> {
			throw new OperationNotSupportedException();
		});
	}

	@Mod.EventBusSubscriber(modid = ParagliderMod.MODID, bus = Bus.MOD, value = Dist.CLIENT)
	private static final class ClientHandler{
		private ClientHandler(){}

		@SubscribeEvent
		public static void clientSetup(FMLClientSetupEvent event){
			RenderingRegistry.registerEntityRenderingHandler(Contents.WIND.get(), m -> new EntityRenderer<WindEntity>(m){
				@Override public ResourceLocation getEntityTexture(WindEntity entity){
					return new ResourceLocation("missing");
				}
			});

			IItemPropertyGetter itemPropertyGetter = (stack, world, entity) -> {
				return entity instanceof PlayerEntity&&ParagliderItem.hasParaglidingFlag(stack) ? 1 : 0;
			};

			ItemModelsProperties.func_239418_a_(Contents.PARAGLIDER.get(), new ResourceLocation("paragliding"), itemPropertyGetter);
			ItemModelsProperties.func_239418_a_(Contents.DEKU_LEAF.get(), new ResourceLocation("paragliding"), itemPropertyGetter);
		}

		@SubscribeEvent
		public static void addColorHandler(ColorHandlerEvent.Item event){
			event.getItemColors().register((stack, tint) -> tint>0 ? -1 : ((IDyeableArmorItem)stack.getItem()).getColor(stack),
					Contents.PARAGLIDER.get(),
					Contents.DEKU_LEAF.get());
		}
	}
}
