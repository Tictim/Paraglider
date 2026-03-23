package tictim.paraglider.api.movement;

import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import org.jspecify.annotations.NullMarked;

import java.util.Objects;
import java.util.Set;

@NullMarked
public sealed interface MovementPluginAction {
	sealed interface NewState extends MovementPluginAction {
		Identifier id();

		record Regular(
				Identifier id,
				double defaultStaminaDelta,
				@Unmodifiable Set<Identifier> flags
		) implements NewState {
			public Regular {
				Objects.requireNonNull(id, "id == null");
				Objects.requireNonNull(flags, "flags == null");
				for (Identifier flag : flags) Objects.requireNonNull(flag);
			}
		}

		record Synthetic(Identifier id) implements NewState {
			public Synthetic {
				Objects.requireNonNull(id, "id == null");
			}
		}
	}

	record ChangeDefaultStaminaDelta(
			Identifier id,
			double defaultStaminaDelta
	) implements MovementPluginAction {
		public ChangeDefaultStaminaDelta {
			Objects.requireNonNull(id, "id == null");
		}
	}

	record SetFallbackConnection(
			Identifier parent,
			@Nullable Identifier fallback,
			double priority
	) implements MovementPluginAction {}
}
