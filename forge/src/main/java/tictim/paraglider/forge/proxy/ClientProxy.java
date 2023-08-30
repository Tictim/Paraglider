package tictim.paraglider.forge.proxy;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.client.settings.KeyModifier;
import net.minecraftforge.event.server.ServerAboutToStartEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;
import tictim.paraglider.client.ParagliderClientSettings;
import tictim.paraglider.client.ParagliderItemColor;
import tictim.paraglider.client.ParagliderItemProperty;
import tictim.paraglider.contents.Contents;
import tictim.paraglider.forge.client.StaminaWheelOverlay;
import tictim.paraglider.impl.movement.PlayerStateMap;

public class ClientProxy extends CommonProxy{
	private final ParagliderClientSettings clientSettings = new ParagliderClientSettings(FMLPaths.GAMEDIR.get());

	@Nullable private PlayerStateMap syncedStateMap;

	@Nullable private KeyMapping paragliderSettingsKey;

	{
		IEventBus eventBus = FMLJavaModLoadingContext.get().getModEventBus();
		eventBus.addListener((FMLClientSetupEvent e) -> e.enqueueWork(() -> {
			ItemProperties.register(Contents.get().paraglider(), ParagliderItemProperty.KEY_PARAGLIDING, ParagliderItemProperty.get());
			ItemProperties.register(Contents.get().dekuLeaf(), ParagliderItemProperty.KEY_PARAGLIDING, ParagliderItemProperty.get());
		}));
		eventBus.addListener((RegisterKeyMappingsEvent e) -> e.register(paragliderSettingsKey = new KeyMapping(
				"key.paraglider.paragliderSettings",
				KeyConflictContext.IN_GAME,
				KeyModifier.CONTROL,
				InputConstants.Type.KEYSYM,
				GLFW.GLFW_KEY_P, "key.categories.misc")));
		eventBus.addListener((RegisterColorHandlersEvent.Item e) -> {
			e.register(new ParagliderItemColor(Contents.get().paraglider()), Contents.get().paraglider());
			e.register(new ParagliderItemColor(Contents.get().dekuLeaf()), Contents.get().dekuLeaf());
		});
		eventBus.addListener((RegisterGuiOverlaysEvent e) -> e.registerAboveAll("stamina_wheel", new StaminaWheelOverlay()));

		clientSettings.load();
	}

	@Override protected void onServerAboutToStart(ServerAboutToStartEvent event){
		super.onServerAboutToStart(event);
		this.syncedStateMap = null;
	}

	@Override @NotNull public ParagliderClientSettings getClientSettings(){
		return clientSettings;
	}

	@Override @NotNull public PlayerStateMap getStateMap(){
		return syncedStateMap!=null ? syncedStateMap : super.getStateMap();
	}

	@Override public void setSyncedStateMap(@Nullable PlayerStateMap stateMap){
		this.syncedStateMap = stateMap;
	}

	@Override @NotNull public KeyMapping getParagliderSettingsKey(){
		if(paragliderSettingsKey==null) throw new IllegalStateException("paragliderSettingsKey is not available yet");
		return paragliderSettingsKey;
	}
}
