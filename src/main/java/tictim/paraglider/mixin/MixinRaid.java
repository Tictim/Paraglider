package tictim.paraglider.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.raid.Raid;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
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
	private Set<UUID> heroesOfTheVillage;
	@Shadow @Final
	private ServerLevel level;

	@Inject(
			method = "tick()V",
			at = {
					@At(shift = Shift.AFTER, value = "FIELD", target = "Lnet/minecraft/world/entity/raid/Raid;status:Lnet/minecraft/world/entity/raid/Raid$RaidStatus;", opcode = Opcodes.PUTFIELD)
			},
			slice = {
					@Slice(from = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/raid/Raid;isStarted()Z"))
			}
	)
	public void tick(CallbackInfo info){
		if(!ModCfg.raidGivesVessel()) return;
		Item item = ParagliderUtils.getAppropriateVessel();
		if(item==null) return;
		for(UUID uuid : heroesOfTheVillage){
			if(level.getEntity(uuid) instanceof Player player&&!player.isSpectator()){
				ParagliderUtils.giveItem(player, new ItemStack(item));
			}
		}
	}
}
