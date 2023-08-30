package tictim.paraglider.impl.movement;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;
import tictim.paraglider.api.movement.PlayerState;

import java.util.Set;

public record SimplePlayerState(
		@NotNull ResourceLocation id,
		@NotNull @Unmodifiable Set<@NotNull ResourceLocation> flags,
		int staminaDelta,
		int recoveryDelay
) implements PlayerState{
	@NotNull public static SimplePlayerState read(@NotNull FriendlyByteBuf buffer){
		ResourceLocation id = buffer.readResourceLocation();
		Set<ResourceLocation> flags = new ObjectOpenHashSet<>();
		for(int i = 0, count = buffer.readVarInt(); i<count; i++){
			flags.add(buffer.readResourceLocation());
		}
		int staminaDelta = buffer.readInt();
		int recoveryDelay = buffer.readVarInt();
		return new SimplePlayerState(id, flags, staminaDelta, recoveryDelay);
	}

	public SimplePlayerState(@NotNull PlayerState originalState, int staminaDelta, int recoveryDelay){
		this(originalState.id(), originalState.flags(), staminaDelta, recoveryDelay);
	}

	public static void write(@NotNull FriendlyByteBuf buffer, @NotNull PlayerState state){
		buffer.writeResourceLocation(state.id());
		var flags = state.flags();
		buffer.writeVarInt(flags.size());
		for(ResourceLocation flag : flags){
			buffer.writeResourceLocation(flag);
		}
		buffer.writeInt(state.staminaDelta());
		buffer.writeVarInt(state.recoveryDelay());
	}

	public void write(@NotNull FriendlyByteBuf buffer){
		write(buffer, this);
	}

	@Override public boolean equals(Object obj){
		return this==obj||obj instanceof PlayerState another&&id.equals(another.id());
	}
	@Override public int hashCode(){
		return id.hashCode();
	}
}
