package tictim.paraglider.mixin;

import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.end.DragonFightManager;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.gen.feature.EndPodiumFeature;
import net.minecraft.world.server.ServerWorld;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tictim.paraglider.ModCfg;
import tictim.paraglider.utils.ParagliderUtils;

@Mixin(DragonFightManager.class)
public class MixinDragonFightManager{
	@Shadow @Final
	private ServerWorld world;

	@Inject(
			method = "processDragonDeath(Lnet/minecraft/entity/boss/dragon/EnderDragonEntity;)V",
			at = {
					@At(shift = Shift.AFTER, value = "FIELD", target = "Lnet/minecraft/world/end/DragonFightManager;dragonKilled:Z", opcode = Opcodes.PUTFIELD)
			}
	)
	public void processDragonDeath(EnderDragonEntity entity, CallbackInfo info){
		if(!ModCfg.enderDragonDropsVessel()) return;
		Item item = ParagliderUtils.getAppropriateVessel();
		if(item==null) return;
		BlockPos endPodium = this.world.getHeight(Heightmap.Type.MOTION_BLOCKING, EndPodiumFeature.END_PODIUM_LOCATION);
		ItemEntity itemEntity = new ItemEntity(world, endPodium.getX()+.5, endPodium.getY()+1, endPodium.getZ()+.5, new ItemStack(item));
		itemEntity.setInvulnerable(true);
		itemEntity.setNoDespawn();
		itemEntity.setNoGravity(true);
		itemEntity.setPickupDelay(40);
		itemEntity.setMotion(0, 0, 0);
		this.world.addEntity(itemEntity);
	}
}
