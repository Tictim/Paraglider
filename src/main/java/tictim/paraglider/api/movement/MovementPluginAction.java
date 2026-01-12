package tictim.paraglider.api.movement;

import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Objects;
import java.util.Set;

public sealed interface MovementPluginAction {
	sealed interface NewState extends MovementPluginAction {
		@NotNull Identifier id();

		record Regular(
				@NotNull Identifier id,
				double defaultStaminaDelta,
				@NotNull @Unmodifiable Set<@NotNull Identifier> flags
		) implements NewState {
			public Regular {
				Objects.requireNonNull(id, "id == null");
				Objects.requireNonNull(flags, "flags == null");
				for (Identifier flag : flags) Objects.requireNonNull(flag);
			}
		}

		record Synthetic(@NotNull Identifier id) implements NewState {
			public Synthetic {
				Objects.requireNonNull(id, "id == null");
			}
		}
	}

	record ChangeDefaultStaminaDelta(
			@NotNull Identifier id,
			double defaultStaminaDelta
	) implements MovementPluginAction {
		public ChangeDefaultStaminaDelta {
			Objects.requireNonNull(id, "id == null");
		}
	}

	record SetFallbackConnection(
			@NotNull Identifier parent,
			@Nullable Identifier fallback,
			double priority
	) implements MovementPluginAction {}
}
