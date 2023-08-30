package tictim.paraglider.fabric.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.DebugScreenOverlay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import tictim.paraglider.ParagliderUtils;

import java.util.List;

@Mixin(DebugScreenOverlay.class)
public abstract class MixinDebugScreenOverlay{
	@Inject(
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/gui/components/DebugScreenOverlay;renderLines(Lnet/minecraft/client/gui/GuiGraphics;Ljava/util/List;Z)V"),
			method = "drawSystemInformation(Lnet/minecraft/client/gui/GuiGraphics;)V",
			locals = LocalCapture.CAPTURE_FAILSOFT
	)
	public void onDrawSystemInformation(GuiGraphics guiGraphics, CallbackInfo info, List<String> list){
		Minecraft mc = Minecraft.getInstance();
		if(mc.player==null) return;
		ParagliderUtils.addDebugText(mc.player, list);
	}
}
