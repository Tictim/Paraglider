package tictim.paraglider.api.movement;

import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Objects;
import java.util.Set;

public sealed interface MovementPluginAction{
	sealed interface NewState extends MovementPluginAction{
		@NotNull ResourceLocation id();

		record Regular(
				@NotNull ResourceLocation id,
				int defaultStaminaDelta,
				@NotNull @Unmodifiable Set<@NotNull ResourceLocation> flags
		) implements NewState{
			public Regular{
				Objects.requireNonNull(id, "id == null");
				Objects.requireNonNull(flags, "flags == null");
				for(ResourceLocation flag : flags) Objects.requireNonNull(flag);
			}
		}

		record Synthetic(@NotNull ResourceLocation id) implements NewState{
			public Synthetic{
				Objects.requireNonNull(id, "id == null");
			}
		}
	}

	record ChangeDefaultStaminaDelta(
			@NotNull ResourceLocation id,
			int defaultStaminaDelta
	) implements MovementPluginAction{
		public ChangeDefaultStaminaDelta{
			Objects.requireNonNull(id, "id == null");
		}
	}

	record SetFallbackBranch(
			@NotNull ResourceLocation parent,
			@Nullable ResourceLocation fallback,
			double priority
	) implements MovementPluginAction{}
}
