package tictim.paraglider;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.client.gui.ScreenManager.IScreenFactory;
import net.minecraft.data.DataGenerator;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.IDyeableArmorItem;
import net.minecraft.item.IItemPropertyGetter;
import net.minecraft.item.ItemModelsProperties;
import net.minecraft.nbt.INBT;
import net.minecraft.resources.IReloadableResourceManager;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.GatherDataEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import tictim.paraglider.capabilities.Paraglider;
import tictim.paraglider.capabilities.PlayerMovement;
import tictim.paraglider.capabilities.wind.Wind;
import tictim.paraglider.client.DialogScreen;
import tictim.paraglider.contents.Contents;
import tictim.paraglider.contents.Dialogs;
import tictim.paraglider.datagen.BlockTagGen;
import tictim.paraglider.datagen.ItemTagGen;
import tictim.paraglider.datagen.LootTableGen;
import tictim.paraglider.datagen.RecipeGen;
import tictim.paraglider.dialog.Dialog;
import tictim.paraglider.dialog.DialogContainer;
import tictim.paraglider.dialog.DialogReloadListener;
import tictim.paraglider.dialog.parser.DialogJson;
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
		registerDefaultCapability(Wind.class);
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

	@SubscribeEvent
	public static void gatherData(GatherDataEvent event){
		DataGenerator gen = event.getGenerator();
		if(event.includeServer()){
			gen.addProvider(new RecipeGen(gen));
			BlockTagGen blockTagGen = new BlockTagGen(gen, event.getExistingFileHelper());
			gen.addProvider(blockTagGen);
			gen.addProvider(new ItemTagGen(gen, blockTagGen, event.getExistingFileHelper()));
			gen.addProvider(new LootTableGen(gen));
		}
	}

	@Mod.EventBusSubscriber(modid = ParagliderMod.MODID, bus = Bus.MOD, value = Dist.CLIENT)
	private static final class ClientHandler{
		private ClientHandler(){}

		@SubscribeEvent
		public static void clientSetup(FMLClientSetupEvent event){
			IItemPropertyGetter itemPropertyGetter = (stack, world, entity) -> {
				return entity instanceof PlayerEntity&&ParagliderItem.hasParaglidingFlag(stack) ? 1 : 0;
			};

			ItemModelsProperties.registerProperty(Contents.PARAGLIDER.get(), new ResourceLocation("paragliding"), itemPropertyGetter);
			ItemModelsProperties.registerProperty(Contents.DEKU_LEAF.get(), new ResourceLocation("paragliding"), itemPropertyGetter);

			IScreenFactory<DialogContainer, DialogScreen> f = (container, playerInventory, title) -> new DialogScreen(container);
			for(Dialog dialog : Dialogs.DIALOGS_BY_NAME.values()){
				ScreenManager.registerFactory(dialog.getContainerType(), f);
			}

			if(ModCfg.debugDialogLoading())
				DialogJson.Client.readFromResource(new ResourceLocation(MODID, "parser_test"));

			IResourceManager resourceManager = Minecraft.getInstance().getResourceManager();
			if(resourceManager instanceof IReloadableResourceManager){
				IReloadableResourceManager manager = (IReloadableResourceManager)resourceManager;
				for(Dialog dialog : Dialogs.DIALOGS_BY_NAME.values()){
					addListener(manager, dialog);
				}
			}
		}

		private static void addListener(IReloadableResourceManager manager, Dialog dialog){
			DialogReloadListener listener = new DialogReloadListener(dialog, MODID);
			listener.loadScenario();
			manager.addReloadListener(listener);
		}

		@SubscribeEvent
		public static void addColorHandler(ColorHandlerEvent.Item event){
			event.getItemColors().register((stack, tint) -> tint>0 ? -1 : ((IDyeableArmorItem)stack.getItem()).getColor(stack),
					Contents.PARAGLIDER.get(),
					Contents.DEKU_LEAF.get());
		}
	}
}
