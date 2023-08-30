package tictim.paraglider.forge.capability;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tictim.paraglider.api.Serde;
import tictim.paraglider.impl.movement.PlayerMovement;

import java.util.Objects;

public class PlayerMovementProvider implements ICapabilityProvider{
	public static final Capability<PlayerMovement> PLAYER_MOVEMENT = CapabilityManager.get(new CapabilityToken<>(){});

	@NotNull public static PlayerMovementProvider create(@NotNull PlayerMovement movement){
		Objects.requireNonNull(movement);
		return movement instanceof Serde ? new Serializable(movement) : new PlayerMovementProvider(movement);
	}

	public final PlayerMovement movement;

	private PlayerMovementProvider(@NotNull PlayerMovement movement){
		this.movement = movement;
	}

	@Nullable private LazyOptional<PlayerMovement> self;

	@Override @NotNull public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction arg){
		if(cap==PLAYER_MOVEMENT){
			if(self==null) self = LazyOptional.of(() -> movement);
			return self.cast();
		}
		return LazyOptional.empty();
	}

	@SuppressWarnings("ConstantConditions")
	@Nullable public static PlayerMovement of(@NotNull ICapabilityProvider capabilityProvider){
		return capabilityProvider.getCapability(PLAYER_MOVEMENT).orElse(null);
	}

	public static class Serializable extends PlayerMovementProvider implements ICapabilitySerializable<CompoundTag>{
		@NotNull private final Serde serde;

		private Serializable(@NotNull PlayerMovement movement){
			super(movement);
			if(movement instanceof Serde s) this.serde = s;
			else throw new IllegalArgumentException("PlayerMovement does not implement Serde");
		}

		@Override public CompoundTag serializeNBT(){
			return serde.write();
		}
		@Override public void deserializeNBT(CompoundTag tag){
			serde.read(tag);
		}
	}
}
