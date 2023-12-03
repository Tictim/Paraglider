package tictim.paraglider;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.KeyMapping;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tictim.paraglider.client.ParagliderClientSettings;
import tictim.paraglider.config.Cfg;
import tictim.paraglider.config.DebugCfg;
import tictim.paraglider.config.FeatureCfg;
import tictim.paraglider.config.PlayerStateMapConfig;
import tictim.paraglider.contents.BargainTypeRegistry;
import tictim.paraglider.contents.Contents;
import tictim.paraglider.impl.movement.PlayerStateConnectionMap;
import tictim.paraglider.impl.movement.PlayerStateMap;
import tictim.paraglider.network.ParagliderNetwork;
import tictim.paraglider.plugin.ParagliderPluginLoader;

public abstract class ParagliderMod{
	public static final Logger LOGGER = LogManager.getLogger("Paraglider");

	private static ParagliderMod instance;

	@NotNull public static ParagliderMod instance(){
		if(instance==null) throw new IllegalStateException("Mod instance not ready yet");
		return instance;
	}

	{
		if(instance!=null) throw new IllegalStateException("Paraglider mod instantiated twice");
		instance = this;
	}

	@NotNull public abstract Cfg getConfig();

	@NotNull public abstract DebugCfg getDebugConfig();
	@NotNull public abstract FeatureCfg getFeatureConfig();

	@Environment(EnvType.CLIENT)
	@NotNull public abstract ParagliderClientSettings getClientSettings();

	@NotNull public abstract Contents getContents();
	@NotNull public abstract ParagliderNetwork getNetwork();
	@NotNull public abstract BargainTypeRegistry getBargainTypeRegistry();
	@NotNull public abstract ParagliderPluginLoader getPluginLoader();

	@NotNull public abstract PlayerStateMap getPlayerStateMap();
	@NotNull public abstract PlayerStateMap getLocalPlayerStateMap();
	@NotNull public abstract PlayerStateConnectionMap getPlayerConnectionMap();
	@NotNull public abstract PlayerStateMapConfig getPlayerStateMapConfig();

	@Environment(EnvType.CLIENT)
	@NotNull public abstract KeyMapping getParagliderSettingsKey();
	@Environment(EnvType.CLIENT)
	public abstract void setSyncedPlayerStateMap(@Nullable PlayerStateMap stateMap);
}
