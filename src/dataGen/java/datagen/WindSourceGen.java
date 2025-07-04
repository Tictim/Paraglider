package datagen;

import datagen.builder.WindSourceBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.server.packs.PackType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.common.data.JsonCodecProvider;
import org.jetbrains.annotations.NotNull;
import tictim.paraglider.wind.WindSource;

import java.util.concurrent.CompletableFuture;

import static tictim.paraglider.api.ParagliderAPI.MODID;
import static tictim.paraglider.api.ParagliderAPI.id;

public class WindSourceGen extends JsonCodecProvider<WindSource> {
	public WindSourceGen(@NotNull PackOutput output, @NotNull CompletableFuture<HolderLookup.Provider> lookupProvider, ExistingFileHelper existingFileHelper) {
		super(output, PackOutput.Target.DATA_PACK, MODID + "/wind_sources", PackType.SERVER_DATA, WindSource.CODEC, lookupProvider, MODID, existingFileHelper);
	}

	@Override protected void gather() {
		new WindSourceBuilder()
				.blocks(Blocks.FIRE, Blocks.SOUL_FIRE)
				.save(this, id("fire"));

		new WindSourceBuilder()
				.blockStates(p ->
								p.property(CampfireBlock.LIT, true),
						Blocks.CAMPFIRE, Blocks.SOUL_CAMPFIRE)
				.save(this, id("campfire"));
	}
}
