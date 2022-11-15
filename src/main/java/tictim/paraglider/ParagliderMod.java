package tictim.paraglider;

import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.MenuScreens.ScreenConstructor;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.client.renderer.item.ItemPropertyFunction;
import net.minecraft.core.cauldron.CauldronInteraction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeableLeatherItem;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.event.entity.EntityAttributeModificationEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import tictim.paraglider.capabilities.Paraglider;
import tictim.paraglider.capabilities.PlayerMovement;
import tictim.paraglider.capabilities.Stamina;
import tictim.paraglider.client.overlay.StaminaWheelOverlay;
import tictim.paraglider.client.screen.StatueBargainScreen;
import tictim.paraglider.contents.Contents;
import tictim.paraglider.contents.ModVillageStructures;
import tictim.paraglider.contents.item.ParagliderItem;
import tictim.paraglider.network.ModNet;
import tictim.paraglider.contents.recipe.ConfigConditionSerializer;
import tictim.paraglider.contents.recipe.bargain.StatueBargainContainer;
import tictim.paraglider.wind.Wind;

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
		for(ConfigConditionSerializer c : ConfigConditionSerializer.values()) CraftingHelper.register(c);
	}

	@SubscribeEvent
	public static void setup(FMLCommonSetupEvent event){
		event.enqueueWork(() -> {
			ModVillageStructures.addVillageStructures();
			CauldronInteraction.WATER.put(Contents.PARAGLIDER.get(), CauldronInteraction.DYED_ITEM);
			CauldronInteraction.WATER.put(Contents.DEKU_LEAF.get(), CauldronInteraction.DYED_ITEM);
		});
	}

	@SubscribeEvent
	public static void registerCapabilities(RegisterCapabilitiesEvent event){
		event.register(PlayerMovement.class);
		event.register(Paraglider.class);
		event.register(Wind.class);
		event.register(Stamina.class);
	}

	@SubscribeEvent
	public static void onEntityAttributeModification(EntityAttributeModificationEvent event){
		event.add(EntityType.PLAYER, Contents.MAX_STAMINA.get());
	}

	@Mod.EventBusSubscriber(modid = ParagliderMod.MODID, bus = Bus.MOD, value = Dist.CLIENT)
	private static final class ClientHandler{
		private ClientHandler(){}

		@SubscribeEvent
		public static void clientSetup(FMLClientSetupEvent event){
			event.enqueueWork(() -> {
				@SuppressWarnings("deprecation") ItemPropertyFunction itemPropertyGetter =
						(stack, world, entity, i) -> entity instanceof Player&&ParagliderItem.isItemParagliding(stack) ? 1 : 0;

				ItemProperties.register(Contents.PARAGLIDER.get(), new ResourceLocation("paragliding"), itemPropertyGetter);
				ItemProperties.register(Contents.DEKU_LEAF.get(), new ResourceLocation("paragliding"), itemPropertyGetter);

				ScreenConstructor<StatueBargainContainer, StatueBargainScreen> f = StatueBargainScreen::new;
				MenuScreens.register(Contents.GODDESS_STATUE_CONTAINER.get(), f);
				MenuScreens.register(Contents.HORNED_STATUE_CONTAINER.get(), f);
			});
		}

		@SubscribeEvent
		public static void registerItemColorHandlers(RegisterColorHandlersEvent.Item event){
			event.register((stack, tint) -> tint>0 ? -1 : ((DyeableLeatherItem)stack.getItem()).getColor(stack),
					Contents.PARAGLIDER.get(),
					Contents.DEKU_LEAF.get());
		}

		@SubscribeEvent
		public static void registerGuiOverlays(RegisterGuiOverlaysEvent event){
			event.registerAboveAll("stamina_wheel", new StaminaWheelOverlay());
		}
	}
}
