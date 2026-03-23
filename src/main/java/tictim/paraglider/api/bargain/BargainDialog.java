package tictim.paraglider.api.bargain;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import org.jspecify.annotations.NullMarked;

import java.util.*;

@NullMarked
public record BargainDialog(
		@Unmodifiable List<Dialog> initialDialog,
		@Nullable Dialog initialDialogFallback,
		@Unmodifiable List<Dialog> successDialog,
		@Nullable Dialog successDialogFallback,
		@Unmodifiable List<Dialog> failDialog,
		@Nullable Dialog failDialogFallback
) {
	public static final BargainDialog EMPTY = new BargainDialog(List.of(), null, List.of(), null, List.of(), null);

	public static final Codec<BargainDialog> CODEC = RecordCodecBuilder.create(b -> b.group(
			Dialog.CODEC.listOf().fieldOf("initial").forGetter(BargainDialog::initialDialog),
			Dialog.CODEC.optionalFieldOf("initial_fallback").forGetter(d -> Optional.ofNullable(d.initialDialogFallback)),
			Dialog.CODEC.listOf().fieldOf("success").forGetter(BargainDialog::successDialog),
			Dialog.CODEC.optionalFieldOf("success_fallback").forGetter(d -> Optional.ofNullable(d.successDialogFallback)),
			Dialog.CODEC.listOf().fieldOf("fail").forGetter(BargainDialog::failDialog),
			Dialog.CODEC.optionalFieldOf("fail_fallback").forGetter(d -> Optional.ofNullable(d.failDialogFallback))
	).apply(b, (initialDialog, initialDialogFallback,
	            successDialog, successDialogFallback,
	            failDialog, failDialogFallback) -> new BargainDialog(
			initialDialog, initialDialogFallback.orElse(null),
			successDialog, successDialogFallback.orElse(null),
			failDialog, failDialogFallback.orElse(null))));

	public BargainDialog(List<Dialog> initialDialog, @Nullable Dialog initialDialogFallback,
	                     List<Dialog> successDialog, @Nullable Dialog successDialogFallback,
	                     List<Dialog> failDialog, @Nullable Dialog failDialogFallback) {
		this.initialDialog = initialDialog.stream().filter(dialog -> dialog.weight > 0).toList();
		this.initialDialogFallback = initialDialogFallback;
		this.successDialog = successDialog.stream().filter(dialog -> dialog.weight > 0).toList();
		this.successDialogFallback = successDialogFallback;
		this.failDialog = failDialog.stream().filter(dialog -> dialog.weight > 0).toList();
		this.failDialogFallback = failDialogFallback;
	}

	public @Nullable Component randomInitialDialog(Random random) {
		return randomDialog(this.initialDialog, this.initialDialogFallback, random, null, null);
	}

	public @Nullable Component randomSuccessDialog(Random random, Set<String> tags) {
		return randomDialog(this.successDialog, this.successDialogFallback, random, Objects.requireNonNull(tags), null);
	}

	public @Nullable Component randomFailDialog(Random random, Set<String> tags, Set<String> failReasons) {
		return randomDialog(this.failDialog, this.failDialogFallback, random, Objects.requireNonNull(tags), Objects.requireNonNull(failReasons));
	}

	private @Nullable Component randomDialog(List<Dialog> dialogs,
	                                         @Nullable Dialog fallback,
	                                         Random random,
	                                         @Nullable Set<String> tags,
	                                         @Nullable Set<String> failReasons) {
		long weightSum = 0;
		IntList indices = new IntArrayList();
		for (int i = 0; i < dialogs.size(); i++) {
			Dialog dialog = dialogs.get(i);
			if (tags != null && dialog.tagFilter != null && !tags.containsAll(dialog.tagFilter)) {
				continue;
			}
			if (failReasons != null && dialog.failReasonFilter != null && !failReasons.containsAll(dialog.failReasonFilter)) {
				continue;
			}
			indices.add(i);
			weightSum += dialog.weight;
		}
		if (weightSum > 0) {
			long weight = random.nextLong(weightSum);
			for (int i = 0; i < indices.size(); i++) {
				Dialog dialog = dialogs.get(indices.getInt(i));
				weight -= dialog.weight;
				if (weight < 0) return dialog.text;
			}
		}
		return fallback == null ? null : fallback.text;
	}

	public record Dialog(
			Component text,
			int weight,
			@Nullable @Unmodifiable Set<String> tagFilter,
			@Nullable @Unmodifiable Set<String> failReasonFilter
	) {
		public static final Codec<Dialog> CODEC = RecordCodecBuilder.create(b -> b.group(
				ComponentSerialization.CODEC.fieldOf("dialog").forGetter(d -> d.text),
				Codec.INT.optionalFieldOf("weight", 1).forGetter(d -> d.weight),
				Codec.STRING.listOf().optionalFieldOf("tag", List.of())
						.forGetter(d -> d.tagFilter == null ? List.of() : List.copyOf(d.tagFilter)),
				Codec.STRING.listOf().optionalFieldOf("reason", List.of())
						.forGetter(d -> d.failReasonFilter == null ? List.of() : List.copyOf(d.failReasonFilter))
		).apply(b, (text, weight, tagFilter, reasonFilter) -> new Dialog(text, weight, Set.copyOf(tagFilter), Set.copyOf(reasonFilter))));

		// utility methods below

		public static Dialog create(String translateKey) {
			return create(translateKey, 1);
		}

		public static Dialog create(String translateKey, int weight) {
			return new Dialog(Component.translatable(Objects.requireNonNull(translateKey)), weight, null, null);
		}

		public static Dialog createForTag(String translateKey, String... tags) {
			return createForTag(translateKey, 1, tags);
		}

		public static Dialog createForTag(String translateKey, int weight, String... tags) {
			return new Dialog(Component.translatable(Objects.requireNonNull(translateKey)), weight,
					tags.length == 0 ? null : Set.of(tags), null);
		}

		public static Dialog createForFailReason(String translateKey, String... failReasons) {
			return createForFailReason(translateKey, 1, failReasons);
		}

		public static Dialog createForFailReason(String translateKey, int weight, String... failReasons) {
			return new Dialog(Component.translatable(Objects.requireNonNull(translateKey)), weight,
					null, failReasons.length == 0 ? null : Set.of(failReasons));
		}
	}
}
