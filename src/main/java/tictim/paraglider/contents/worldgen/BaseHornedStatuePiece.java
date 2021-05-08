package tictim.paraglider.contents.worldgen;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Mirror;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.world.IServerWorld;
import net.minecraft.world.gen.feature.structure.IStructurePieceType;
import net.minecraft.world.gen.feature.structure.TemplateStructurePiece;
import net.minecraft.world.gen.feature.template.PlacementSettings;
import net.minecraft.world.gen.feature.template.TemplateManager;

import java.util.Random;

public abstract class BaseHornedStatuePiece extends TemplateStructurePiece{
	private final Rotation rotation;

	public BaseHornedStatuePiece(IStructurePieceType structurePieceType, TemplateManager templateManager, BlockPos pos, Rotation rotation){
		super(structurePieceType, 0);
		this.templatePosition = pos;
		this.rotation = rotation;
		setup(templateManager);
	}
	public BaseHornedStatuePiece(IStructurePieceType structurePieceType, TemplateManager templateManager, CompoundNBT nbt){
		super(structurePieceType, nbt);
		this.rotation = Rotation.valueOf(nbt.getString("Rot"));
		setup(templateManager);
	}

	private void setup(TemplateManager templateManager){
		setup(templateManager.getTemplateDefaulted(getTemplate()),
				this.templatePosition,
				new PlacementSettings()
						.setRotation(rotation)
						.setMirror(Mirror.NONE));
	}

	protected abstract ResourceLocation getTemplate();

	@Override protected void readAdditional(CompoundNBT nbt){
		super.readAdditional(nbt);
		nbt.putString("Rot", this.rotation.name());
	}

	@Override protected void handleDataMarker(String function, BlockPos pos, IServerWorld worldIn, Random rand, MutableBoundingBox sbb){}
}
