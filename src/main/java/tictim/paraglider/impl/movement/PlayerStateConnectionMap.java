package tictim.paraglider.impl.movement;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import tictim.paraglider.api.movement.ParagliderPlayerStates;
import tictim.paraglider.api.movement.PlayerState;
import tictim.paraglider.api.movement.PlayerStateCondition;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class PlayerStateConnectionMap {
	private final Map<ResourceLocation, ConnectionList> connections;
	private final Object2IntMap<ResourceLocation> stateEvalIndices = new Object2IntOpenHashMap<>();

	public PlayerStateConnectionMap(@NotNull Map<@NotNull ResourceLocation, @NotNull ConnectionList> connections) {
		this.connections = connections;
	}

	public @NotNull @Unmodifiable Map<@NotNull ResourceLocation, @NotNull ConnectionList> connections() {
		return Collections.unmodifiableMap(this.connections);
	}

	public @NotNull PlayerState evaluate(@NotNull PlayerStateMap stateMap,
	                                     @NotNull PlayerStateCondition.Context context) {
		ResourceLocation currentState = ParagliderPlayerStates.IDLE;
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
			@NotNull @Unmodifiable List<@NotNull Connection> connections,
			@Nullable ResourceLocation fallback
	) {
		public ConnectionList(@NotNull @Unmodifiable List<@NotNull Connection> connections, @Nullable ResourceLocation fallback) {
			this.connections = List.copyOf(Objects.requireNonNull(connections, "connections == null"));
			for (Connection connection : this.connections) Objects.requireNonNull(connection);
			this.fallback = fallback;
		}
	}

	public record Connection(
			@NotNull PlayerStateCondition condition,
			@NotNull ResourceLocation state
	) {}
}
