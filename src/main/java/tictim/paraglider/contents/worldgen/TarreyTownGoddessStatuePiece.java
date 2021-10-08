package tictim.paraglider.contents.worldgen;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;
import tictim.paraglider.contents.ModStructures;

import static tictim.paraglider.ParagliderMod.MODID;

public class TarreyTownGoddessStatuePiece extends BaseHornedStatuePiece{
	private static final ResourceLocation TEMPLATE = new ResourceLocation(MODID, "tarrey_town_goddess_statue");

	public TarreyTownGoddessStatuePiece(StructureManager structureManager, Rotation rotation, BlockPos templatePos){
		super(ModStructures.TARREY_TOWN_GODDESS_STATUE_PIECE_TYPE, structureManager, TEMPLATE, rotation, templatePos);
	}
	public TarreyTownGoddessStatuePiece(ServerLevel level, CompoundTag tag){
		super(ModStructures.TARREY_TOWN_GODDESS_STATUE_PIECE_TYPE, level, tag);
	}
}
