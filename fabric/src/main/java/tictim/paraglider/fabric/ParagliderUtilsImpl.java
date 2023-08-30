package tictim.paraglider.fabric;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.KeyMapping;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.function.Consumer;

public final class ParagliderUtilsImpl{
	private ParagliderUtilsImpl(){}

	public static boolean canBreatheUnderwater(@NotNull Player player){
		if(player.hasEffect(MobEffects.WATER_BREATHING)) return true;
		if(player.onGround()){
			if(player.canBreatheUnderwater()||player.level()
					.getBlockState(new BlockPos((int)player.getX(), (int)player.getEyeY(), (int)player.getZ()))
					.is(Blocks.BUBBLE_COLUMN)) return true;
		}

		ItemStack head = player.getItemBySlot(EquipmentSlot.HEAD);
		if(!head.isEmpty()){
			if(head.getItem()==Items.TURTLE_HELMET) return true;
			else if(EnchantmentHelper.getItemEnchantmentLevel(Enchantments.AQUA_AFFINITY, head)>0) return true;
		}
		ItemStack feet = player.getItemBySlot(EquipmentSlot.FEET);
		return !feet.isEmpty()&&EnchantmentHelper.getItemEnchantmentLevel(Enchantments.DEPTH_STRIDER, feet)>0;
	}

	public static boolean hasTag(@NotNull Block block, @NotNull TagKey<Block> tagKey){
		Optional<HolderSet.Named<Block>> tag = BuiltInRegistries.BLOCK.getTag(tagKey);
		return tag.isPresent()&&tag.get().contains(Holder.direct(block));
	}

	@NotNull public static Item getItem(@NotNull ResourceLocation id){
		return BuiltInRegistries.ITEM.get(id);
	}

	@NotNull public static ResourceLocation getKey(@NotNull Item item){
		return BuiltInRegistries.ITEM.getKey(item);
	}

	@NotNull public static Block getBlock(@NotNull ResourceLocation id){
		return BuiltInRegistries.BLOCK.get(id);
	}

	public static void forRemainingItem(@NotNull ItemStack stack, @NotNull Consumer<@NotNull ItemStack> forRemainingItem){
		ItemStack recipeRemainder = stack.getItem().getRecipeRemainder(stack);
		if(!recipeRemainder.isEmpty()) forRemainingItem.accept(recipeRemainder);
	}

	@Environment(EnvType.CLIENT)
	@NotNull public static InputConstants.Key getKey(@NotNull KeyMapping keyMapping){
		return KeyBindingHelper.getBoundKeyOf(keyMapping);
	}

	@Environment(EnvType.CLIENT)
	public static boolean isActiveAndMatches(@NotNull KeyMapping keyMapping, @NotNull InputConstants.Key key){
		return key!=InputConstants.UNKNOWN&&key.equals(KeyBindingHelper.getBoundKeyOf(keyMapping));
	}

	public static boolean isClient(){
		return FabricLoader.getInstance().getEnvironmentType()==EnvType.CLIENT;
	}
}
