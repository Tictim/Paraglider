package tictim.paraglider.loot;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.common.loot.LootModifier;
import tictim.paraglider.ModCfg;
import tictim.paraglider.contents.Contents;

import javax.annotation.Nonnull;

public class SpiritOrbLootModifier extends LootModifier{
	public static final Codec<SpiritOrbLootModifier> CODEC = RecordCodecBuilder.create(inst ->
			codecStart(inst)
					.and(Codec.INT.fieldOf("count").forGetter(m -> m.count))
					.apply(inst, SpiritOrbLootModifier::new));

	private final int count;

	public SpiritOrbLootModifier(LootItemCondition[] conditionsIn){
		this(conditionsIn, 0);
	}
	public SpiritOrbLootModifier(LootItemCondition[] conditionsIn, int count){
		super(conditionsIn);
		this.count = count;
	}

	@Nonnull @Override protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context){
		if(ModCfg.enableSpiritOrbGens()) generatedLoot.add(new ItemStack(Contents.SPIRIT_ORB.get(), count));
		return generatedLoot;
	}

	@Override public Codec<? extends IGlobalLootModifier> codec(){
		return CODEC;
	}
}
