package tictim.paraglider.fabric.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tictim.paraglider.client.screen.BargainScreen;

@Mixin(Gui.class)
public abstract class MixinGui{
	@Inject(
			at = @At(value = "HEAD"),
			method = "renderCrosshair(Lnet/minecraft/client/gui/GuiGraphics;)V",
			cancellable = true
	)
	public void onRenderCrosshair(CallbackInfo info){
		if(Minecraft.getInstance().screen instanceof BargainScreen){
			info.cancel();
		}
	}
}
