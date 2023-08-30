package tictim.paraglider.config;

import it.unimi.dsi.fastutil.objects.Object2ObjectAVLTreeMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.Util;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.ForgeConfigSpec;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tictim.paraglider.ParagliderMod;
import tictim.paraglider.api.movement.PlayerState;
import tictim.paraglider.impl.movement.PlayerStateMap;
import tictim.paraglider.impl.movement.SimplePlayerState;

import java.util.Map;
import java.util.concurrent.Future;

import static tictim.paraglider.api.ParagliderAPI.MODID;
import static tictim.paraglider.api.movement.ParagliderPlayerStates.Flags.FLAG_PARAGLIDING;
import static tictim.paraglider.api.movement.ParagliderPlayerStates.Flags.FLAG_RUNNING;

public class StateMapConfig{
	protected static final String FILENAME = "paraglider-player-states.toml";

	protected final ForgeConfigSpec spec;

	private final PlayerStateMap originalStateMap;
	private final Map<ResourceLocation, Config> configSpecs;

	@Nullable private PlayerStateMap configuredStateMap;

	private @Nullable MinecraftServer server;
	private @Nullable Callback<PlayerStateMap> onSuccessCallback;
	private @Nullable Callback<RuntimeException> onErrorCallback;

	public StateMapConfig(@NotNull PlayerStateMap originalStateMap){
		this.originalStateMap = originalStateMap;

		// sort player states, always write paragliders state at the top
		Map<String, Map<String, PlayerState>> states = new Object2ObjectAVLTreeMap<>((o1, o2) -> {
			if(o1.equals(o2)) return 0;
			if(MODID.equals(o1)) return -1;
			if(MODID.equals(o2)) return 1;
			return o1.compareTo(o2);
		});

		for(var e : originalStateMap.states().entrySet()){
			states.computeIfAbsent(e.getKey().getNamespace(), s -> new Object2ObjectAVLTreeMap<>())
					.put(e.getKey().getPath(), e.getValue());
		}

		ForgeConfigSpec.Builder b = new ForgeConfigSpec.Builder();
		this.configSpecs = new Object2ObjectOpenHashMap<>();

		for(var e : states.entrySet()){
			if(e.getKey().equals(MODID)){
				b.comment("""
						Configuration file for player states.
						You can adjust stamina delta (negative value means consumption / positive value means gain) and
						recovery delay (in ticks) of all player states registered in the game.
						To reload the config, use the following command: /paraglider reloadPlayerStates
						""");
			}
			b.push(e.getKey());
			for(var e2 : e.getValue().entrySet()){
				b.push(e2.getKey());
				PlayerState state = e2.getValue();

				ForgeConfigSpec.IntValue staminaDelta = b.defineInRange("staminaDelta", state.staminaDelta(), Integer.MIN_VALUE, Integer.MAX_VALUE);
				ForgeConfigSpec.IntValue recoveryDelay = b.defineInRange("recoveryDelay", state.recoveryDelay(), 0, Integer.MAX_VALUE);
				configSpecs.put(state.id(), new Config(staminaDelta, recoveryDelay));

				b.pop();
			}
			b.pop();
		}

		this.spec = b.build();
	}

	@NotNull public PlayerStateMap stateMap(){
		return configuredStateMap!=null ? configuredStateMap : originalStateMap;
	}

	@NotNull public PlayerStateMap originalStateMap(){
		return originalStateMap;
	}
	@Nullable public PlayerStateMap configuredStateMap(){
		return configuredStateMap;
	}

	// since I can't get instance of server in fabric easily like forge (at least in my knowledge), I am binding instance
	// of the server preemptively so reloading configs can Just Work (TM)
	public void bindServer(@NotNull MinecraftServer server,
	                       @Nullable Callback<PlayerStateMap> onSuccessCallback,
	                       @Nullable Callback<RuntimeException> onErrorCallback){
		this.server = server;
		this.onSuccessCallback = onSuccessCallback;
		this.onErrorCallback = onErrorCallback;
	}

	public void unbindServer(){
		this.server = null;
		this.onSuccessCallback = null;
		this.onErrorCallback = null;
	}

	@NotNull public Future<?> scheduleReload(){
		return Util.ioPool().submit(() -> {
			PlayerStateMap prevStateMap = stateMap();
			try{
				reloadInternal();
				dispatchSuccess(true, prevStateMap);
			}catch(RuntimeException ex){
				this.configuredStateMap = null;
				dispatchError(true, ex, prevStateMap);
			}
		});
	}

	private void dispatchSuccess(boolean dispatchOnServer, @NotNull PlayerStateMap prevStateMap){
		MinecraftServer server = this.server;
		if(server==null) return;
		if(server.isStopped()){
			unbindServer();
			return;
		}
		Callback<PlayerStateMap> callback = this.onSuccessCallback;
		if(callback==null) return;
		PlayerStateMap stateMap = stateMap();
		boolean contentUpdated = !prevStateMap.equals(stateMap);
		if(dispatchOnServer){
			server.submit(() -> callback.accept(stateMap, contentUpdated));
		}else{
			callback.accept(stateMap, contentUpdated);
		}
	}

	private void dispatchError(boolean dispatchOnServer, @NotNull RuntimeException exception, @NotNull PlayerStateMap prevStateMap){
		ParagliderMod.LOGGER.error("Cannot load state map due to an error", exception);
		MinecraftServer server = this.server;
		if(server==null) return;
		if(server.isStopped()){
			unbindServer();
			return;
		}
		Callback<RuntimeException> callback = this.onErrorCallback;
		if(callback==null) return;
		boolean contentUpdated = !prevStateMap.equals(stateMap());
		if(dispatchOnServer){
			server.submit(() -> callback.accept(exception, contentUpdated));
		}else{
			callback.accept(exception, contentUpdated);
		}
	}

	public void reload(){
		PlayerStateMap prevStateMap = stateMap();
		try{
			reloadInternal();
			dispatchSuccess(false, prevStateMap);
		}catch(RuntimeException ex){
			this.configuredStateMap = null;
			dispatchError(false, ex, prevStateMap);
		}
	}

	public void reloadInternal(){
		boolean paraglidingConsumesStamina = Cfg.get().paraglidingConsumesStamina();
		boolean runningConsumesStamina = Cfg.get().runningConsumesStamina();

		@Nullable Map<ResourceLocation, PlayerState> newStates = null;
		for(var e : this.originalStateMap.states().entrySet()){
			ResourceLocation id = e.getKey();
			PlayerState state = e.getValue();

			Config config = this.configSpecs.get(id);

			int staminaDelta = config.staminaDelta.get();
			int recoveryDelay = config.recoveryDelay.get();

			if(state.has(FLAG_RUNNING)){
				if(staminaDelta<0&&!runningConsumesStamina) staminaDelta = 0;
			}
			if(state.has(FLAG_PARAGLIDING)){
				if(staminaDelta<0&&!paraglidingConsumesStamina) staminaDelta = 0;
			}

			if(staminaDelta>0&&recoveryDelay>0){
				ParagliderMod.LOGGER.warn("Player state {} has both positive stamina delta ({}) and recovery delay ({}). Setting recovery value to 0.",
						id, staminaDelta, recoveryDelay);
				recoveryDelay = 0;
			}

			if(state.staminaDelta()==staminaDelta&&state.recoveryDelay()==recoveryDelay) continue;

			if(newStates==null) newStates = new Object2ObjectOpenHashMap<>(this.originalStateMap.states());
			newStates.put(id, new SimplePlayerState(state, staminaDelta, recoveryDelay));
		}

		this.configuredStateMap = newStates==null ? null : new PlayerStateMap(newStates);
	}

	@FunctionalInterface
	public interface Callback<T>{
		void accept(@NotNull T t, boolean contentUpdated);
	}

	private record Config(@NotNull ForgeConfigSpec.IntValue staminaDelta,
	                      @NotNull ForgeConfigSpec.IntValue recoveryDelay){}
}
