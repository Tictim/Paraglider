package tictim.paraglider.fabric.contents.loot;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import org.jetbrains.annotations.NotNull;
import tictim.paraglider.config.Cfg;

public enum LootFunctions implements LootItemFunction, LootItemFunction.Builder{
	SPAWNER_SPIRIT_ORB_COUNT;

	private final Codec<LootItemFunction> codec = RecordCodecBuilder.create(instance -> instance.stable(this));

	@Override @NotNull public LootItemFunctionType getType(){
		return switch(this){
			case SPAWNER_SPIRIT_ORB_COUNT -> ParagliderLoots.SPAWNER_SPIRIT_ORB_COUNT;
		};
	}
	@Override @NotNull public ItemStack apply(ItemStack stack, LootContext lootContext){
		return switch(this){
			case SPAWNER_SPIRIT_ORB_COUNT -> {
				stack.setCount(Cfg.get().spawnerSpiritOrbDrops());
				yield stack;
			}
		};
	}

	@NotNull public Codec<LootItemFunction> codec(){
		return codec;
	}

	@Override @NotNull public LootItemFunction build(){
		return this;
	}
}
