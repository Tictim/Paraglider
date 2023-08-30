package tictim.paraglider.fabric.mixin;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.ItemInHandRenderer.HandRenderSelection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import tictim.paraglider.api.movement.Movement;

import static tictim.paraglider.api.movement.ParagliderPlayerStates.Flags.FLAG_PARAGLIDING;

@Mixin(ItemInHandRenderer.class)
public abstract class MixinItemInHandRenderer{
	@Inject(
			at = @At("HEAD"),
			method = "evaluateWhichHandsToRender(Lnet/minecraft/client/player/LocalPlayer;)Lnet/minecraft/client/renderer/ItemInHandRenderer$HandRenderSelection;",
			cancellable = true
	)
	private static void onEvaluateWhichHandsToRender(LocalPlayer player, CallbackInfoReturnable<HandRenderSelection> info){
		if(Movement.get(player).state().has(FLAG_PARAGLIDING)){
			info.setReturnValue(HandRenderSelection.RENDER_MAIN_HAND_ONLY);
		}
	}
}
