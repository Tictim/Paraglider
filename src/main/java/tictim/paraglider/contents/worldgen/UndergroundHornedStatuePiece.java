package tictim.paraglider.contents.worldgen;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.feature.template.TemplateManager;
import tictim.paraglider.contents.ModStructures;

import static tictim.paraglider.ParagliderMod.MODID;

public class UndergroundHornedStatuePiece extends BaseHornedStatuePiece{
	private static final ResourceLocation TEMPLATE = new ResourceLocation(MODID, "underground_horned_statue");

	public UndergroundHornedStatuePiece(TemplateManager templateManager, BlockPos pos, Rotation rotation){
		super(ModStructures.UNDERGROUND_HORNED_STATUE_PIECE_TYPE, templateManager, pos, rotation);
	}
	public UndergroundHornedStatuePiece(TemplateManager templateManager, CompoundNBT nbt){
		super(ModStructures.UNDERGROUND_HORNED_STATUE_PIECE_TYPE, templateManager, nbt);
	}

	@Override protected ResourceLocation getTemplate(){
		return TEMPLATE;
	}
}
