package tictim.paraglider.contents.worldgen;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.feature.template.TemplateManager;
import tictim.paraglider.contents.ModStructures;

import static tictim.paraglider.ParagliderMod.MODID;

public class NetherHornedStatuePiece extends BaseHornedStatuePiece{
	private static final ResourceLocation TEMPLATE = new ResourceLocation(MODID, "nether_horned_statue");

	public NetherHornedStatuePiece(TemplateManager templateManager, BlockPos pos, Rotation rotation){
		super(ModStructures.NETHER_HORNED_STATUE_PIECE_TYPE, templateManager, pos, rotation);
	}
	public NetherHornedStatuePiece(TemplateManager templateManager, CompoundNBT nbt){
		super(ModStructures.NETHER_HORNED_STATUE_PIECE_TYPE, templateManager, nbt);
	}

	@Override protected ResourceLocation getTemplate(){
		return TEMPLATE;
	}
}
