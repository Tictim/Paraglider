package tictim.paraglider.forge.config;

import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.jetbrains.annotations.NotNull;
import tictim.paraglider.config.StateMapConfig;
import tictim.paraglider.impl.movement.PlayerStateMap;

public class ForgeStateMapConfig extends StateMapConfig{
	public ForgeStateMapConfig(@NotNull PlayerStateMap originalStateMap){
		super(originalStateMap);
		ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, this.spec, FILENAME);
		FMLJavaModLoadingContext.get().getModEventBus().addListener((ModConfigEvent.Reloading event) -> {
			if(event.getConfig().getSpec()==this.spec) scheduleReload();
		});
	}
}
