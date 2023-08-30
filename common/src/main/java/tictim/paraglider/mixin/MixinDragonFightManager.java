package tictim.paraglider.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.dimension.end.EndDragonFight;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.EndPodiumFeature;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tictim.paraglider.ParagliderUtils;
import tictim.paraglider.config.Cfg;

@Mixin(EndDragonFight.class)
public abstract class MixinDragonFightManager{
	@Shadow @Final
	private ServerLevel level;

	@Inject(
			method = "setDragonKilled(Lnet/minecraft/world/entity/boss/enderdragon/EnderDragon;)V",
			at = {
					@At(shift = Shift.AFTER, value = "FIELD", target = "Lnet/minecraft/world/level/dimension/end/EndDragonFight;dragonKilled:Z", opcode = Opcodes.PUTFIELD)
			}
	)
	public void processDragonDeath(EnderDragon entity, CallbackInfo info){
		if(!Cfg.get().enderDragonDropsVessel()) return;
		Item item = ParagliderUtils.getAppropriateVessel();
		if(item==null) return;
		BlockPos endPodium = this.level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, EndPodiumFeature.getLocation(BlockPos.ZERO));
		ItemEntity itemEntity = new ItemEntity(level, endPodium.getX()+.5, endPodium.getY()+1, endPodium.getZ()+.5, new ItemStack(item));
		itemEntity.setInvulnerable(true);
		itemEntity.setExtendedLifetime();
		itemEntity.setNoGravity(true);
		itemEntity.setPickUpDelay(40);
		itemEntity.setDeltaMovement(0, 0, 0);
		this.level.addFreshEntity(itemEntity);
	}
}
