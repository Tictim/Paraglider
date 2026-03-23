package datagen.test;

import datagen.builder.WindSourceBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.data.JsonCodecProvider;
import org.jspecify.annotations.NullMarked;
import tictim.paraglider.wind.WindSource;

import java.util.concurrent.CompletableFuture;

import static tictim.paraglider.api.ParagliderAPI.MODID;
import static tictim.paraglider.api.ParagliderAPI.id;

@NullMarked
public class TestWindSourceGen extends JsonCodecProvider<WindSource> {
	public TestWindSourceGen(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
		super(output, PackOutput.Target.DATA_PACK, MODID + "/wind_sources", WindSource.CODEC, lookupProvider, MODID);
	}

	@Override protected void gather() {
		new WindSourceBuilder()
				.blocks(Blocks.MAGMA_BLOCK)
				.height(3)
				.save(this, id("test/magma"));

		new WindSourceBuilder()
				.blocks(Blocks.LAVA)
				.blocks(Blocks.LAVA_CAULDRON)
				.height(5)
				.save(this, id("test/lava"));

		new WindSourceBuilder()
				.blocks(Blocks.GLOWSTONE)
				.height(1)
				.save(this, id("test/glowstone"));
	}

	@Override public String getName() {
		return super.getName() + " - test";
	}
}
