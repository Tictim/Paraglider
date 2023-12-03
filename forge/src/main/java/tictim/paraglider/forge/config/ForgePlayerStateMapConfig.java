package tictim.paraglider.forge.config;

import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.jetbrains.annotations.NotNull;
import tictim.paraglider.config.PlayerStateMapConfig;
import tictim.paraglider.impl.movement.PlayerStateMap;

public class ForgePlayerStateMapConfig extends PlayerStateMapConfig{
	public ForgePlayerStateMapConfig(@NotNull PlayerStateMap originalStateMap){
		super(originalStateMap);
		ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, this.spec, FILENAME);
		FMLJavaModLoadingContext.get().getModEventBus().addListener((ModConfigEvent.Reloading event) -> {
			if(event.getConfig().getSpec()==this.spec) scheduleReload(ServerLifecycleHooks.getCurrentServer(), null);
		});
	}
}
