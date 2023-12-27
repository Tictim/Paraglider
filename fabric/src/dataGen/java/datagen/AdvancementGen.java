package datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricAdvancementProvider;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.FrameType;
import net.minecraft.advancements.critereon.ImpossibleTrigger;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import org.jetbrains.annotations.Nullable;
import tictim.paraglider.api.ParagliderAPI;
import tictim.paraglider.contents.Contents;
import tictim.paraglider.contents.ParagliderAdvancements;
import tictim.paraglider.contents.ParagliderTags;

import java.util.function.Consumer;

import static net.minecraft.advancements.critereon.InventoryChangeTrigger.TriggerInstance.hasItems;
import static net.minecraft.advancements.critereon.ItemPredicate.Builder.item;
import static tictim.paraglider.api.ParagliderAPI.MODID;

public class AdvancementGen extends FabricAdvancementProvider{
	protected AdvancementGen(FabricDataOutput output){
		super(output);
	}

	@Override public void generateAdvancement(Consumer<AdvancementHolder> consumer){
		Contents contents = Contents.get();
		AdvancementHolder root = advancement(
				new ItemStack(contents.paraglider()),
				"advancement.paraglider",
				ParagliderAPI.id("textures/gui/advancement_background.png"),
				FrameType.TASK,
				false,
				false,
				false)
				.addCriterion("crafting_table", hasItems(Blocks.CRAFTING_TABLE))
				.save(consumer, MODID+":root");
		AdvancementHolder paraglider = advancement(
				new ItemStack(contents.paraglider()),
				"advancement.paraglider.paraglider",
				FrameType.GOAL,
				true,
				true,
				false)
				.parent(root)
				.addCriterion("paraglider", hasItems(item().of(ParagliderTags.PARAGLIDERS).build()))
				.save(consumer, MODID+":paraglider");
		AdvancementHolder prayToTheGoddess = advancement(
				new ItemStack(contents.goddessStatue()),
				"advancement.paraglider.pray_to_the_goddess",
				FrameType.GOAL,
				true,
				true,
				false)
				.parent(root)
				.addCriterion("bargain", CriteriaTriggers.IMPOSSIBLE.createCriterion(new ImpossibleTrigger.TriggerInstance()))
				.save(consumer, ParagliderAdvancements.PRAY_TO_THE_GODDESS.toString());
		AdvancementHolder statuesBargain = advancement(
				new ItemStack(contents.hornedStatue()),
				"advancement.paraglider.statues_bargain",
				FrameType.GOAL,
				true,
				true,
				false)
				.parent(root)
				.addCriterion("bargain", CriteriaTriggers.IMPOSSIBLE.createCriterion(new ImpossibleTrigger.TriggerInstance()))
				.save(consumer, ParagliderAdvancements.STATUES_BARGAIN.toString());
		AdvancementHolder allVessels = advancement(
				new ItemStack(contents.heartContainer()),
				"advancement.paraglider.all_vessels",
				FrameType.CHALLENGE,
				true,
				true,
				false)
				.parent(root)
				.addCriterion("code_triggered", CriteriaTriggers.IMPOSSIBLE.createCriterion(new ImpossibleTrigger.TriggerInstance()))
				.save(consumer, ParagliderAdvancements.ALL_VESSELS.toString());
	}

	private static Advancement.Builder advancement(ItemStack stack,
	                                               String display,
	                                               FrameType frameType,
	                                               boolean showToast,
	                                               boolean announceToChat,
	                                               boolean hidden){
		return advancement(stack, display, null, frameType, showToast, announceToChat, hidden);
	}
	private static Advancement.Builder advancement(ItemStack stack,
	                                               String display,
	                                               @Nullable ResourceLocation background,
	                                               FrameType frameType,
	                                               boolean showToast,
	                                               boolean announceToChat,
	                                               boolean hidden){
		return Advancement.Builder.advancement().display(stack,
				Component.translatable(display),
				Component.translatable(display+".desc"),
				background,
				frameType,
				showToast,
				announceToChat,
				hidden);
	}
}
