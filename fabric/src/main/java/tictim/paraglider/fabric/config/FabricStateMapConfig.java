package tictim.paraglider.fabric.config;

import fuzs.forgeconfigapiport.api.config.v2.ForgeConfigRegistry;
import fuzs.forgeconfigapiport.api.config.v2.ModConfigEvents;
import net.minecraftforge.fml.config.ModConfig;
import org.jetbrains.annotations.NotNull;
import tictim.paraglider.config.StateMapConfig;
import tictim.paraglider.impl.movement.PlayerStateMap;

import static tictim.paraglider.api.ParagliderAPI.MODID;

public class FabricStateMapConfig extends StateMapConfig{
	public FabricStateMapConfig(@NotNull PlayerStateMap originalStateMap){
		super(originalStateMap);
		ForgeConfigRegistry.INSTANCE.register(MODID, ModConfig.Type.COMMON, this.spec, FILENAME);
		ModConfigEvents.reloading(MODID).register(cfg -> {
			if(cfg.getSpec()==this.spec) scheduleReload();
		});
	}
}
