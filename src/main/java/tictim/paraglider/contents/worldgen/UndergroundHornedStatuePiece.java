package tictim.paraglider.contents.worldgen;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;
import tictim.paraglider.contents.ModStructures;

import static tictim.paraglider.ParagliderMod.MODID;

public class UndergroundHornedStatuePiece extends BaseHornedStatuePiece{
	private static final ResourceLocation TEMPLATE = new ResourceLocation(MODID, "underground_horned_statue");

	public UndergroundHornedStatuePiece(StructureManager structureManager, Rotation rotation, BlockPos templatePos){
		super(ModStructures.UNDERGROUND_HORNED_STATUE_PIECE_TYPE, structureManager, TEMPLATE, rotation, templatePos);
	}
	public UndergroundHornedStatuePiece(ServerLevel level, CompoundTag tag){
		super(ModStructures.UNDERGROUND_HORNED_STATUE_PIECE_TYPE, level, tag);
	}
}
