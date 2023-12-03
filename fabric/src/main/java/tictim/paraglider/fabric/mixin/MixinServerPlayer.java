package tictim.paraglider.fabric.mixin;

import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tictim.paraglider.api.movement.Movement;
import tictim.paraglider.impl.movement.ServerPlayerMovement;

@Mixin(ServerPlayer.class)
public abstract class MixinServerPlayer extends Player{
	public MixinServerPlayer(Level level, BlockPos blockPos, float f, GameProfile gameProfile){
		super(level, blockPos, f, gameProfile);
	}

	@Inject(at = @At("RETURN"), method = "triggerDimensionChangeTriggers(Lnet/minecraft/server/level/ServerLevel;)V")
	public void triggerDimensionChangeTriggers(ServerLevel level, CallbackInfo info){
		if(Movement.get(this) instanceof ServerPlayerMovement serverPlayerMovement){
			serverPlayerMovement.markForSync();
		}
	}
}