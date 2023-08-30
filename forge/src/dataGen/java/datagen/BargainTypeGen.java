package datagen;

import com.mojang.serialization.JsonOps;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.common.data.JsonCodecProvider;
import org.jetbrains.annotations.NotNull;
import tictim.paraglider.api.bargain.BargainDialog;
import tictim.paraglider.api.bargain.BargainDialog.Dialog;
import tictim.paraglider.api.bargain.BargainType;
import tictim.paraglider.api.bargain.ParagliderBargainTypes;

import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

import static tictim.paraglider.api.ParagliderAPI.MODID;
import static tictim.paraglider.api.bargain.ParagliderBargainTags.*;
import static tictim.paraglider.api.bargain.ParagliderFailReasons.*;

public final class BargainTypeGen extends JsonCodecProvider<BargainType>{
	public BargainTypeGen(@NotNull PackOutput output, @NotNull ExistingFileHelper existingFileHelper){
		super(output, existingFileHelper, MODID, JsonOps.INSTANCE, PackType.SERVER_DATA, MODID+"/bargain_types", BargainType.CODEC, Map.of());
	}

	@Override protected void gather(@NotNull BiConsumer<ResourceLocation, BargainType> consumer){
		consumer.accept(ParagliderBargainTypes.GODDESS_STATUE, new BargainType(new BargainDialog(
				List.of(
						Dialog.create("bargain.dialog.goddess_statue.initial.0"),
						Dialog.create("bargain.dialog.goddess_statue.initial.1")
				),
				null,
				List.of(
						Dialog.create("bargain.dialog.goddess_statue.success.0"),
						Dialog.createForTag("bargain.dialog.goddess_statue.success.heart.0", GIVES_HEART_CONTAINER),
						Dialog.createForTag("bargain.dialog.goddess_statue.success.heart.1", GIVES_HEART_CONTAINER),
						Dialog.createForTag("bargain.dialog.goddess_statue.success.stamina.0", GIVES_STAMINA_VESSEL),
						Dialog.createForTag("bargain.dialog.goddess_statue.success.stamina.1", GIVES_STAMINA_VESSEL)
				),
				null,
				List.of(
						Dialog.createForFailReason("bargain.dialog.goddess_statue.failure.not_enough_items.0", NOT_ENOUGH_ITEMS),
						Dialog.createForFailReason("bargain.dialog.goddess_statue.failure.not_enough_items.1", NOT_ENOUGH_ITEMS),
						Dialog.createForFailReason("bargain.dialog.goddess_statue.failure.not_enough_items.2", NOT_ENOUGH_ITEMS),
						Dialog.createForFailReason("bargain.dialog.goddess_statue.failure.full.0", HEART_FULL),
						Dialog.createForFailReason("bargain.dialog.goddess_statue.failure.heart_full.0", HEART_FULL),
						Dialog.createForFailReason("bargain.dialog.goddess_statue.failure.heart_full.1", HEART_FULL),
						Dialog.createForFailReason("bargain.dialog.goddess_statue.failure.full.0", STAMINA_FULL),
						Dialog.createForFailReason("bargain.dialog.goddess_statue.failure.stamina_full.0", STAMINA_FULL),
						Dialog.createForFailReason("bargain.dialog.goddess_statue.failure.stamina_full.1", STAMINA_FULL)
				),
				Dialog.create("bargain.dialog.goddess_statue.failure.fallback.0")
		)));

		consumer.accept(ParagliderBargainTypes.HORNED_STATUE, new BargainType(new BargainDialog(
				List.of(
						Dialog.create("bargain.dialog.horned_statue.initial.0"),
						Dialog.create("bargain.dialog.horned_statue.initial.1")
				),
				null,
				List.of(
						Dialog.create("bargain.dialog.horned_statue.success.0"),
						Dialog.create("bargain.dialog.horned_statue.success.1"),
						Dialog.createForTag("bargain.dialog.horned_statue.success.consumes_heart.0", CONSUMES_HEART_CONTAINER),
						Dialog.createForTag("bargain.dialog.horned_statue.success.consumes_heart.1", CONSUMES_HEART_CONTAINER),
						Dialog.createForTag("bargain.dialog.horned_statue.success.consumes_stamina.0", CONSUMES_STAMINA_VESSEL),
						Dialog.createForTag("bargain.dialog.horned_statue.success.consumes_stamina.1", CONSUMES_STAMINA_VESSEL)
				),
				null,
				List.of(
						Dialog.createForFailReason("bargain.dialog.horned_statue.failure.not_enough_items.0", NOT_ENOUGH_ITEMS),
						Dialog.createForFailReason("bargain.dialog.horned_statue.failure.not_enough_heart.0", NOT_ENOUGH_HEARTS),
						Dialog.createForFailReason("bargain.dialog.horned_statue.failure.not_enough_heart.1", NOT_ENOUGH_HEARTS),
						Dialog.createForFailReason("bargain.dialog.horned_statue.failure.not_enough_stamina.0", NOT_ENOUGH_STAMINA),
						Dialog.createForFailReason("bargain.dialog.horned_statue.failure.not_enough_stamina.1", NOT_ENOUGH_STAMINA),
						Dialog.createForFailReason("bargain.dialog.horned_statue.failure.not_enough_essence.0", NOT_ENOUGH_ESSENCES),
						Dialog.createForFailReason("bargain.dialog.horned_statue.failure.not_enough_essence.1", NOT_ENOUGH_ESSENCES),
						Dialog.createForFailReason("bargain.dialog.horned_statue.failure.heart_full.0", HEART_FULL),
						Dialog.createForFailReason("bargain.dialog.horned_statue.failure.stamina_full.0", STAMINA_FULL),
						Dialog.createForFailReason("bargain.dialog.horned_statue.failure.essence_full.0", ESSENCE_FULL)
				),
				Dialog.create("bargain.dialog.horned_statue.failure.fallback.0")
		)));
	}
}
