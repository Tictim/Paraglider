package tictim.paraglider.fabric.contents;

import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import org.jetbrains.annotations.NotNull;
import tictim.paraglider.api.bargain.Bargain;
import tictim.paraglider.contents.Contents;
import tictim.paraglider.contents.block.GoddessStatueBlock;
import tictim.paraglider.contents.block.HornedStatueBlock;
import tictim.paraglider.contents.item.AntiVesselItem;
import tictim.paraglider.contents.item.EssenceItem;
import tictim.paraglider.contents.item.HeartContainerItem;
import tictim.paraglider.contents.item.ParagliderItem;
import tictim.paraglider.contents.item.StaminaVesselItem;
import tictim.paraglider.contents.recipe.CosmeticRecipe;
import tictim.paraglider.contents.worldgen.NetherHornedStatue;
import tictim.paraglider.contents.worldgen.TarreyTownGoddessStatue;
import tictim.paraglider.contents.worldgen.UndergroundHornedStatue;
import tictim.paraglider.fabric.contents.item.FabricParagliderItem;
import tictim.paraglider.fabric.contents.recipe.FabricBargainSerializer;

import static net.minecraft.core.registries.BuiltInRegistries.*;
import static tictim.paraglider.api.ParagliderAPI.MODID;
import static tictim.paraglider.api.ParagliderAPI.id;
import static tictim.paraglider.contents.CommonContents.*;

public record FabricContents(
		ParagliderItem paraglider,
		ParagliderItem dekuLeaf,
		Item heartContainer,
		Item staminaVessel,
		Item spiritOrb,
		Item antiVessel,
		Item essence,

		Block goddessStatue,
		Block kakarikoGoddessStatue,
		Block goronGoddessStatue,
		Block ritoGoddessStatue,
		Block hornedStatue,

		BlockItem goddessStatueItem,
		BlockItem kakarikoGoddessStatueItem,
		BlockItem goronGoddessStatueItem,
		BlockItem ritoGoddessStatueItem,
		BlockItem hornedStatueItem,

		CosmeticRecipe.Serializer cosmeticRecipeSerializer,
		FabricBargainSerializer bargainRecipeSerializer,

		RecipeType<Bargain> bargainRecipeType,

		StructureType<TarreyTownGoddessStatue> tarreyTownGoddessStatue,
		StructureType<NetherHornedStatue> netherHornedStatue,
		StructureType<UndergroundHornedStatue> undergroundHornedStatue,

		StructurePieceType tarreyTownGoddessStatuePiece,
		StructurePieceType netherHornedStatuePiece,
		StructurePieceType undergroundHornedStatuePiece,

		CreativeModeTab tab
) implements Contents{
	@NotNull public static FabricContents create(){
		var paraglider = new FabricParagliderItem(PARAGLIDER_DEFAULT_COLOR);
		var dekuLeaf = new FabricParagliderItem(DEKU_LEAF_DEFAULT_COLOR);
		var heartContainer = new HeartContainerItem(rareItem());
		var staminaVessel = new StaminaVesselItem(rareItem());
		var spiritOrb = new Item(uncommonItem());
		var antiVessel = new AntiVesselItem(epicItem());
		var essence = new EssenceItem(rareItem());

		var goddessStatue = new GoddessStatueBlock(statueBlock());
		var kakarikoGoddessStatue = new GoddessStatueBlock(statueBlock(), kakarikoStatueTooltip());
		var goronGoddessStatue = new GoddessStatueBlock(statueBlock().lightLevel(value -> 15), goronStatueTooltip());
		var ritoGoddessStatue = new GoddessStatueBlock(statueBlock(), ritoStatueTooltip());
		var hornedStatue = new HornedStatueBlock(statueBlock());

		var goddessStatueItem = new BlockItem(goddessStatue, rareItem());
		var kakarikoGoddessStatueItem = new BlockItem(kakarikoGoddessStatue, rareItem());
		var goronGoddessStatueItem = new BlockItem(goronGoddessStatue, rareItem());
		var ritoGoddessStatueItem = new BlockItem(ritoGoddessStatue, rareItem());
		var hornedStatueItem = new BlockItem(hornedStatue, epicItem());

		var cosmeticRecipeSerializer = new CosmeticRecipe.Serializer();
		var simpleBargainRecipeSerializer = new FabricBargainSerializer();

		var bargainRecipeType = new RecipeType<Bargain>(){
			@Override public String toString(){
				return MODID+":bargain";
			}
		};

		StructureType<TarreyTownGoddessStatue> tarreyTownGoddessStatue = () -> TarreyTownGoddessStatue.CODEC;
		StructureType<NetherHornedStatue> netherHornedStatue = () -> NetherHornedStatue.CODEC;
		StructureType<UndergroundHornedStatue> undergroundHornedStatue = () -> UndergroundHornedStatue.CODEC;

		var tarreyTownGoddessStatuePiece = TarreyTownGoddessStatue.pieceType();
		var netherHornedStatuePiece = NetherHornedStatue.pieceType();
		var undergroundHornedStatuePiece = UndergroundHornedStatue.pieceType();

		var tab = FabricItemGroup.builder()
				.icon(() -> new ItemStack(paraglider))
				.title(Component.translatable("itemGroup."+MODID))
				.displayItems((features, out) -> {
					out.accept(paraglider);
					out.accept(dekuLeaf);
					out.accept(heartContainer);
					out.accept(staminaVessel);
					out.accept(spiritOrb);
					out.accept(antiVessel);
					out.accept(essence);
					out.accept(goddessStatue);
					out.accept(kakarikoGoddessStatue);
					out.accept(goronGoddessStatue);
					out.accept(ritoGoddessStatue);
					out.accept(hornedStatue);
				}).build();

		return new FabricContents(paraglider, dekuLeaf, heartContainer, staminaVessel, spiritOrb, antiVessel, essence,
				goddessStatue, kakarikoGoddessStatue, goronGoddessStatue, ritoGoddessStatue, hornedStatue,
				goddessStatueItem, kakarikoGoddessStatueItem, goronGoddessStatueItem, ritoGoddessStatueItem,
				hornedStatueItem, cosmeticRecipeSerializer, simpleBargainRecipeSerializer, bargainRecipeType,
				tarreyTownGoddessStatue, netherHornedStatue, undergroundHornedStatue, tarreyTownGoddessStatuePiece,
				netherHornedStatuePiece, undergroundHornedStatuePiece, tab);
	}

	public void register(){
		Registry.register(ITEM, id("paraglider"), paraglider);
		Registry.register(ITEM, id("deku_leaf"), dekuLeaf);
		Registry.register(ITEM, id("heart_container"), heartContainer);
		Registry.register(ITEM, id("stamina_vessel"), staminaVessel);
		Registry.register(ITEM, id("spirit_orb"), spiritOrb);
		Registry.register(ITEM, id("anti_vessel"), antiVessel);
		Registry.register(ITEM, id("essence"), essence);

		Registry.register(BLOCK, id("goddess_statue"), goddessStatue);
		Registry.register(BLOCK, id("kakariko_goddess_statue"), kakarikoGoddessStatue);
		Registry.register(BLOCK, id("goron_goddess_statue"), goronGoddessStatue);
		Registry.register(BLOCK, id("rito_goddess_statue"), ritoGoddessStatue);
		Registry.register(BLOCK, id("horned_statue"), hornedStatue);

		Registry.register(ITEM, id("goddess_statue"), goddessStatueItem);
		Registry.register(ITEM, id("kakariko_goddess_statue"), kakarikoGoddessStatueItem);
		Registry.register(ITEM, id("goron_goddess_statue"), goronGoddessStatueItem);
		Registry.register(ITEM, id("rito_goddess_statue"), ritoGoddessStatueItem);
		Registry.register(ITEM, id("horned_statue"), hornedStatueItem);

		Registry.register(RECIPE_SERIALIZER, id("cosmetic"), cosmeticRecipeSerializer);
		Registry.register(RECIPE_SERIALIZER, id("statue_bargain"), bargainRecipeSerializer);

		Registry.register(RECIPE_TYPE, id("bargain"), bargainRecipeType);

		Registry.register(STRUCTURE_TYPE, id("tarrey_town_goddess_statue"), tarreyTownGoddessStatue);
		Registry.register(STRUCTURE_TYPE, id("nether_horned_statue"), netherHornedStatue);
		Registry.register(STRUCTURE_TYPE, id("underground_horned_statue"), undergroundHornedStatue);

		Registry.register(STRUCTURE_PIECE, id("tarrey_town_goddess_statue"), tarreyTownGoddessStatuePiece);
		Registry.register(STRUCTURE_PIECE, id("nether_horned_statue"), netherHornedStatuePiece);
		Registry.register(STRUCTURE_PIECE, id("underground_horned_statue"), undergroundHornedStatuePiece);

		Registry.register(CREATIVE_MODE_TAB, id(MODID), tab);
	}
}
