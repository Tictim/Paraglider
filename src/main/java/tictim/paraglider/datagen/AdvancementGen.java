package tictim.paraglider.datagen;

import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.FrameType;
import net.minecraft.advancements.criterion.ImpossibleTrigger;
import net.minecraft.advancements.criterion.InventoryChangeTrigger;
import net.minecraft.advancements.criterion.ItemPredicate;
import net.minecraft.block.Blocks;
import net.minecraft.data.DataGenerator;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TranslationTextComponent;
import tictim.paraglider.contents.Contents;
import tictim.paraglider.contents.ModAdvancements;
import tictim.paraglider.contents.ModTags;

import javax.annotation.Nullable;
import java.util.function.Consumer;

import static net.minecraft.advancements.Advancement.Builder.builder;
import static tictim.paraglider.ParagliderMod.MODID;

public class AdvancementGen extends ModAdvancementProvider{
	public AdvancementGen(DataGenerator generatorIn){
		super(generatorIn);
	}

	@Override protected void registerAdvancements(Consumer<Advancement> consumer){
		Advancement root = advancement(
				new ItemStack(Contents.PARAGLIDER.get()),
				"advancement.paraglider",
				new ResourceLocation(MODID, "textures/gui/advancement_background.png"),
				FrameType.TASK,
				false,
				false,
				false)
				.withCriterion("crafting_table", InventoryChangeTrigger.Instance.forItems(Blocks.CRAFTING_TABLE))
				.register(consumer, MODID+":root");
		Advancement paraglider = advancement(
				new ItemStack(Contents.PARAGLIDER.get()),
				"advancement.paraglider.paraglider",
				FrameType.GOAL,
				true,
				true,
				false)
				.withParent(root)
				.withCriterion("paraglider", InventoryChangeTrigger.Instance.forItems(ItemPredicate.Builder.create().tag(ModTags.PARAGLIDERS).build()))
				.register(consumer, MODID+":paraglider");
		Advancement prayToTheGoddess = advancement(
				new ItemStack(Contents.GODDESS_STATUE.get()),
				"advancement.paraglider.pray_to_the_goddess",
				FrameType.GOAL,
				true,
				true,
				false)
				.withParent(root)
				.withCriterion("bargain", new ImpossibleTrigger.Instance())
				.register(consumer, ModAdvancements.PRAY_TO_THE_GODDESS.toString());
		Advancement statuesBargain = advancement(
				new ItemStack(Contents.HORNED_STATUE.get()),
				"advancement.paraglider.statues_bargain",
				FrameType.GOAL,
				true,
				true,
				false)
				.withParent(root)
				.withCriterion("bargain", new ImpossibleTrigger.Instance())
				.register(consumer, ModAdvancements.STATUES_BARGAIN.toString());
		Advancement allVessels = advancement(
				new ItemStack(Contents.HEART_CONTAINER.get()),
				"advancement.paraglider.all_vessels",
				FrameType.CHALLENGE,
				true,
				true,
				false)
				.withParent(root)
				.withCriterion("code_triggered", new ImpossibleTrigger.Instance())
				.register(consumer, ModAdvancements.ALL_VESSELS.toString());
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
		return builder().withDisplay(stack,
				new TranslationTextComponent(display),
				new TranslationTextComponent(display+".desc"),
				background,
				frameType,
				showToast,
				announceToChat,
				hidden);
	}
}
