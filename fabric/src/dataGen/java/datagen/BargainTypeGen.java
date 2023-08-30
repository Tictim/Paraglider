package datagen;

import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import tictim.paraglider.api.bargain.BargainDialog;
import tictim.paraglider.api.bargain.BargainType;
import tictim.paraglider.api.bargain.ParagliderBargainTypes;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

import static tictim.paraglider.api.ParagliderAPI.MODID;
import static tictim.paraglider.api.bargain.ParagliderBargainTags.*;
import static tictim.paraglider.api.bargain.ParagliderFailReasons.*;

public class BargainTypeGen implements DataProvider{
	protected final FabricDataOutput output;
	private final PackOutput.PathProvider pathResolver;

	public BargainTypeGen(FabricDataOutput output){
		this.output = output;
		this.pathResolver = output.createPathProvider(PackOutput.Target.DATA_PACK, MODID+"/bargain_types");
	}

	@Override @NotNull public String getName(){
		return "Bargain Types";
	}

	protected void generateBargainTypes(@NotNull BiConsumer<@NotNull ResourceLocation, @NotNull BargainType> consumer){
		consumer.accept(ParagliderBargainTypes.GODDESS_STATUE, new BargainType(new BargainDialog(
				List.of(
						BargainDialog.Dialog.create("bargain.dialog.goddess_statue.initial.0"),
						BargainDialog.Dialog.create("bargain.dialog.goddess_statue.initial.1")
				),
				null,
				List.of(
						BargainDialog.Dialog.create("bargain.dialog.goddess_statue.success.0"),
						BargainDialog.Dialog.createForTag("bargain.dialog.goddess_statue.success.heart.0", GIVES_HEART_CONTAINER),
						BargainDialog.Dialog.createForTag("bargain.dialog.goddess_statue.success.heart.1", GIVES_HEART_CONTAINER),
						BargainDialog.Dialog.createForTag("bargain.dialog.goddess_statue.success.stamina.0", GIVES_STAMINA_VESSEL),
						BargainDialog.Dialog.createForTag("bargain.dialog.goddess_statue.success.stamina.1", GIVES_STAMINA_VESSEL)
				),
				null,
				List.of(
						BargainDialog.Dialog.createForFailReason("bargain.dialog.goddess_statue.failure.not_enough_items.0", NOT_ENOUGH_ITEMS),
						BargainDialog.Dialog.createForFailReason("bargain.dialog.goddess_statue.failure.not_enough_items.1", NOT_ENOUGH_ITEMS),
						BargainDialog.Dialog.createForFailReason("bargain.dialog.goddess_statue.failure.not_enough_items.2", NOT_ENOUGH_ITEMS),
						BargainDialog.Dialog.createForFailReason("bargain.dialog.goddess_statue.failure.full.0", HEART_FULL),
						BargainDialog.Dialog.createForFailReason("bargain.dialog.goddess_statue.failure.heart_full.0", HEART_FULL),
						BargainDialog.Dialog.createForFailReason("bargain.dialog.goddess_statue.failure.heart_full.1", HEART_FULL),
						BargainDialog.Dialog.createForFailReason("bargain.dialog.goddess_statue.failure.full.0", STAMINA_FULL),
						BargainDialog.Dialog.createForFailReason("bargain.dialog.goddess_statue.failure.stamina_full.0", STAMINA_FULL),
						BargainDialog.Dialog.createForFailReason("bargain.dialog.goddess_statue.failure.stamina_full.1", STAMINA_FULL)
				),
				BargainDialog.Dialog.create("bargain.dialog.goddess_statue.failure.fallback.0")
		)));

		consumer.accept(ParagliderBargainTypes.HORNED_STATUE, new BargainType(new BargainDialog(
				List.of(
						BargainDialog.Dialog.create("bargain.dialog.horned_statue.initial.0"),
						BargainDialog.Dialog.create("bargain.dialog.horned_statue.initial.1")
				),
				null,
				List.of(
						BargainDialog.Dialog.create("bargain.dialog.horned_statue.success.0"),
						BargainDialog.Dialog.create("bargain.dialog.horned_statue.success.1"),
						BargainDialog.Dialog.createForTag("bargain.dialog.horned_statue.success.consumes_heart.0", CONSUMES_HEART_CONTAINER),
						BargainDialog.Dialog.createForTag("bargain.dialog.horned_statue.success.consumes_heart.1", CONSUMES_HEART_CONTAINER),
						BargainDialog.Dialog.createForTag("bargain.dialog.horned_statue.success.consumes_stamina.0", CONSUMES_STAMINA_VESSEL),
						BargainDialog.Dialog.createForTag("bargain.dialog.horned_statue.success.consumes_stamina.1", CONSUMES_STAMINA_VESSEL)
				),
				null,
				List.of(
						BargainDialog.Dialog.createForFailReason("bargain.dialog.horned_statue.failure.not_enough_items.0", NOT_ENOUGH_ITEMS),
						BargainDialog.Dialog.createForFailReason("bargain.dialog.horned_statue.failure.not_enough_heart.0", NOT_ENOUGH_HEARTS),
						BargainDialog.Dialog.createForFailReason("bargain.dialog.horned_statue.failure.not_enough_heart.1", NOT_ENOUGH_HEARTS),
						BargainDialog.Dialog.createForFailReason("bargain.dialog.horned_statue.failure.not_enough_stamina.0", NOT_ENOUGH_STAMINA),
						BargainDialog.Dialog.createForFailReason("bargain.dialog.horned_statue.failure.not_enough_stamina.1", NOT_ENOUGH_STAMINA),
						BargainDialog.Dialog.createForFailReason("bargain.dialog.horned_statue.failure.not_enough_essence.0", NOT_ENOUGH_ESSENCES),
						BargainDialog.Dialog.createForFailReason("bargain.dialog.horned_statue.failure.not_enough_essence.1", NOT_ENOUGH_ESSENCES),
						BargainDialog.Dialog.createForFailReason("bargain.dialog.horned_statue.failure.heart_full.0", HEART_FULL),
						BargainDialog.Dialog.createForFailReason("bargain.dialog.horned_statue.failure.stamina_full.0", STAMINA_FULL),
						BargainDialog.Dialog.createForFailReason("bargain.dialog.horned_statue.failure.essence_full.0", ESSENCE_FULL)
				),
				BargainDialog.Dialog.create("bargain.dialog.horned_statue.failure.fallback.0")
		)));
	}

	@Override @NotNull public CompletableFuture<?> run(CachedOutput cachedOutput){
		Map<ResourceLocation, BargainType> bargainTypes = new Object2ObjectOpenHashMap<>();

		generateBargainTypes((id, bargainType) -> {
			Objects.requireNonNull(id);
			Objects.requireNonNull(bargainType);
			if(bargainTypes.putIfAbsent(id, bargainType)!=null)
				throw new IllegalStateException("Duplicated bargain type "+id);
		});

		final List<CompletableFuture<?>> futures = new ArrayList<>();

		for(var e : bargainTypes.entrySet()){
			JsonElement json = BargainType.CODEC.encodeStart(JsonOps.INSTANCE, e.getValue()).getOrThrow(false, s -> {});
			ResourceLocation id = e.getKey();
			futures.add(DataProvider.saveStable(cachedOutput, json, pathResolver.json(id)));
		}

		return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
	}
}
