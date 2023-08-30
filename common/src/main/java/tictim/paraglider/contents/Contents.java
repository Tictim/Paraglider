package tictim.paraglider.contents;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import org.jetbrains.annotations.NotNull;
import tictim.paraglider.ParagliderMod;
import tictim.paraglider.api.bargain.Bargain;
import tictim.paraglider.contents.item.ParagliderItem;
import tictim.paraglider.contents.recipe.CosmeticRecipe;
import tictim.paraglider.contents.worldgen.NetherHornedStatue;
import tictim.paraglider.contents.worldgen.TarreyTownGoddessStatue;
import tictim.paraglider.contents.worldgen.UndergroundHornedStatue;

public interface Contents{
	@NotNull static Contents get(){
		return ParagliderMod.instance().getContents();
	}

	// items

	@NotNull ParagliderItem paraglider();
	@NotNull ParagliderItem dekuLeaf();

	@NotNull Item heartContainer();
	@NotNull Item staminaVessel();
	@NotNull Item spiritOrb();
	@NotNull Item antiVessel();
	@NotNull Item essence();

	// blocks

	@NotNull Block goddessStatue();
	@NotNull Block kakarikoGoddessStatue();
	@NotNull Block goronGoddessStatue();
	@NotNull Block ritoGoddessStatue();
	@NotNull Block hornedStatue();

	@NotNull BlockItem goddessStatueItem();
	@NotNull BlockItem kakarikoGoddessStatueItem();
	@NotNull BlockItem goronGoddessStatueItem();
	@NotNull BlockItem ritoGoddessStatueItem();
	@NotNull BlockItem hornedStatueItem();

	// recipes

	@NotNull CosmeticRecipe.Serializer cosmeticRecipeSerializer();
	@NotNull RecipeSerializer<? extends Bargain> bargainRecipeSerializer();

	@NotNull RecipeType<Bargain> bargainRecipeType();

	// structures

	@NotNull StructureType<TarreyTownGoddessStatue> tarreyTownGoddessStatue();
	@NotNull StructureType<NetherHornedStatue> netherHornedStatue();
	@NotNull StructureType<UndergroundHornedStatue> undergroundHornedStatue();

	@NotNull StructurePieceType tarreyTownGoddessStatuePiece();
	@NotNull StructurePieceType netherHornedStatuePiece();
	@NotNull StructurePieceType undergroundHornedStatuePiece();
}
