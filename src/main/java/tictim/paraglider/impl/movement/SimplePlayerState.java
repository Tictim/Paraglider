package tictim.paraglider.impl.movement;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Unmodifiable;
import org.jspecify.annotations.NullMarked;
import tictim.paraglider.api.movement.PlayerState;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@NullMarked
public record SimplePlayerState(
		Identifier id,
		@Unmodifiable Set<Identifier> flags,
		double staminaDelta,
		int recoveryDelay
) implements PlayerState {
	public static SimplePlayerState read(FriendlyByteBuf buffer) {
		Identifier id = buffer.readIdentifier();
		List<Identifier> flags = new ArrayList<>();
		for (int i = 0, count = buffer.readVarInt(); i < count; i++) {
			flags.add(buffer.readIdentifier());
		}
		double staminaDelta = buffer.readDouble();
		int recoveryDelay = buffer.readVarInt();
		return new SimplePlayerState(id, Set.of(flags.toArray(new Identifier[0])), staminaDelta, recoveryDelay);
	}

	public SimplePlayerState(PlayerState originalState, double staminaDelta, int recoveryDelay) {
		this(originalState.id(), originalState.flags(), staminaDelta, recoveryDelay);
	}

	public static void write(FriendlyByteBuf buffer, PlayerState state) {
		buffer.writeIdentifier(state.id());
		var flags = state.flags();
		buffer.writeVarInt(flags.size());
		for (Identifier flag : flags) {
			buffer.writeIdentifier(flag);
		}
		buffer.writeDouble(state.staminaDelta());
		buffer.writeVarInt(state.recoveryDelay());
	}
}
