package tictim.paraglider.forge.config;

import net.minecraft.server.MinecraftServer;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.server.ServerLifecycleHooks;
import tictim.paraglider.config.LocalConfig;

public final class ForgeConfig extends LocalConfig{
	public ForgeConfig(){
		ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, this.spec);
		IEventBus eventBus = FMLJavaModLoadingContext.get().getModEventBus();
		eventBus.addListener(this::onLoad);
		eventBus.addListener(this::onReload);
	}

	private void onLoad(ModConfigEvent.Loading event){
		if(event.getConfig().getSpec()==this.spec) reloadWindSources();
	}

	private void onReload(ModConfigEvent.Reloading event){
		if(event.getConfig().getSpec()==this.spec){
			MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
			if(server!=null) server.execute(this::reloadWindSources);
		}
	}
}
