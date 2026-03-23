package tictim.paraglider.impl.movement;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import org.jspecify.annotations.NullMarked;
import tictim.paraglider.api.movement.ParagliderPlayerStates;
import tictim.paraglider.api.movement.PlayerState;
import tictim.paraglider.api.movement.PlayerStateCondition;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@NullMarked
public final class PlayerStateConnectionMap {
	private final Map<Identifier, ConnectionList> connections;
	private final Object2IntMap<Identifier> stateEvalIndices = new Object2IntOpenHashMap<>();

	public PlayerStateConnectionMap(Map<Identifier, ConnectionList> connections) {
		this.connections = connections;
	}

	public @Unmodifiable Map<Identifier, ConnectionList> connections() {
		return Collections.unmodifiableMap(this.connections);
	}

	public PlayerState evaluate(PlayerStateMap stateMap,
	                            PlayerStateCondition.Context context) {
		Identifier currentState = ParagliderPlayerStates.IDLE;
		@Nullable ConnectionList currentConnections = this.connections.get(currentState);
		int currentIndex = 0;

		LOOP:
		while (true) {
			if (currentConnections == null) break;
			while (currentIndex < currentConnections.connections.size()) {
				Connection c = currentConnections.connections.get(currentIndex++);
				if (c.condition().test(context)) {
					this.stateEvalIndices.put(currentState, currentIndex);
					currentState = c.state();
					currentConnections = this.connections.get(currentState);
					currentIndex = this.stateEvalIndices.getInt(currentState);
					continue LOOP;
				}
			}

			if (currentConnections.fallback == null) break;

			this.stateEvalIndices.put(currentState, currentIndex);
			currentState = currentConnections.fallback;
			currentConnections = this.connections.get(currentState);
			currentIndex = this.stateEvalIndices.getInt(currentState);
		}

		this.stateEvalIndices.clear();
		return stateMap.expectState(currentState);
	}

	@Override public String toString() {
		return "PlayerStateConnectionMap{" +
				", connections=" + connections +
				'}';
	}

	public record ConnectionList(
			@Unmodifiable List<Connection> connections,
			@Nullable Identifier fallback
	) {
		public ConnectionList(@Unmodifiable List<Connection> connections, @Nullable Identifier fallback) {
			this.connections = List.copyOf(Objects.requireNonNull(connections, "connections == null"));
			for (Connection connection : this.connections) Objects.requireNonNull(connection);
			this.fallback = fallback;
		}
	}

	public record Connection(
			PlayerStateCondition condition,
			Identifier state
	) {}
}
