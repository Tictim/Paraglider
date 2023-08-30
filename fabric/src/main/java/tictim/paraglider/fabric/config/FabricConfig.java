package tictim.paraglider.fabric.config;

import fuzs.forgeconfigapiport.api.config.v2.ForgeConfigRegistry;
import fuzs.forgeconfigapiport.api.config.v2.ModConfigEvents;
import net.minecraftforge.fml.config.ModConfig;
import tictim.paraglider.config.LocalConfig;

import static tictim.paraglider.api.ParagliderAPI.MODID;

public class FabricConfig extends LocalConfig{
	public FabricConfig(){
		ForgeConfigRegistry.INSTANCE.register(MODID, ModConfig.Type.SERVER, this.spec);
		ModConfigEvents.loading(MODID).register(this::onLoad);
		ModConfigEvents.reloading(MODID).register(this::onReload);
	}

	private void onLoad(ModConfig cfg){
		if(cfg.getSpec()==this.spec) reloadWindSources();
	}

	private void onReload(ModConfig cfg){
		if(cfg.getSpec()==this.spec){
			reloadWindSources();
		}
	}
}
