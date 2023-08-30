package tictim.paraglider.fabric.mixin;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tictim.paraglider.api.Serde;
import tictim.paraglider.fabric.impl.PlayerMovementAccess;
import tictim.paraglider.impl.movement.ClientPlayerMovement;
import tictim.paraglider.impl.movement.PlayerMovement;
import tictim.paraglider.impl.movement.RemotePlayerMovement;
import tictim.paraglider.impl.movement.ServerPlayerMovement;

import static tictim.paraglider.api.ParagliderAPI.MODID;

@Mixin(Player.class)
public abstract class MixinPlayer implements PlayerMovementAccess{
	private static final String TAG = MODID+"_player_movement";

	@Unique
	@Nullable private PlayerMovement movement;

	@Override @NotNull public PlayerMovement paragliderPlayerMovement(){
		if(this.movement==null){
			@SuppressWarnings("DataFlowIssue")
			Player player = (Player)(Object)this;
			this.movement = player instanceof ServerPlayer serverPlayer ?
					new ServerPlayerMovement(serverPlayer) :
					switch(FabricLoader.getInstance().getEnvironmentType()){
						case CLIENT -> Client.createPlayerMovement(player);
						case SERVER -> new RemotePlayerMovement(player);
					};
		}
		return this.movement;
	}

	@Inject(at = @At("RETURN"), method = "tick()V")
	public void onTick(CallbackInfo info){
		paragliderPlayerMovement().update();
	}

	@Inject(at = @At("RETURN"), method = "readAdditionalSaveData(Lnet/minecraft/nbt/CompoundTag;)V")
	public void onReadAdditionalSaveData(CompoundTag tag, CallbackInfo info){
		if(tag.contains(TAG, Tag.TAG_COMPOUND)&&paragliderPlayerMovement() instanceof Serde serde){
			serde.read(tag.getCompound(TAG));
		}
	}

	@Inject(at = @At("RETURN"), method = "addAdditionalSaveData(Lnet/minecraft/nbt/CompoundTag;)V")
	public void onAddAdditionalSaveData(CompoundTag tag, CallbackInfo info){
		if(paragliderPlayerMovement() instanceof Serde serde){
			tag.put(TAG, serde.write());
		}
	}

	private static final class Client{
		@NotNull static PlayerMovement createPlayerMovement(@NotNull Player player){
			return player instanceof LocalPlayer localPlayer ?
					new ClientPlayerMovement(localPlayer) :
					new RemotePlayerMovement(player);
		}
	}
}
