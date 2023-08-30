package tictim.paraglider.impl.movement;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import tictim.paraglider.ParagliderMod;
import tictim.paraglider.api.movement.ParagliderPlayerStates;
import tictim.paraglider.api.movement.PlayerState;

import java.util.Collections;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;

public final class PlayerStateMap{
	private final Map<ResourceLocation, PlayerState> states;

	public PlayerStateMap(@NotNull Map<@NotNull ResourceLocation, @NotNull PlayerState> states){
		this.states = Objects.requireNonNull(states);
	}

	@NotNull public static PlayerStateMap read(@NotNull FriendlyByteBuf buffer){
		Map<ResourceLocation, PlayerState> states = new Object2ObjectOpenHashMap<>();
		for(int i = 0, count = buffer.readVarInt(); i<count; i++){
			SimplePlayerState state = SimplePlayerState.read(buffer);
			states.put(state.id(), state);
		}
		if(!states.containsKey(ParagliderPlayerStates.IDLE)){
			ParagliderMod.LOGGER.error("Instance of PlayerStateMap constructed from packet does not have idle state, something is wrong!");
			states.put(ParagliderPlayerStates.IDLE, new SimplePlayerState(
					ParagliderPlayerStates.IDLE,
					Set.of(),
					ParagliderPlayerStates.IDLE_STAMINA_DELTA,
					0)); // just soft fail and insert the default value silently
		}
		return new PlayerStateMap(states);
	}

	@NotNull @Unmodifiable public Map<@NotNull ResourceLocation, @NotNull PlayerState> states(){
		return Collections.unmodifiableMap(states);
	}

	@NotNull public PlayerState expectState(@NotNull ResourceLocation id){
		PlayerState state = getState(id);
		if(state==null) throw new NoSuchElementException("No state named "+id+" in state map");
		return state;
	}

	@Nullable public PlayerState getState(@NotNull ResourceLocation id){
		return this.states.get(id);
	}

	@NotNull public PlayerState getIdleState(){
		return expectState(ParagliderPlayerStates.IDLE);
	}

	@Nullable private Boolean hasStaminaConsumptionCache = null;

	/**
	 * @return Whether this state map has any stamina-consuming state
	 */
	public boolean hasStaminaConsumption(){
		if(this.hasStaminaConsumptionCache!=null) return this.hasStaminaConsumptionCache;
		else return this.hasStaminaConsumptionCache = this.states.values().stream().anyMatch(s -> s.staminaDelta()<0);
	}

	public void write(@NotNull FriendlyByteBuf buffer){
		buffer.writeVarInt(states.size());
		for(PlayerState state : states.values()){
			SimplePlayerState.write(buffer, state);
		}
	}

	@Override
	public boolean equals(Object obj){
		if(obj==this) return true;
		if(!(obj instanceof PlayerStateMap stateMap)) return false;
		return this.states.equals(stateMap.states);
	}

	@Override
	public int hashCode(){
		return Objects.hash(states);
	}

	@Override public String toString(){
		return "PlayerStateMap{"+
				"states="+states+
				'}';
	}
}
