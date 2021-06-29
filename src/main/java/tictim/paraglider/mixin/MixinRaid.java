package tictim.paraglider.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.raid.Raid;
import net.minecraft.world.server.ServerWorld;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tictim.paraglider.ModCfg;
import tictim.paraglider.utils.ParagliderUtils;

import java.util.Set;
import java.util.UUID;

/**
 * SHADOW LEGENDS
 */
@Mixin(Raid.class)
public class MixinRaid{
	@Shadow @Final
	private Set<UUID> heroes;
	@Shadow @Final
	private ServerWorld world;

	@Inject(
			method = "tick()V",
			at = {
					@At(shift = Shift.AFTER, value = "FIELD", target = "Lnet/minecraft/world/raid/Raid;status:Lnet/minecraft/world/raid/Raid$Status;", opcode = Opcodes.PUTFIELD)
			},
			slice = {
					@Slice(from = @At(value = "INVOKE", target = "Lnet/minecraft/world/raid/Raid;isStarted()Z"))
			}
	)
	public void tick(CallbackInfo info){
		if(!ModCfg.raidGivesVessel()) return;
		Item item = ParagliderUtils.getAppropriateVessel();
		if(item==null) return;
		for(UUID uuid : heroes){
			Entity entity = world.getEntityByUuid(uuid);
			if(entity instanceof PlayerEntity&&!entity.isSpectator()){
				PlayerEntity player = (PlayerEntity)entity;
				ParagliderUtils.giveItem(player, new ItemStack(item));
			}
		}
	}
}
