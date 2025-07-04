package datagen.test;

import datagen.builder.WindSourceBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.server.packs.PackType;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.common.data.JsonCodecProvider;
import org.jetbrains.annotations.NotNull;
import tictim.paraglider.wind.WindSource;

import java.util.concurrent.CompletableFuture;

import static tictim.paraglider.api.ParagliderAPI.MODID;
import static tictim.paraglider.api.ParagliderAPI.id;

public class TestWindSourceGen extends JsonCodecProvider<WindSource> {
	public TestWindSourceGen(@NotNull PackOutput output, @NotNull CompletableFuture<HolderLookup.Provider> lookupProvider, ExistingFileHelper existingFileHelper) {
		super(output, PackOutput.Target.DATA_PACK, MODID + "/wind_sources", PackType.SERVER_DATA, WindSource.CODEC, lookupProvider, MODID, existingFileHelper);
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

	@Override public @NotNull String getName() {
		return super.getName() + " - test";
	}
}
