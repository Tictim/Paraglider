package tictim.paraglider.fabric.config;

import fuzs.forgeconfigapiport.api.config.v2.ForgeConfigRegistry;
import net.minecraftforge.fml.config.ModConfig;
import tictim.paraglider.config.CommonConfig;

import static tictim.paraglider.api.ParagliderAPI.MODID;

public class FabricCommonConfig extends CommonConfig{
	public FabricCommonConfig(){
		ForgeConfigRegistry.INSTANCE.register(MODID, ModConfig.Type.COMMON, this.spec);
	}
}
