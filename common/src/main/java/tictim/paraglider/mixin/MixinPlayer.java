package tictim.paraglider.mixin;

import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import tictim.paraglider.api.movement.Movement;
import tictim.paraglider.config.Cfg;

import static org.spongepowered.asm.mixin.injection.At.Shift.BY;
import static tictim.paraglider.api.movement.ParagliderPlayerStates.Flags.FLAG_PARAGLIDING;

@Mixin(Player.class)
public abstract class MixinPlayer{
	@Inject(
			method = "getFlyingSpeed()F",
			at = {
					@At(value = "INVOKE",
							target = "net/minecraft/world/entity/player/Player.isSprinting()Z",
							ordinal = 1,
							shift = BY, by = -1)
			},
			cancellable = true
	)
	public void onGetFlyingSpeed(CallbackInfoReturnable<Float> info){
		@SuppressWarnings("DataFlowIssue")
		Player player = (Player)(Object)this;
		Movement movement = Movement.get(player);

		final float defaultSprintingFlyingSpeed = 0.025999999F;

		if(movement.state().has(FLAG_PARAGLIDING)){
			double v = Cfg.get().paraglidingSpeed();
			info.setReturnValue((float)(defaultSprintingFlyingSpeed*v));
		}
	}
}
