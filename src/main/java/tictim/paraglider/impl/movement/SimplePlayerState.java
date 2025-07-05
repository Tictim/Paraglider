package tictim.paraglider.impl.movement;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;
import tictim.paraglider.api.movement.PlayerState;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public record SimplePlayerState(
		@NotNull ResourceLocation id,
		@NotNull @Unmodifiable Set<@NotNull ResourceLocation> flags,
		double staminaDelta,
		int recoveryDelay
) implements PlayerState {
	public static @NotNull SimplePlayerState read(@NotNull FriendlyByteBuf buffer) {
		ResourceLocation id = buffer.readResourceLocation();
		List<ResourceLocation> flags = new ArrayList<>();
		for (int i = 0, count = buffer.readVarInt(); i < count; i++) {
			flags.add(buffer.readResourceLocation());
		}
		double staminaDelta = buffer.readDouble();
		int recoveryDelay = buffer.readVarInt();
		return new SimplePlayerState(id, Set.of(flags.toArray(new ResourceLocation[0])), staminaDelta, recoveryDelay);
	}

	public SimplePlayerState(@NotNull PlayerState originalState, double staminaDelta, int recoveryDelay) {
		this(originalState.id(), originalState.flags(), staminaDelta, recoveryDelay);
	}

	public static void write(@NotNull FriendlyByteBuf buffer, @NotNull PlayerState state) {
		buffer.writeResourceLocation(state.id());
		var flags = state.flags();
		buffer.writeVarInt(flags.size());
		for (ResourceLocation flag : flags) {
			buffer.writeResourceLocation(flag);
		}
		buffer.writeDouble(state.staminaDelta());
		buffer.writeVarInt(state.recoveryDelay());
	}
}
