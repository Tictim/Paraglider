package tictim.paraglider.forge.config;

import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import tictim.paraglider.config.CommonConfig;

public final class ForgeCommonConfig extends CommonConfig{
	public ForgeCommonConfig(){
		ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, this.spec);
	}
}
