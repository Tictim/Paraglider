package tictim.paraglider.impl.movement;

import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.*;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.*;
import tictim.paraglider.ParagliderMod;
import tictim.paraglider.api.movement.MovementPlugin;
import tictim.paraglider.api.movement.MovementPlugin.PlayerStateConnectionRegister;
import tictim.paraglider.api.movement.MovementPlugin.PlayerStateModifier;
import tictim.paraglider.api.movement.MovementPlugin.PlayerStateRegister;
import tictim.paraglider.api.movement.MovementPluginAction.ChangeDefaultStaminaDelta;
import tictim.paraglider.api.movement.MovementPluginAction.NewState;
import tictim.paraglider.api.movement.MovementPluginAction.SetFallbackConnection;
import tictim.paraglider.api.movement.ParagliderPlayerStates;
import tictim.paraglider.api.movement.PlayerState;
import tictim.paraglider.api.movement.PlayerStateCondition;
import tictim.paraglider.api.plugin.PluginAction;
import tictim.paraglider.api.plugin.PluginInstance;
import tictim.paraglider.impl.movement.PlayerStateConnectionMap.Connection;
import tictim.paraglider.plugin.ParagliderPluginLoader;

import java.util.*;
import java.util.stream.Collectors;

import static tictim.paraglider.plugin.ParagliderPluginUtils.*;

public final class PlayerStateMapLoader {
	private PlayerStateMapLoader() {}

	public static @NotNull Pair<PlayerStateMap, PlayerStateConnectionMap> loadStates() {
		return loadStates(ParagliderPluginLoader.get().getMovementPlugins(), true);
	}
	public static @NotNull Pair<PlayerStateMap, PlayerStateConnectionMap> loadStates(
			@NotNull List<@NotNull PluginInstance<MovementPlugin>> plugins,
			boolean insertIdleIfMissing
	) {
		Map<ResourceLocation, State> states = gatherStates(plugins);

		if (insertIdleIfMissing && !states.containsKey(ParagliderPlayerStates.IDLE)) {
			ParagliderMod.LOGGER.error("None of the plugins registered state {}, this is a bug!", ParagliderPlayerStates.IDLE);
			State idle = new State(ParagliderPlayerStates.IDLE, false);
			idle.defaultStaminaDelta = ParagliderPlayerStates.IDLE_STAMINA_DELTA;
			states.put(ParagliderPlayerStates.IDLE, idle);
		}

		gatherStateModifications(plugins, states);
		gatherStateConnections(plugins, states);

		Set<String> issues = new ObjectOpenHashSet<>();
		// validate time
		for (State state : states.values()) {
			if (state.synthetic && state.fallbackConnection == null) {
				issues.add("Fallback connection of the state " + state.id + " should be set");
			}
			List<State> circularLoop = state.checkCircularLoop(states);
			if (circularLoop != null) {
				issues.add("Circular loop on fallback connections detected: " + circularLoop.stream()
						.map(s -> s.id.toString())
						.collect(Collectors.joining(" -> ")));
			}
		}

		if (!issues.isEmpty()) {
			throw new RuntimeException("Cannot continue loading paraglider plugins due to " + (
					issues.size() == 1 ? "an issue" : issues.size() + " issues"
			) + ":\n  " + String.join("\n  ", issues));
		}

		return Pair.of(
				new PlayerStateMap(states.values().stream()
						.collect(Collectors.toMap(
								s -> s.id,
								s -> new SimplePlayerState(s.id, s.flags, s.defaultStaminaDelta,
										s.defaultStaminaDelta < 0 ? ParagliderPlayerStates.RECOVERY_DELAY : 0),
								(s1, s2) -> s1,
								Object2ObjectOpenHashMap::new
						))),
				new PlayerStateConnectionMap(states.values().stream()
						.filter(s -> !s.connections.isEmpty() || s.fallbackConnection != null)
						.collect(Collectors.toMap(
								s -> s.id,
								s -> new PlayerStateConnectionMap.ConnectionList(s.connections, s.fallbackConnection),
								(s1, s2) -> s1,
								Object2ObjectOpenHashMap::new
						))));
	}

	private static @NotNull Map<ResourceLocation, State> gatherStates(List<PluginInstance<MovementPlugin>> plugins) {
		List<PluginAction<MovementPlugin, NewState>> newStates = new ArrayList<>();
		Object2IntMap<ResourceLocation> idToCountMap = new Object2IntArrayMap<>();

		for (PluginInstance<MovementPlugin> plugin : plugins) {
			plugin.instance().registerNewStates(new PlayerStateRegister() {
				@Override public void register(@NotNull ResourceLocation id, double defaultStaminaDelta, @NotNull ResourceLocation @NotNull ... flags) {
					Objects.requireNonNull(id, "id == null");
					Objects.requireNonNull(flags, "flags == null");
					if (Double.isNaN(defaultStaminaDelta))
						throw new IllegalArgumentException("defaultStaminaDelta is NaN");

					for (ResourceLocation flag : flags) Objects.requireNonNull(flag);
					idToCountMap.put(id, idToCountMap.getInt(id) + 1);
					newStates.add(new PluginAction<>(plugin, new NewState.Regular(id, defaultStaminaDelta, Set.of(flags))));
				}

				@Override public void registerSyntheticState(@NotNull ResourceLocation id) {
					Objects.requireNonNull(id, "id == null");
					idToCountMap.put(id, idToCountMap.getInt(id) + 1);
					newStates.add(new PluginAction<>(plugin, new NewState.Synthetic(id)));
				}
			});
		}

		for (var e : idToCountMap.object2IntEntrySet()) {
			if (e.getIntValue() < 2) continue;
			var list = removeAll(newStates, pa -> pa.action().id().equals(e.getKey()));
			var resolved = resolve(
					MovementPlugin::getMovementPluginConflictResolver,
					list);
			if (resolved == null || resolved.size() >= 2) throw composePluginLoadingError(list);
			if (resolved.size() == 1) newStates.add(resolved.get(0));
		}

		Map<ResourceLocation, State> states = new Object2ObjectOpenHashMap<>();
		for (var pa : newStates) {
			states.put(pa.action().id(), new State(pa.action()));
		}
		return states;
	}

	private static void gatherStateModifications(List<PluginInstance<MovementPlugin>> plugins, Map<ResourceLocation, State> states) {
		List<PluginAction<MovementPlugin, ChangeDefaultStaminaDelta>> staminaDeltaChanges = new ArrayList<>();
		Object2IntMap<ResourceLocation> idToCountMap = new Object2IntArrayMap<>();

		Map<ResourceLocation, Set<ResourceLocation>> flagAdditions = new Object2ObjectOpenHashMap<>();
		Map<ResourceLocation, Set<ResourceLocation>> flagRemovals = new Object2ObjectOpenHashMap<>();

		for (PluginInstance<MovementPlugin> plugin : plugins) {
			plugin.instance().modifyRegisteredStates(new PlayerStateModifier() {
				@Override public @NotNull @UnmodifiableView Map<@NotNull ResourceLocation, @NotNull PlayerState> playerStates() {
					return Collections.unmodifiableMap(states);
				}

				@Override public boolean exists(@NotNull ResourceLocation id) {
					return states.containsKey(Objects.requireNonNull(id, "id == null"));
				}

				@Override public void changeDefaultStaminaDelta(@NotNull ResourceLocation id, double defaultStaminaDelta) {
					Objects.requireNonNull(id, "id == null");
					if (Double.isNaN(defaultStaminaDelta))
						throw new IllegalArgumentException("defaultStaminaDelta is NaN");

					State state = states.get(id);
					if (state == null) throw new NoSuchElementException("No state with ID " + id + " exists");
					if (state.synthetic)
						throw new IllegalStateException("Cannot change stamina delta of synthetic state " + id);
					staminaDeltaChanges.add(new PluginAction<>(plugin, new ChangeDefaultStaminaDelta(id, defaultStaminaDelta)));
					idToCountMap.put(id, idToCountMap.getInt(id) + 1);
				}

				@Override public void addFlags(@NotNull ResourceLocation id, @NotNull ResourceLocation @NotNull ... flags) {
					Objects.requireNonNull(id, "id == null");
					Objects.requireNonNull(flags, "flags == null");
					State state = states.get(id);
					if (state == null) throw new NoSuchElementException("No state with ID " + id + " exists");
					if (state.synthetic)
						throw new IllegalStateException("Cannot change flags of synthetic state " + id);
					Set<ResourceLocation> set = flagAdditions.computeIfAbsent(id, $ -> new ObjectOpenHashSet<>());
					for (ResourceLocation flag : flags) set.add(Objects.requireNonNull(flag));
				}

				@Override public void removeFlags(@NotNull ResourceLocation id, @NotNull ResourceLocation @NotNull ... flags) {
					Objects.requireNonNull(id, "id == null");
					Objects.requireNonNull(flags, "flags == null");
					State state = states.get(id);
					if (state == null) throw new NoSuchElementException("No state with ID " + id + " exists");
					if (state.synthetic)
						throw new IllegalStateException("Cannot change flags of synthetic state " + id);
					Set<ResourceLocation> set = flagRemovals.computeIfAbsent(id, $ -> new ObjectOpenHashSet<>());
					for (ResourceLocation flag : flags) set.add(Objects.requireNonNull(flag));
				}
			});
		}

		for (var e : idToCountMap.object2IntEntrySet()) {
			if (e.getIntValue() < 2) continue;
			var list = removeAll(staminaDeltaChanges, pa -> pa.action().id().equals(e.getKey()));

			if (list.isEmpty()) continue;
			double delta = list.get(0).action().defaultStaminaDelta();
			boolean same = true;
			for (int i = 1; i < list.size(); i++) {
				if (list.get(i).action().defaultStaminaDelta() != delta) {
					same = false;
					break;
				}
			}
			if (same) continue; // no conflict

			var resolved = resolve(
					MovementPlugin::getMovementPluginConflictResolver,
					list);
			if (resolved == null) throw composePluginLoadingError(list);
			switch (resolved.size()) {
				case 0 -> {}
				case 1 -> staminaDeltaChanges.add(resolved.get(0));
				default -> {
					delta = list.get(0).action().defaultStaminaDelta();
					same = true;
					for (int i = 1; i < list.size(); i++) {
						if (list.get(i).action().defaultStaminaDelta() != delta) {
							same = false;
							break;
						}
					}
					if (same) staminaDeltaChanges.add(resolved.get(0));
					else throw composePluginLoadingError(list);
				}
			}
		}

		for (var pa : staminaDeltaChanges) {
			states.get(pa.action().id()).defaultStaminaDelta = pa.action().defaultStaminaDelta();
		}

		for (var e : flagRemovals.entrySet()) {
			states.get(e.getKey()).flags.removeAll(e.getValue());
		}
		for (var e : flagAdditions.entrySet()) {
			states.get(e.getKey()).flags.addAll(e.getValue());
		}
	}

	private static void gatherStateConnections(List<PluginInstance<MovementPlugin>> plugins, Map<ResourceLocation, State> states) {
		record Connect(@NotNull PlayerStateCondition condition, @NotNull ResourceLocation state, double priority) {}
		record Disconnect(@NotNull ResourceLocation state, @Nullable Double priority) {}

		Map<ResourceLocation, List<PluginAction<MovementPlugin, SetFallbackConnection>>> fallbacks = new Object2ObjectLinkedOpenHashMap<>();
		Map<ResourceLocation, List<Connect>> connections = new Object2ObjectLinkedOpenHashMap<>();
		Map<ResourceLocation, List<Disconnect>> disconnections = new Object2ObjectLinkedOpenHashMap<>();

		for (PluginInstance<MovementPlugin> plugin : plugins) {
			plugin.instance().registerStateConnections(new PlayerStateConnectionRegister() {
				@Override public @NotNull @Unmodifiable Map<@NotNull ResourceLocation, @NotNull PlayerState> playerStates() {
					return Collections.unmodifiableMap(states);
				}

				@Override public boolean exists(@NotNull ResourceLocation id) {
					return states.containsKey(Objects.requireNonNull(id, "id == null"));
				}

				@Override public void connect(@NotNull ResourceLocation parent,
				                              @NotNull ResourceLocation state,
				                              @NotNull PlayerStateCondition condition,
				                              double priority) {
					Objects.requireNonNull(condition, "condition == null");
					if (!states.containsKey(Objects.requireNonNull(parent, "parent == null")))
						throw new NoSuchElementException("No state with ID " + parent + " exists");
					if (!states.containsKey(Objects.requireNonNull(state, "state == null")))
						throw new NoSuchElementException("No state with ID " + state + " exists");
					if (parent.equals(state)) return; // does nothing
					connections.computeIfAbsent(parent, $ -> new ArrayList<>()).add(new Connect(condition, state, priority));
				}

				@Override public void disconnect(@NotNull ResourceLocation parent, @NotNull ResourceLocation state, @Nullable Double priority) {
					if (!states.containsKey(Objects.requireNonNull(parent, "parent == null")))
						throw new NoSuchElementException("No state with ID " + parent + " exists");
					if (!states.containsKey(Objects.requireNonNull(state, "state == null")))
						throw new NoSuchElementException("No state with ID " + state + " exists");
					if (parent.equals(state)) return; // does nothing
					disconnections.computeIfAbsent(parent, $ -> new ArrayList<>()).add(new Disconnect(state, priority));
				}

				@Override public void setFallback(@NotNull ResourceLocation parent, @Nullable ResourceLocation fallback, double priority) {
					if (!states.containsKey(Objects.requireNonNull(parent, "parent == null")))
						throw new NoSuchElementException("No state with ID " + parent + " exists");
					if (fallback != null && !states.containsKey(fallback))
						throw new NoSuchElementException("No state with ID " + fallback + " exists");

					if (parent.equals(fallback))
						throw new IllegalArgumentException("Trying to set itself as fallback state");

					List<PluginAction<MovementPlugin, SetFallbackConnection>> connections = fallbacks.computeIfAbsent(parent, $ -> new ArrayList<>());
					if (!connections.isEmpty()) {
						double p = connections.get(0).action().priority();
						if (p > priority) return;
						if (p < priority) connections.clear();
					}
					connections.add(new PluginAction<>(plugin, new SetFallbackConnection(parent, fallback, priority)));
				}
			});
		}

		for (var e : fallbacks.entrySet()) {
			List<PluginAction<MovementPlugin, SetFallbackConnection>> list = e.getValue();

			if (list.size() <= 1) continue;

			@Nullable ResourceLocation fallback = list.get(0).action().fallback();
			boolean same = true;
			for (int i = 1; i < list.size(); i++) {
				if (!Objects.equals(list.get(i).action().fallback(), fallback)) {
					same = false;
					break;
				}
			}
			if (same) continue; // no conflict

			var resolved = resolve(
					MovementPlugin::getMovementPluginConflictResolver,
					list);
			if (resolved == null) throw composePluginLoadingError(list);
			switch (resolved.size()) {
				case 0 -> {}
				case 1 -> {
					list.clear();
					list.add(resolved.get(0));
				}
				default -> {
					fallback = list.get(0).action().fallback();
					same = true;
					for (int i = 1; i < list.size(); i++) {
						if (!Objects.equals(list.get(i).action().fallback(), fallback)) {
							same = false;
							break;
						}
					}
					if (same) {
						list.clear();
						list.add(resolved.get(0));
					} else throw composePluginLoadingError(list);
				}
			}
		}

		for (var e : connections.entrySet()) {
			@Nullable List<Disconnect> disconnect = disconnections.get(e.getKey());
			List<Connect> list = e.getValue();
			if (disconnect != null) {
				list.removeIf(b -> {
					for (Disconnect r : disconnect) {
						if (r.state.equals(b.state) && (r.priority == null || r.priority == b.priority)) {
							return true;
						}
					}
					return false;
				});
			}
			list.sort(Comparator.comparingDouble(Connect::priority).reversed());
			State state = states.get(e.getKey());
			for (Connect c : list) state.connections.add(new Connection(c.condition, c.state));
		}

		for (var e : fallbacks.entrySet()) {
			if (!e.getValue().isEmpty())
				states.get(e.getKey()).fallbackConnection = e.getValue().get(0).action().fallback();
		}
	}

	private static final class State implements PlayerState {
		final @NotNull ResourceLocation id;
		double defaultStaminaDelta;
		final @NotNull Set<@NotNull ResourceLocation> flags = new ObjectOpenHashSet<>();
		final boolean synthetic;

		final @NotNull List<@NotNull Connection> connections = new ArrayList<>();
		@Nullable ResourceLocation fallbackConnection;

		State(@NotNull NewState newState) {
			this.id = newState.id();
			if (newState instanceof NewState.Regular regular) {
				this.defaultStaminaDelta = regular.defaultStaminaDelta();
				this.flags.addAll(regular.flags());
				this.synthetic = false;
			} else {
				this.synthetic = true;
			}
		}
		State(@NotNull ResourceLocation id, boolean synthetic) {
			this.id = id;
			this.synthetic = synthetic;
		}

		/**
		 * 0: Not checked yet
		 * 1: Checking rn
		 * 2: Checked (and already cleared)
		 */
		@NotNull PlayerStateMapLoader.State.CheckStatus circularLoopCheckStatus = CheckStatus.UNCHECKED;

		/**
		 * Checks for circular loops duh
		 *
		 * @param states Map of other states
		 * @return List of states visited (if there's a circular loop) or {@code null} if it isn't there
		 */
		@Nullable List<State> checkCircularLoop(@NotNull Map<ResourceLocation, State> states) {
			return switch (this.circularLoopCheckStatus) {
				case UNCHECKED -> {
					if (this.fallbackConnection == null) {
						this.circularLoopCheckStatus = CheckStatus.CHECKED;
						yield null;
					}
					this.circularLoopCheckStatus = CheckStatus.CHECKING;
					List<State> circularLoop = states.get(this.fallbackConnection).checkCircularLoop(states);
					if (circularLoop != null) {
						if (circularLoop.size() <= 1 ||
								circularLoop.get(circularLoop.size() - 1) != circularLoop.get(0)) {
							circularLoop.add(this);
						}
					}
					this.circularLoopCheckStatus = CheckStatus.CHECKED;
					yield circularLoop;
				}
				case CHECKING -> { // a second call to this method while checking = circular loop
					this.circularLoopCheckStatus = CheckStatus.CHECKED;
					ArrayList<State> list = new ArrayList<>();
					list.add(this);
					yield list;
				}
				case CHECKED -> null; // already checked and reported, no need to check it twice
			};
		}

		@Override public @NotNull ResourceLocation id() {
			return this.id;
		}
		@Override public @NotNull @Unmodifiable Set<@NotNull ResourceLocation> flags() {
			return this.flags;
		}
		@Override public double staminaDelta() {
			return this.defaultStaminaDelta;
		}
		@Range(from = 0, to = Integer.MAX_VALUE)
		@Override public int recoveryDelay() {
			return this.defaultStaminaDelta < 0 ? ParagliderPlayerStates.RECOVERY_DELAY : 0;
		}

		enum CheckStatus {
			UNCHECKED,
			CHECKING,
			CHECKED
		}
	}
}
