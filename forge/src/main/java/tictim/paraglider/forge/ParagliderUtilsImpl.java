package tictim.paraglider.forge;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.tags.ITagManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.Consumer;

public final class ParagliderUtilsImpl{
	private ParagliderUtilsImpl(){}

	public static boolean canBreatheUnderwater(@NotNull Player player){
		if(player.hasEffect(MobEffects.WATER_BREATHING)) return true;
		if(player.onGround()){
			if(!player.canDrownInFluidType(player.getEyeInFluidType())||player.level()
					.getBlockState(new BlockPos((int)player.getX(), (int)player.getEyeY(), (int)player.getZ()))
					.is(Blocks.BUBBLE_COLUMN)) return true;
		}

		ItemStack head = player.getItemBySlot(EquipmentSlot.HEAD);
		if(!head.isEmpty()){
			if(head.getItem()==Items.TURTLE_HELMET) return true;
			else if(head.getEnchantmentLevel(Enchantments.AQUA_AFFINITY)>0) return true;
		}
		ItemStack feet = player.getItemBySlot(EquipmentSlot.FEET);
		return !feet.isEmpty()&&feet.getEnchantmentLevel(Enchantments.DEPTH_STRIDER)>0;
	}

	public static boolean hasTag(@NotNull Block block, @NotNull TagKey<Block> tagKey){
		ITagManager<Block> tagManager = ForgeRegistries.BLOCKS.tags();
		return tagManager!=null&&!tagManager.isKnownTagName(tagKey)&&tagManager.getTag(tagKey).contains(block);
	}

	@NotNull public static Item getItem(@NotNull ResourceLocation id){
		return Objects.requireNonNullElse(ForgeRegistries.ITEMS.getValue(id), Items.AIR);
	}

	@Nullable public static ResourceLocation getKey(@NotNull Item item){
		return ForgeRegistries.ITEMS.getKey(item);
	}

	@NotNull public static Block getBlock(@NotNull ResourceLocation id){
		return Objects.requireNonNullElse(ForgeRegistries.BLOCKS.getValue(id), Blocks.AIR);
	}

	public static void forRemainingItem(@NotNull ItemStack stack, @NotNull Consumer<@NotNull ItemStack> forRemainingItem){
		if(stack.hasCraftingRemainingItem()){
			forRemainingItem.accept(stack.getCraftingRemainingItem());
		}
	}

	@OnlyIn(Dist.CLIENT)
	@NotNull public static InputConstants.Key getKey(@NotNull KeyMapping keyMapping){
		return keyMapping.getKey();
	}

	@OnlyIn(Dist.CLIENT)
	public static boolean isActiveAndMatches(@NotNull KeyMapping keyMapping, @NotNull InputConstants.Key key){
		return keyMapping.isActiveAndMatches(key);
	}
}
