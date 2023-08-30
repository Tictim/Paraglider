package tictim.paraglider.forge.contents.loot;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.common.loot.LootModifier;
import org.jetbrains.annotations.NotNull;
import tictim.paraglider.ParagliderUtils;

public class VesselLoot extends LootModifier{
	public static final Codec<VesselLoot> CODEC = RecordCodecBuilder.create(b ->
			b.group(Codec.INT.fieldOf("count").forGetter(m -> m.count))
					.and(codecStart(b).t1())
					.apply(b, VesselLoot::new));

	private final int count;

	public VesselLoot(int count, @NotNull LootItemCondition @NotNull ... conditions){
		super(conditions);
		this.count = count;
	}

	@Override @NotNull protected ObjectArrayList<ItemStack> doApply(@NotNull ObjectArrayList<ItemStack> generatedLoot, @NotNull LootContext context){
		Item item = ParagliderUtils.getAppropriateVessel();
		if(item!=null) generatedLoot.add(new ItemStack(item, count));
		return generatedLoot;
	}

	@Override @NotNull public Codec<? extends IGlobalLootModifier> codec(){
		return CODEC;
	}
}
