package tictim.paraglider.mixin;

import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import tictim.paraglider.ModCfg;
import tictim.paraglider.capabilities.PlayerMovement;

import static org.spongepowered.asm.mixin.injection.At.Shift.BY;

@Mixin(Player.class)
public class MixinPlayer{
	@Inject(method = "getFlyingSpeed()F", at = {
			@At(value = "INVOKE",
					target = "net/minecraft/world/entity/player/Player.isSprinting()Z",
					ordinal = 1,
					shift = BY, by = -1)
	}, cancellable = true)
	public void onGetFlyingSpeed(CallbackInfoReturnable<Float> info){
		@SuppressWarnings("DataFlowIssue")
		Player player = (Player)(Object)this;
		PlayerMovement h = PlayerMovement.of(player);

		final float defaultSprintingFlyingSpeed = 0.025999999F;

		if(h!=null&&h.isParagliding()){
			double v = ModCfg.paraglidingSpeed();
			info.setReturnValue((float)(defaultSprintingFlyingSpeed*v));
		}else if(info.isCancelled()) throw new RuntimeException("tfw");
	}
}
