package tictim.paraglider.dialog;

import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.resource.IResourceType;
import net.minecraftforge.resource.ISelectiveResourceReloadListener;
import net.minecraftforge.resource.VanillaResourceType;
import tictim.paraglider.ParagliderMod;
import tictim.paraglider.dialog.parser.DialogJson;

import java.util.Objects;
import java.util.function.Predicate;

public final class DialogReloadListener implements ISelectiveResourceReloadListener{
	private final Dialog dialog;
	private final ResourceLocation dialogResourceLocation;

	public DialogReloadListener(Dialog dialog, String modid){
		this(dialog, new ResourceLocation(modid, dialog.getName()));
	}
	public DialogReloadListener(Dialog dialog, ResourceLocation dialogResourceLocation){
		this.dialog = Objects.requireNonNull(dialog);
		this.dialogResourceLocation = Objects.requireNonNull(dialogResourceLocation);
	}

	@Override public void onResourceManagerReload(IResourceManager resourceManager, Predicate<IResourceType> resourcePredicate){
		if(!resourcePredicate.test(VanillaResourceType.LANGUAGES)) return;
		loadScenario();
	}

	public void loadScenario(){
		try{
			dialog.setScenario(DialogJson.Client.readFromResource(dialogResourceLocation));
		}catch(Exception ex){
			ParagliderMod.LOGGER.error("Exception reading dialog "+dialog.getName()+":", ex);
			dialog.setScenario(null);
		}
	}
}
