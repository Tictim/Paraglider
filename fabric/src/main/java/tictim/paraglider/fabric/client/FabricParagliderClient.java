package tictim.paraglider.fabric.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.event.client.player.ClientPreAttackCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.renderer.item.ItemProperties;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;
import tictim.paraglider.client.ParagliderClientSettings;
import tictim.paraglider.client.ParagliderItemColor;
import tictim.paraglider.client.ParagliderItemProperty;
import tictim.paraglider.contents.Contents;
import tictim.paraglider.fabric.FabricParagliderMod;
import tictim.paraglider.fabric.FabricParagliderNetwork;
import tictim.paraglider.fabric.event.ParagliderClientEventHandler;
import tictim.paraglider.impl.movement.PlayerStateMap;
import tictim.paraglider.wind.Wind;
import tictim.paraglider.wind.WindUtils;

@SuppressWarnings("unused")
public final class FabricParagliderClient implements ClientModInitializer{
	private static FabricParagliderClient instance;

	@NotNull public static FabricParagliderClient get(){
		if(instance==null) throw new IllegalStateException("Mod instance not ready yet");
		return instance;
	}

	private final ParagliderClientSettings clientSettings = new ParagliderClientSettings(FabricLoader.getInstance().getGameDir());

	@Nullable private KeyMapping settingsKey;
	@Nullable private PlayerStateMap syncedStateMap;

	public FabricParagliderClient(){
		if(instance!=null) throw new IllegalStateException("Paraglider mod instantiated twice");
		instance = this;
	}

	@Override public void onInitializeClient(){
		FabricParagliderNetwork.clientInit();

		Contents contents = Contents.get();
		ColorProviderRegistry.ITEM.register(new ParagliderItemColor(contents.paraglider()), contents.paraglider());
		ColorProviderRegistry.ITEM.register(new ParagliderItemColor(contents.dekuLeaf()), contents.dekuLeaf());

		ItemProperties.register(Contents.get().paraglider(), ParagliderItemProperty.KEY_PARAGLIDING, ParagliderItemProperty.get());
		ItemProperties.register(Contents.get().dekuLeaf(), ParagliderItemProperty.KEY_PARAGLIDING, ParagliderItemProperty.get());

		this.settingsKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
				"key.paraglider.paragliderSettings",
				GLFW.GLFW_KEY_P, "key.categories.misc"));

		HudRenderCallback.EVENT.register((guiGraphics, delta) -> ParagliderClientEventHandler.renderHUD(guiGraphics));
		ClientPreAttackCallback.EVENT.register((mc, player, clickCount) -> ParagliderClientEventHandler.beforeAttack(player));
		WorldRenderEvents.BEFORE_BLOCK_OUTLINE.register((ctx, hitResult) -> ParagliderClientEventHandler.beforeBlockOutline());

		ClientTickEvents.END_WORLD_TICK.register(level -> {
			Wind wind = Wind.of(level);
			if(wind!=null) WindUtils.placeWindParticles(level, wind);
		});

		ServerLifecycleEvents.SERVER_STARTING.register(server -> this.syncedStateMap = null);
	}

	@NotNull public ParagliderClientSettings getClientSettings(){
		return clientSettings;
	}

	public KeyMapping getParagliderSettingsKey(){
		if(this.settingsKey==null) throw new IllegalStateException("Key not available yet");
		return this.settingsKey;
	}

	@NotNull public PlayerStateMap getPlayerStateMap(){
		return syncedStateMap==null ? FabricParagliderMod.get().getLocalPlayerStateMap() : syncedStateMap;
	}
	public void setSyncedPlayerStateMap(@Nullable PlayerStateMap stateMap){
		this.syncedStateMap = stateMap;
	}
}
