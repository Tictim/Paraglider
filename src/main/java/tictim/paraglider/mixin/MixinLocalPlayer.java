package tictim.paraglider.mixin;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(LocalPlayer.class)
public abstract class MixinLocalPlayer extends Player {
	@Shadow private boolean wasSprinting;

	public MixinLocalPlayer(Level level, BlockPos pos, float yRot, GameProfile gameProfile) {
		super(level, pos, yRot, gameProfile);
	}

	// gods be damned if someone's also overriding this
	@Override
	public void setSprinting(boolean sprinting) {
		if (isSprinting() != sprinting) {
			// force resync of sprinting state to prevent desync
			// MC's sprinting state can desync when two things happen:
			//  1. on both sides sprinting state is changed to certain value
			//  2. on client side sprinting state immediately changes back, on same tick
			// since there wasn't any changes compared to previous tick's state, client skips syncing changes
			this.wasSprinting = !sprinting;
		}
		super.setSprinting(sprinting);
	}
}
