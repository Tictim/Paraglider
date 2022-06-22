package tictim.paraglider.loot;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.DyeableLeatherItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.common.loot.LootModifier;
import tictim.paraglider.ModCfg;
import tictim.paraglider.contents.Contents;
import tictim.paraglider.item.ParagliderItem;

import javax.annotation.Nonnull;
import java.util.Arrays;

public class ParagliderModifier extends LootModifier{
	public static final Codec<ParagliderModifier> CODEC = RecordCodecBuilder.create(inst ->
			codecStart(inst)
					.and(Codec.BOOL.fieldOf("dekuLeaf").forGetter(m -> m.dekuLeaf))
					.apply(inst, ParagliderModifier::new));

	public final boolean dekuLeaf;

	public ParagliderModifier(LootItemCondition[] conditionsIn){
		this(conditionsIn, false);
	}
	public ParagliderModifier(LootItemCondition[] conditionsIn, boolean dekuLeaf){
		super(conditionsIn);
		this.dekuLeaf = dekuLeaf;
	}

	@Nonnull @Override protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context){
		ConfigOption configOption = ModCfg.paragliderInTowersOfTheWild();
		if(configOption!=ConfigOption.DISABLE){
			ParagliderItem item = (configOption==ConfigOption.DEKU_LEAF_ONLY||(configOption!=ConfigOption.PARAGLIDER_ONLY&&dekuLeaf) ?
					Contents.DEKU_LEAF : Contents.PARAGLIDER).get();
			ItemStack stack = new ItemStack(item);
			if(context.getRandom().nextBoolean()){
				stack = DyeableLeatherItem.dyeArmor(stack, Arrays.asList(
						DyeItem.byColor(DyeColor.byId(context.getRandom().nextInt(16))),
						DyeItem.byColor(DyeColor.byId(context.getRandom().nextInt(16)))));
			}
			generatedLoot.add(stack);
		}
		return generatedLoot;
	}
	@Override public Codec<? extends IGlobalLootModifier> codec(){
		return CODEC;
	}

	public enum ConfigOption{
		/**
		 * Default option, spawn Deku Leaf in ocean tower chests and Paraglider in normal tower chests
		 */
		DEFAULT,
		/**
		 * Don't spawn anything
		 */
		DISABLE,
		/**
		 * Spawn paraglider in both ocean and normal tower chests
		 */
		PARAGLIDER_ONLY,
		/**
		 * Spawn deku leaf in both ocean and normal tower chests, like a boss
		 */
		DEKU_LEAF_ONLY
	}
}
