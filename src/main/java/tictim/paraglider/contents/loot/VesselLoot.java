package tictim.paraglider.contents.loot;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.common.loot.LootModifier;
import org.jspecify.annotations.NullMarked;
import tictim.paraglider.ParagliderUtils;

@NullMarked
public class VesselLoot extends LootModifier {
	public static final MapCodec<VesselLoot> CODEC = RecordCodecBuilder.mapCodec(b ->
			b.group(Codec.INT.fieldOf("count").forGetter(m -> m.count))
					.and(codecStart(b).t1())
					.apply(b, VesselLoot::new));

	private final int count;

	public VesselLoot(int count, LootItemCondition... conditions) {
		super(conditions);
		this.count = count;
	}

	@Override protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
		Item item = ParagliderUtils.getAppropriateVessel();
		if (item != null) generatedLoot.add(new ItemStack(item, count));
		return generatedLoot;
	}

	@Override public MapCodec<? extends IGlobalLootModifier> codec() {
		return CODEC;
	}
}
