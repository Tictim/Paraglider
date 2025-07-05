package tictim.paraglider;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.*;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.client.renderstate.RegisterRenderStateModifiersEvent;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import net.neoforged.neoforge.client.settings.KeyModifier;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;
import net.neoforged.neoforge.registries.RegisterEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;
import tictim.paraglider.api.ParagliderAPI;
import tictim.paraglider.client.*;
import tictim.paraglider.client.settings.ParagliderClientSettings;
import tictim.paraglider.client.settings.ParagliderClientSettingsIO;
import tictim.paraglider.contents.ParagliderTags;
import tictim.paraglider.impl.movement.PlayerStateMap;

import java.util.Objects;

@Mod(value = ParagliderAPI.MODID, dist = Dist.CLIENT)
public class ParagliderClientMod implements ParagliderMod.IClient {
	private static ParagliderClientMod instance;

	public static @NotNull ParagliderClientMod instance() {
		if (instance == null) throw new IllegalStateException("Mod instance not ready yet");
		return instance;
	}

	{
		if (instance != null) throw new IllegalStateException("Paraglider mod instantiated twice");
		instance = this;
	}

	private @Nullable ParagliderClientSettings clientSettings;
	private @Nullable PlayerStateMap syncedStateMap;
	private @Nullable KeyMapping paragliderSettingsKey;

	public ParagliderClientMod(ModContainer modContainer, IEventBus eventBus) {
		ParagliderMod.instance().client = this;

		modContainer.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);

		eventBus.addListener((RegisterConditionalItemModelPropertyEvent event) -> {
			event.register(ParagliderAPI.id("paragliding"), ParaglidingItemProperty.CODEC);
		});

		eventBus.addListener((RegisterKeyMappingsEvent event) -> {
			event.register(this.paragliderSettingsKey = new KeyMapping(
					"key.paraglider.paragliderSettings",
					KeyConflictContext.IN_GAME,
					KeyModifier.CONTROL,
					InputConstants.Type.KEYSYM,
					GLFW.GLFW_KEY_P, "key.categories.misc"));
		});

		eventBus.addListener((RegisterGuiLayersEvent event) -> {
			event.registerAboveAll(ParagliderAPI.id("stamina_wheel"), ParagliderGuiLayers::renderStaminaWheel);
			event.registerAboveAll(ParagliderAPI.id("movement_debug"), ParagliderGuiLayers::renderMovementDebug);
		});

		eventBus.addListener((RegisterRenderStateModifiersEvent event) -> {
			event.registerEntityModifier(PlayerRenderer.class, (p, s) -> {
				ItemStack stack = p.getMainHandItem();
				if (stack.is(ParagliderTags.PARAGLIDERS) && ParagliderUtils.getCaps(stack).isParagliding(stack)) {
					s.leftArmPose = s.rightArmPose = ParaglidingArmPose.ENUM.getValue();
				}
			});
		});

		eventBus.addListener((RegisterRenderPipelinesEvent event) -> event.registerPipeline(ParagliderRenderTypes.STAMINA_WHEEL_PIPELINE));

		eventBus.addListener((RegisterEvent event) -> {
			event.register(Registries.PARTICLE_TYPE, h -> h.register(
					WindParticleProvider.PARTICLE_TYPE_ID, WindParticleProvider.PARTICLE_TYPE
			));
		});

		eventBus.addListener((RegisterParticleProvidersEvent event) -> {
			event.registerSpriteSet(WindParticleProvider.PARTICLE_TYPE, WindParticleProvider::new);
		});

		NeoForge.EVENT_BUS.addListener((ServerAboutToStartEvent event) -> this.syncedStateMap = null);

		ParagliderClientSettingsIO.load(null);
	}

	public @NotNull ParagliderClientSettings getSettings() {
		return Objects.requireNonNullElse(this.clientSettings, ParagliderClientSettings.DEFAULT);
	}

	public void setSettings(@Nullable ParagliderClientSettings settings) {
		this.clientSettings = settings;
	}

	public @NotNull KeyMapping getParagliderSettingsKey() {
		if (this.paragliderSettingsKey == null)
			throw new IllegalStateException("paragliderSettingsKey is not available yet");
		return this.paragliderSettingsKey;
	}

	@Override public @Nullable PlayerStateMap getSyncedStateMap() {
		return this.syncedStateMap;
	}
	public void setSyncedStateMap(@Nullable PlayerStateMap stateMap) {
		this.syncedStateMap = stateMap;
	}
}
