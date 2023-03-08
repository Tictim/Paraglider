package tictim.paraglider.jei;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import net.minecraft.util.ResourceLocation;
import tictim.paraglider.client.screen.StatueBargainScreen;

import static tictim.paraglider.ParagliderMod.MODID;

@JeiPlugin
public final class ParagliderJeiPlugin implements IModPlugin{
	public static final ResourceLocation ID = new ResourceLocation(MODID, MODID);

	@Override public ResourceLocation getPluginUid(){
		return ID;
	}

	@Override public void registerGuiHandlers(IGuiHandlerRegistration registration){
		registration.addGuiScreenHandler(StatueBargainScreen.class, s -> null);
	}
}
