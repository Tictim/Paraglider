package tictim.paraglider.api.bargain;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ExtraCodecs;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.Set;

public record BargainDialog(@NotNull @Unmodifiable List<@NotNull Dialog> initialDialog,
                            @Nullable Dialog initialDialogFallback,
                            @NotNull @Unmodifiable List<@NotNull Dialog> successDialog,
                            @Nullable Dialog successDialogFallback,
                            @NotNull @Unmodifiable List<@NotNull Dialog> failDialog,
                            @Nullable Dialog failDialogFallback){
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

	public BargainDialog(@NotNull List<@NotNull Dialog> initialDialog, @Nullable Dialog initialDialogFallback,
	                     @NotNull List<@NotNull Dialog> successDialog, @Nullable Dialog successDialogFallback,
	                     @NotNull List<@NotNull Dialog> failDialog, @Nullable Dialog failDialogFallback){
		this.initialDialog = initialDialog.stream().filter(dialog -> dialog.weight>0).toList();
		this.initialDialogFallback = initialDialogFallback;
		this.successDialog = successDialog.stream().filter(dialog -> dialog.weight>0).toList();
		this.successDialogFallback = successDialogFallback;
		this.failDialog = failDialog.stream().filter(dialog -> dialog.weight>0).toList();
		this.failDialogFallback = failDialogFallback;
	}

	@Nullable public Component randomInitialDialog(@NotNull Random random){
		return randomDialog(this.initialDialog, this.initialDialogFallback, random, null, null);
	}

	@Nullable public Component randomSuccessDialog(@NotNull Random random, @NotNull Set<String> tags){
		return randomDialog(this.successDialog, this.successDialogFallback, random, Objects.requireNonNull(tags), null);
	}

	@Nullable public Component randomFailDialog(@NotNull Random random, @NotNull Set<String> tags, @NotNull Set<String> failReasons){
		return randomDialog(this.failDialog, this.failDialogFallback, random, Objects.requireNonNull(tags), Objects.requireNonNull(failReasons));
	}

	@Nullable private Component randomDialog(@NotNull List<Dialog> dialogs,
	                                         @Nullable Dialog fallback,
	                                         @NotNull Random random,
	                                         @Nullable Set<String> tags,
	                                         @Nullable Set<String> failReasons){
		long weightSum = 0;
		IntList indices = new IntArrayList();
		for(int i = 0; i<dialogs.size(); i++){
			Dialog dialog = dialogs.get(i);
			if(tags!=null&&dialog.tagFilter!=null&&!tags.containsAll(dialog.tagFilter)){
				continue;
			}
			if(failReasons!=null&&dialog.failReasonFilter!=null&&!failReasons.containsAll(dialog.failReasonFilter)){
				continue;
			}
			indices.add(i);
			weightSum += dialog.weight;
		}
		if(weightSum>0){
			long weight = random.nextLong(weightSum);
			for(int i = 0; i<indices.size(); i++){
				Dialog dialog = dialogs.get(indices.getInt(i));
				weight -= dialog.weight;
				if(weight<0) return dialog.text;
			}
		}
		return fallback==null ? null : fallback.text;
	}

	public record Dialog(
			@NotNull Component text,
			int weight,
			@Nullable @Unmodifiable Set<@NotNull String> tagFilter,
			@Nullable @Unmodifiable Set<@NotNull String> failReasonFilter
	){
		public static final Codec<Dialog> CODEC = RecordCodecBuilder.create(b -> b.group(
				ExtraCodecs.COMPONENT.fieldOf("dialog").forGetter(d -> d.text),
				Codec.INT.optionalFieldOf("weight", 1).forGetter(d -> d.weight),
				Codec.STRING.listOf().optionalFieldOf("tag", List.of())
						.forGetter(d -> d.tagFilter==null ? List.of() : List.copyOf(d.tagFilter)),
				Codec.STRING.listOf().optionalFieldOf("reason", List.of())
						.forGetter(d -> d.failReasonFilter==null ? List.of() : List.copyOf(d.failReasonFilter))
		).apply(b, (text, weight, tagFilter, reasonFilter) -> new Dialog(text, weight, Set.copyOf(tagFilter), Set.copyOf(reasonFilter))));

		// utility methods below

		@NotNull public static Dialog create(@NotNull String translateKey){
			return create(translateKey, 1);
		}
		@NotNull public static Dialog create(@NotNull String translateKey, int weight){
			return new Dialog(Component.translatable(Objects.requireNonNull(translateKey)), weight, null, null);
		}
		@NotNull public static Dialog createForTag(@NotNull String translateKey, @NotNull String @NotNull ... tags){
			return createForTag(translateKey, 1, tags);
		}
		@NotNull public static Dialog createForTag(@NotNull String translateKey, int weight, @NotNull String @NotNull ... tags){
			return new Dialog(Component.translatable(Objects.requireNonNull(translateKey)), weight,
					tags.length==0 ? null : Set.of(tags), null);
		}
		@NotNull public static Dialog createForFailReason(@NotNull String translateKey, @NotNull String @NotNull ... failReasons){
			return createForFailReason(translateKey, 1, failReasons);
		}
		@NotNull public static Dialog createForFailReason(@NotNull String translateKey, int weight, @NotNull String @NotNull ... failReasons){
			return new Dialog(Component.translatable(Objects.requireNonNull(translateKey)), weight,
					null, failReasons.length==0 ? null : Set.of(failReasons));
		}
	}
}
