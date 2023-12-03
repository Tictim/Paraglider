package tictim.paraglider.fabric.config;

import fuzs.forgeconfigapiport.api.config.v2.ForgeConfigRegistry;
import fuzs.forgeconfigapiport.api.config.v2.ModConfigEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.config.ModConfig;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tictim.paraglider.config.PlayerStateMapConfig;
import tictim.paraglider.impl.movement.PlayerStateMap;

import static tictim.paraglider.api.ParagliderAPI.MODID;

public class FabricPlayerStateMapConfig extends PlayerStateMapConfig{
	@Nullable private MinecraftServer server;

	public FabricPlayerStateMapConfig(@NotNull PlayerStateMap originalStateMap){
		super(originalStateMap);
		ForgeConfigRegistry.INSTANCE.register(MODID, ModConfig.Type.COMMON, this.spec, FILENAME);
		ModConfigEvents.reloading(MODID).register(cfg -> {
			if(cfg.getSpec()==this.spec) scheduleReload(server, null);
		});
	}

	public void setServer(@Nullable MinecraftServer server){
		this.server = server;
	}
}
