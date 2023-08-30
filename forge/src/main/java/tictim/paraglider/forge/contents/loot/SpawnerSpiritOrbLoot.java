package tictim.paraglider.forge.contents.loot;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.common.loot.LootModifier;
import org.jetbrains.annotations.NotNull;
import tictim.paraglider.config.Cfg;
import tictim.paraglider.config.FeatureCfg;
import tictim.paraglider.contents.Contents;

public class SpawnerSpiritOrbLoot extends LootModifier{
	public static final Codec<SpawnerSpiritOrbLoot> CODEC = RecordCodecBuilder.create(b ->
			codecStart(b).apply(b, SpawnerSpiritOrbLoot::new));

	public SpawnerSpiritOrbLoot(@NotNull LootItemCondition @NotNull ... conditions){
		super(conditions);
	}

	@Override @NotNull protected ObjectArrayList<ItemStack> doApply(@NotNull ObjectArrayList<ItemStack> generatedLoot, @NotNull LootContext context){
		if(FeatureCfg.get().enableSpiritOrbGens()){
			int drops = Cfg.get().spawnerSpiritOrbDrops();
			if(drops>0){
				generatedLoot.add(new ItemStack(Contents.get().spiritOrb(), drops));
			}
		}
		return generatedLoot;
	}

	@Override @NotNull public Codec<? extends IGlobalLootModifier> codec(){
		return CODEC;
	}
}
