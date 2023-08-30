package tictim.paraglider.impl.movement;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tictim.paraglider.api.movement.ParagliderPlayerStates;
import tictim.paraglider.api.movement.PlayerState;
import tictim.paraglider.api.movement.PlayerStateCondition;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class PlayerStateConnectionMap{
	private final Map<ResourceLocation, ConnectionList> connections;

	public PlayerStateConnectionMap(@NotNull Map<@NotNull ResourceLocation, @NotNull ConnectionList> connections){
		this.connections = connections;
	}

	@NotNull public PlayerState evaluate(@NotNull PlayerStateMap stateMap,
	                                     @NotNull Player player,
	                                     @NotNull PlayerState prevState,
	                                     boolean canDoParagliding,
	                                     double accumulatedFallDistance){
		Object2IntMap<ResourceLocation> states = new Object2IntOpenHashMap<>();
		ResourceLocation currentState = ParagliderPlayerStates.IDLE;
		@Nullable ConnectionList currentConnections = connections.get(currentState);
		int currentIndex = 0;

		LOOP:
		while(true){
			if(currentConnections==null) return stateMap.expectState(currentState);
			while(currentIndex<currentConnections.branches.size()){
				Branch c = currentConnections.branches.get(currentIndex++);
				if(c.condition().test(player, prevState, canDoParagliding, accumulatedFallDistance)){
					states.put(currentState, currentIndex);
					currentState = c.state();
					currentConnections = connections.get(currentState);
					currentIndex = states.getInt(currentState);
					continue LOOP;
				}
			}

			if(currentConnections.fallback==null) return stateMap.expectState(currentState);

			states.put(currentState, currentIndex);
			currentState = currentConnections.fallback;
			currentConnections = connections.get(currentState);
			currentIndex = states.getInt(currentState);
		}
	}

	@Override public String toString(){
		return "PlayerStateConnectionMap{"+
				", connections="+connections+
				'}';
	}

	public record ConnectionList(
			@NotNull List<@NotNull Branch> branches,
			@Nullable ResourceLocation fallback
	){
		public ConnectionList(@NotNull List<@NotNull Branch> branches, @Nullable ResourceLocation fallback){
			this.branches = List.copyOf(Objects.requireNonNull(branches, "branches == null"));
			for(Branch branch : this.branches) Objects.requireNonNull(branch);
			this.fallback = fallback;
		}
	}

	public record Branch(
			@NotNull PlayerStateCondition condition,
			@NotNull ResourceLocation state
	){}
}
