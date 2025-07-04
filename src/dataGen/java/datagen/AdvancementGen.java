package datagen;

import net.minecraft.advancements.*;
import net.minecraft.advancements.critereon.ImpossibleTrigger;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.data.AdvancementProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import tictim.paraglider.api.ParagliderAPI;
import tictim.paraglider.contents.Contents;
import tictim.paraglider.contents.ParagliderAdvancements;
import tictim.paraglider.contents.ParagliderTags;

import javax.annotation.Nullable;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static net.minecraft.advancements.critereon.InventoryChangeTrigger.TriggerInstance.hasItems;
import static net.minecraft.advancements.critereon.ItemPredicate.Builder.item;
import static tictim.paraglider.api.ParagliderAPI.MODID;

public class AdvancementGen extends AdvancementProvider {
	public AdvancementGen(PackOutput output, CompletableFuture<HolderLookup.Provider> registries, ExistingFileHelper existingFileHelper) {
		super(output, registries, existingFileHelper, List.of((r, s, e) -> {
			Contents contents = Contents.get();
			AdvancementHolder root = advancement(
					new ItemStack(contents.paraglider()),
					"advancement.paraglider",
					ParagliderAPI.id("gui/advancement_background"),
					AdvancementType.TASK,
					false,
					false,
					false)
					.addCriterion("crafting_table", hasItems(Blocks.CRAFTING_TABLE))
					.save(s, MODID + ":root");
			AdvancementHolder paraglider = advancement(
					new ItemStack(contents.paraglider()),
					"advancement.paraglider.paraglider",
					AdvancementType.GOAL,
					true,
					true,
					false)
					.parent(root)
					.addCriterion("paraglider", hasItems(item().of(ParagliderTags.PARAGLIDERS).build()))
					.save(s, MODID + ":paraglider");
			AdvancementHolder prayToTheGoddess = advancement(
					new ItemStack(contents.goddessStatue()),
					"advancement.paraglider.pray_to_the_goddess",
					AdvancementType.GOAL,
					true,
					true,
					false)
					.parent(root)
					.addCriterion("bargain", impossibleCriterion())
					.save(s, ParagliderAdvancements.PRAY_TO_THE_GODDESS.toString());
			AdvancementHolder statuesBargain = advancement(
					new ItemStack(contents.hornedStatue()),
					"advancement.paraglider.statues_bargain",
					AdvancementType.GOAL,
					true,
					true,
					false)
					.parent(root)
					.addCriterion("bargain", impossibleCriterion())
					.save(s, ParagliderAdvancements.STATUES_BARGAIN.toString());
			AdvancementHolder allVessels = advancement(
					new ItemStack(contents.heartContainer()),
					"advancement.paraglider.all_vessels",
					AdvancementType.CHALLENGE,
					true,
					true,
					false)
					.parent(root)
					.addCriterion("code_triggered", impossibleCriterion())
					.save(s, ParagliderAdvancements.ALL_VESSELS.toString());
		}));
	}

	private static Criterion<?> impossibleCriterion() {
		return CriteriaTriggers.IMPOSSIBLE.createCriterion(new ImpossibleTrigger.TriggerInstance());
	}

	private static Advancement.Builder advancement(ItemStack stack,
	                                               String display,
	                                               AdvancementType frameType,
	                                               boolean showToast,
	                                               boolean announceToChat,
	                                               boolean hidden) {
		return advancement(stack, display, null, frameType, showToast, announceToChat, hidden);
	}

	private static Advancement.Builder advancement(ItemStack stack,
	                                               String display,
	                                               @Nullable ResourceLocation background,
	                                               AdvancementType frameType,
	                                               boolean showToast,
	                                               boolean announceToChat,
	                                               boolean hidden) {
		return Advancement.Builder.advancement().display(stack,
				Component.translatable(display),
				Component.translatable(display + ".desc"),
				background,
				frameType,
				showToast,
				announceToChat,
				hidden);
	}
}
