package tictim.paraglider.mixin;

import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.end.DragonFightManager;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.gen.feature.EndPodiumFeature;
import net.minecraft.world.server.ServerWorld;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tictim.paraglider.ModCfg;
import tictim.paraglider.contents.Contents;

@Mixin(DragonFightManager.class)
public class MixinDragonFightManager{
	@Shadow
	private ServerWorld world;

	@Inject(
			method = "processDragonDeath(Lnet/minecraft/entity/boss/dragon/EnderDragonEntity;)V",
			at = {
					@At(shift = Shift.AFTER, value = "FIELD", target = "Lnet/minecraft/world/end/DragonFightManager;dragonKilled:Z", opcode = Opcodes.PUTFIELD)
			}
	)
	public void processDragonDeath(EnderDragonEntity entity, CallbackInfo info){
		if(!ModCfg.enderDragonDropsHeartContainer()) return;
		BlockPos endPodium = this.world.getHeight(Heightmap.Type.MOTION_BLOCKING, EndPodiumFeature.END_PODIUM_LOCATION);
		ItemEntity item = new ItemEntity(world, endPodium.getX()+.5, endPodium.getY()+1, endPodium.getZ()+.5, new ItemStack(Contents.HEART_CONTAINER.get()));
		item.setInvulnerable(true);
		item.setNoDespawn();
		item.setNoGravity(true);
		item.setPickupDelay(40);
		item.setMotion(0, 0, 0);
		this.world.addEntity(item);
	}
}
