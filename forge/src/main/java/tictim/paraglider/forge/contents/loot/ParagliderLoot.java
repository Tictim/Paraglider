package tictim.paraglider.forge.contents.loot;

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
import org.jetbrains.annotations.NotNull;
import tictim.paraglider.config.Cfg;
import tictim.paraglider.config.Cfg.TotwCompatConfigOption;
import tictim.paraglider.contents.Contents;
import tictim.paraglider.contents.item.ParagliderItem;

import java.util.Arrays;

import static tictim.paraglider.config.Cfg.TotwCompatConfigOption.*;

public class ParagliderLoot extends LootModifier{
	public static final Codec<ParagliderLoot> CODEC = RecordCodecBuilder.create(b ->
			b.group(Codec.BOOL.fieldOf("dekuLeaf").forGetter(m -> m.dekuLeaf))
					.and(codecStart(b).t1())
					.apply(b, ParagliderLoot::new));

	public final boolean dekuLeaf;

	public ParagliderLoot(boolean dekuLeaf, @NotNull LootItemCondition @NotNull ... conditions){
		super(conditions);
		this.dekuLeaf = dekuLeaf;
	}

	@Override @NotNull protected ObjectArrayList<ItemStack> doApply(@NotNull ObjectArrayList<ItemStack> generatedLoot, @NotNull LootContext context){
		TotwCompatConfigOption configOption = Cfg.get().paragliderInTowersOfTheWild();
		if(configOption!=DISABLE){
			ParagliderItem item = configOption==DEKU_LEAF_ONLY||configOption!=PARAGLIDER_ONLY&&dekuLeaf ?
					Contents.get().dekuLeaf() : Contents.get().paraglider();
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
	@Override @NotNull public Codec<? extends IGlobalLootModifier> codec(){
		return CODEC;
	}
}
