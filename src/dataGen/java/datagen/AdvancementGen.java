package datagen;

import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementType;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.Criterion;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.advancements.AdvancementProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Blocks;
import tictim.paraglider.api.ParagliderAPI;
import tictim.paraglider.contents.Contents;
import tictim.paraglider.contents.ParagliderAdvancements;
import tictim.paraglider.contents.ParagliderTags;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static net.minecraft.advancements.Advancement.Builder.advancement;
import static net.minecraft.advancements.criterion.ImpossibleTrigger.TriggerInstance;
import static net.minecraft.advancements.criterion.InventoryChangeTrigger.TriggerInstance.hasItems;
import static net.minecraft.advancements.criterion.ItemPredicate.Builder.item;
import static tictim.paraglider.api.ParagliderAPI.MODID;

public class AdvancementGen extends AdvancementProvider {
	public AdvancementGen(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
		super(output, registries, List.of((r, s) -> {
			Contents contents = Contents.get();

			AdvancementHolder root = advancement().display(contents.paraglider(),
							Component.translatable("advancement.paraglider"),
							Component.translatable("advancement.paraglider" + ".desc"),
							ParagliderAPI.id("gui/advancement_background"),
							AdvancementType.TASK,
							false,
							false,
							false)
					.addCriterion("crafting_table", hasItems(Blocks.CRAFTING_TABLE))
					.save(s, MODID + ":root");

			AdvancementHolder paraglider = advancement().display(contents.paraglider(),
							Component.translatable("advancement.paraglider.paraglider"),
							Component.translatable("advancement.paraglider.paraglider" + ".desc"),
							null,
							AdvancementType.GOAL,
							true,
							true,
							false)
					.parent(root)
					.addCriterion("paraglider", hasItems(item().of(r.lookupOrThrow(Registries.ITEM), ParagliderTags.PARAGLIDERS).build()))
					.save(s, MODID + ":paraglider");

			AdvancementHolder prayToTheGoddess = advancement().display(contents.goddessStatue(),
							Component.translatable("advancement.paraglider.pray_to_the_goddess"),
							Component.translatable("advancement.paraglider.pray_to_the_goddess" + ".desc"),
							null,
							AdvancementType.GOAL,
							true,
							true,
							false)
					.parent(root)
					.addCriterion("bargain", impossibleCriterion())
					.save(s, ParagliderAdvancements.PRAY_TO_THE_GODDESS.toString());

			AdvancementHolder statuesBargain = advancement().display(contents.hornedStatue(),
							Component.translatable("advancement.paraglider.statues_bargain"),
							Component.translatable("advancement.paraglider.statues_bargain" + ".desc"),
							null,
							AdvancementType.GOAL,
							true,
							true,
							false)
					.parent(root)
					.addCriterion("bargain", impossibleCriterion())
					.save(s, ParagliderAdvancements.STATUES_BARGAIN.toString());

			AdvancementHolder allVessels = advancement().display(contents.heartContainer(),
							Component.translatable("advancement.paraglider.all_vessels"),
							Component.translatable("advancement.paraglider.all_vessels" + ".desc"),
							null,
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
		return CriteriaTriggers.IMPOSSIBLE.createCriterion(new TriggerInstance());
	}

}
