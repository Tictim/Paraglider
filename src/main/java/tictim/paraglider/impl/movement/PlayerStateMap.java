package tictim.paraglider.impl.movement;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import tictim.paraglider.ParagliderMod;
import tictim.paraglider.api.movement.ParagliderPlayerStates;
import tictim.paraglider.api.movement.PlayerState;

import java.util.*;

public final class PlayerStateMap {
	public static final StreamCodec<FriendlyByteBuf, PlayerStateMap> STREAM_CODEC =
			StreamCodec.of((buf, psm) -> psm.write(buf), PlayerStateMap::read);

	private final Map<Identifier, PlayerState> states;

	public PlayerStateMap(@NotNull Map<@NotNull Identifier, @NotNull PlayerState> states) {
		this.states = Objects.requireNonNull(states);
	}

	public static @NotNull PlayerStateMap read(@NotNull FriendlyByteBuf buffer) {
		Map<Identifier, PlayerState> states = new Object2ObjectOpenHashMap<>();
		for (int i = 0, count = buffer.readVarInt(); i < count; i++) {
			SimplePlayerState state = SimplePlayerState.read(buffer);
			states.put(state.id(), state);
		}
		if (!states.containsKey(ParagliderPlayerStates.IDLE)) {
			ParagliderMod.LOGGER.error("Instance of PlayerStateMap constructed from packet does not have idle state, something is wrong!");
			states.put(ParagliderPlayerStates.IDLE, new SimplePlayerState(
					ParagliderPlayerStates.IDLE,
					Set.of(),
					ParagliderPlayerStates.IDLE_STAMINA_DELTA,
					0)); // just soft fail and insert the default value silently
		}
		return new PlayerStateMap(states);
	}

	public @NotNull @Unmodifiable Map<@NotNull Identifier, @NotNull PlayerState> states() {
		return Collections.unmodifiableMap(states);
	}

	public @NotNull PlayerState expectState(@NotNull Identifier id) {
		PlayerState state = getState(id);
		if (state == null) throw new NoSuchElementException("No state named " + id + " in state map");
		return state;
	}

	public @Nullable PlayerState getState(@NotNull Identifier id) {
		return this.states.get(id);
	}

	public @NotNull PlayerState getIdleState() {
		return expectState(ParagliderPlayerStates.IDLE);
	}

	public void write(@NotNull FriendlyByteBuf buffer) {
		buffer.writeVarInt(this.states.size());
		for (PlayerState state : this.states.values()) {
			SimplePlayerState.write(buffer, state);
		}
	}

	@Override public boolean equals(Object obj) {
		if (obj == this) return true;
		if (!(obj instanceof PlayerStateMap stateMap)) return false;
		return this.states.equals(stateMap.states);
	}

	@Override public int hashCode() {
		return Objects.hash(this.states);
	}

	@Override public String toString() {
		return "PlayerStateMap{" +
				"states=" + states +
				'}';
	}

	public static boolean isSame(PlayerStateMap stateMap1, PlayerStateMap stateMap2) {
		if (stateMap1.states.size() != stateMap2.states.size()) return false;

		for (Identifier key : stateMap1.states.keySet()) {
			PlayerState s1 = stateMap1.states.get(key);
			PlayerState s2 = stateMap2.states.get(key);

			if (!s1.equals(s2)) return false;
		}

		return true;
	}
}
