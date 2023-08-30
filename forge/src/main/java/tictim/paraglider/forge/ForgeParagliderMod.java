package tictim.paraglider.forge;

import net.minecraft.client.KeyMapping;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tictim.paraglider.ParagliderMod;
import tictim.paraglider.api.ParagliderAPI;
import tictim.paraglider.client.ParagliderClientSettings;
import tictim.paraglider.config.Cfg;
import tictim.paraglider.config.DebugCfg;
import tictim.paraglider.config.FeatureCfg;
import tictim.paraglider.contents.BargainTypeRegistry;
import tictim.paraglider.forge.config.ForgeCommonConfig;
import tictim.paraglider.forge.config.ForgeConfig;
import tictim.paraglider.forge.contents.ForgeBargainTypeRegistry;
import tictim.paraglider.forge.contents.ForgeContents;
import tictim.paraglider.forge.proxy.ClientProxy;
import tictim.paraglider.forge.proxy.CommonProxy;
import tictim.paraglider.impl.movement.PlayerStateConnectionMap;
import tictim.paraglider.impl.movement.PlayerStateMap;
import tictim.paraglider.network.ParagliderNetwork;
import tictim.paraglider.plugin.ParagliderPluginLoader;

@Mod(ParagliderAPI.MODID)
public class ForgeParagliderMod extends ParagliderMod{
	@NotNull public static ForgeParagliderMod instance(){
		return (ForgeParagliderMod)ParagliderMod.instance();
	}

	private final CommonProxy proxy = DistExecutor.unsafeRunForDist(
			() -> ClientProxy::new,
			() -> CommonProxy::new
	);

	private final ForgeCommonConfig commonCfg = new ForgeCommonConfig();
	private final ForgeContents contents = new ForgeContents();
	private final ForgeConfig config = new ForgeConfig();

	@Override @NotNull public Cfg getConfig(){
		return config;
	}
	@Override @NotNull public DebugCfg getDebugConfig(){
		return commonCfg;
	}
	@Override @NotNull public FeatureCfg getFeatureConfig(){
		return commonCfg;
	}

	@OnlyIn(Dist.CLIENT)
	@Override @NotNull public ParagliderClientSettings getClientSettings(){
		return proxy.getClientSettings();
	}

	@Override @NotNull public ForgeContents getContents(){
		return contents;
	}
	@Override @NotNull public ParagliderNetwork getNetwork(){
		return ForgeParagliderNetwork.get();
	}

	@Override @NotNull public BargainTypeRegistry getBargainTypeRegistry(){
		return ForgeBargainTypeRegistry.get();
	}
	@Override @NotNull public ParagliderPluginLoader getPluginLoader(){
		return ForgeParagliderPluginLoader.get();
	}

	@Override @NotNull public PlayerStateMap getPlayerStateMap(){
		return proxy.getStateMap();
	}
	@Override @NotNull public PlayerStateMap getLocalPlayerStateMap(){
		return proxy.getLocalStateMap();
	}
	@Override @NotNull public PlayerStateConnectionMap getPlayerConnectionMap(){
		return proxy.getConnectionMap();
	}

	@OnlyIn(Dist.CLIENT)
	@Override @NotNull public KeyMapping getParagliderSettingsKey(){
		return proxy.getParagliderSettingsKey();
	}
	@OnlyIn(Dist.CLIENT)
	@Override public void setSyncedPlayerStateMap(@Nullable PlayerStateMap stateMap){
		proxy.setSyncedStateMap(stateMap);
	}
}
