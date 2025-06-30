package tictim.paraglider;

import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.ServerAdvancementManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Blocks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tictim.paraglider.api.ParagliderAPI;
import tictim.paraglider.api.ParagliderItemCapability;
import tictim.paraglider.config.DebugCfg;
import tictim.paraglider.config.FeatureCfg;
import tictim.paraglider.contents.Contents;
import tictim.paraglider.contents.recipe.QuantifiedIngredient;
import tictim.paraglider.impl.movement.PlayerStateConnectionMap;
import tictim.paraglider.impl.movement.PlayerStateMap;

import java.util.Random;
import java.util.stream.Collectors;

public final class ParagliderUtils {
	private ParagliderUtils() {}

	public static final Random DIALOG_RNG = new Random();
	public static final Random PARTICLE_RNG = new Random();

	/**
	 * Give {@code stack} to {@code player}. If there's no more room left, the item will be dropped in the world as
	 * entity. Accounts for ItemStacks with count more than its maximum stack size.
	 *
	 * @param player Who will receive the item
	 * @param stack  The item to be given
	 * @see net.minecraft.world.entity.player.Inventory#placeItemBackInInventory(ItemStack, boolean)
	 */
	public static void giveItem(@NotNull Player player, @NotNull ItemStack stack) {
		if (player.level().isClientSide) return;
		while (!stack.isEmpty()) {
			int slot = player.getInventory().getSlotWithRemainingSpace(stack);
			if (slot == -1) slot = player.getInventory().getFreeSlot();

			if (slot == -1) {
				while (!stack.isEmpty()) {
					ItemEntity itemEntity = new ItemEntity(player.level(),
							player.getX(),
							player.getY(.5),
							player.getZ(),
							stack.split(stack.getMaxStackSize()));
					itemEntity.setPickUpDelay(40);
					itemEntity.setDeltaMovement(0, 0, 0);
					player.level().addFreshEntity(itemEntity);
				}
				break;
			}

			int count = stack.getMaxStackSize() - player.getInventory().getItem(slot).getCount();
			if (player.getInventory().add(slot, stack.split(count)) && player instanceof ServerPlayer serverPlayer)
				serverPlayer.connection.send(
						new ClientboundContainerSetSlotPacket(-2, 0, slot, player.getInventory().getItem(slot)));
		}
	}

	/**
	 * Returns Heart Container, Stamina Vessel or nothing based on config value.
	 */
	public static @Nullable Item getAppropriateVessel() {
		FeatureCfg cfg = FeatureCfg.get();
		return cfg.enableHeartContainers() ? Contents.get().heartContainer() :
				cfg.enableStaminaVessels() ? Contents.get().staminaVessel() :
						null;
	}

	private static final AttributeModifier EXHAUSTION = new AttributeModifier(
			ParagliderAPI.id("exhaustion"), -0.3, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
	);

	public static void addExhaustion(@NotNull LivingEntity entity) {
		AttributeInstance attr = entity.getAttribute(Attributes.MOVEMENT_SPEED);
		if (attr == null || attr.getModifier(EXHAUSTION.id()) != null) return;
		attr.addTransientModifier(EXHAUSTION);
	}

	public static void removeExhaustion(@NotNull LivingEntity entity) {
		AttributeInstance attr = entity.getAttribute(Attributes.MOVEMENT_SPEED);
		if (attr == null) return;
		attr.removeModifier(EXHAUSTION.id());
	}

	@SuppressWarnings("UnusedReturnValue")
	public static boolean giveAdvancement(@NotNull ServerPlayer player,
	                                      @NotNull ResourceLocation advancementName,
	                                      @NotNull String criterion) {
		PlayerAdvancements advancements = player.getAdvancements();
		ServerAdvancementManager advancementManager = player.server.getAdvancements();
		AdvancementHolder advancement = advancementManager.get(advancementName);
		return advancement != null && advancements.award(advancement, criterion);
	}

	/**
	 * Tries to calculate item consumption for ingredient
	 *
	 * @param ingredient   Quantified ingredient
	 * @param inventory    Inventory
	 * @param consumptions Inventory index to consumption count, will be modified by this method
	 * @return Whether it is possible to consume given amount of ingredient from the inventory
	 */
	public static boolean calculateConsumption(@NotNull QuantifiedIngredient ingredient,
	                                           @NotNull Container inventory,
	                                           @NotNull Int2IntOpenHashMap consumptions) {
		int amountLeft = ingredient.quantity();
		for (int i = 0; amountLeft > 0 && i < inventory.getContainerSize(); i++) {
			ItemStack stack = inventory.getItem(i);
			int consumption = consumptions.get(i);
			// already consumed entirety of the stack, or not a valid input
			if (consumption >= stack.getCount() || !ingredient.test(stack)) continue;
			int amountToConsume = Math.min(amountLeft, stack.getCount() - consumption);
			amountLeft -= amountToConsume;
			consumptions.put(i, consumption + amountToConsume);
		}
		return amountLeft <= 0;
	}

	/**
	 * Milliseconds. I'm too lazy to type out nanoTime()/1000000 10 times ok
	 *
	 * @return Milliseconds
	 */
	public static long ms() {
		return System.nanoTime() / 1_000_000;
	}

	public static void printPlayerStates(@NotNull PlayerStateMap stateMap, @NotNull PlayerStateConnectionMap connectionMap) {
		if (!DebugCfg.get().debugPlayerMovement()) return;

		ParagliderMod.LOGGER.debug("All Player States: {} entries{}",
				stateMap.states().size(),
				stateMap.states().values().stream()
						.map(s -> "\n  " + s.id() + " : staminaDelta=" + s.staminaDelta() +
								", recoveryDelay=" + s.recoveryDelay() +
								(s.flags().isEmpty() ? "" : ", flags=[" + s.flags().stream()
										.map(Object::toString)
										.collect(Collectors.joining(", ")) + "]"))
						.collect(Collectors.joining()));

		StringBuilder stb = new StringBuilder("All Player State Connections: ")
				.append(connectionMap.connections().size()).append(" entries");

		for (var e : connectionMap.connections().entrySet()) {
			stb.append("\n  ").append(e.getKey());
			for (PlayerStateConnectionMap.Branch branch : e.getValue().branches()) {
				stb.append("\n    -> ").append(branch.state());
			}
			if (e.getValue().fallback() != null) {
				stb.append("\n    fallback: ").append(e.getValue().fallback());
			}
			stb.append('\n');
		}

		ParagliderMod.LOGGER.debug(stb.toString());
	}

	public static boolean canBreatheUnderwater(@NotNull Player player) {
		if (player.hasEffect(MobEffects.WATER_BREATHING)) return true;
		if (player.onGround()) {
			if (!player.canDrownInFluidType(player.getEyeInFluidType()) || player.level()
					.getBlockState(new BlockPos((int)player.getX(), (int)player.getEyeY(), (int)player.getZ()))
					.is(Blocks.BUBBLE_COLUMN)) return true;
		}

		var enchantments = player.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);

		ItemStack head = player.getItemBySlot(EquipmentSlot.HEAD);
		if (!head.isEmpty()) {
			if (head.getItem() == Items.TURTLE_HELMET) return true;
			else if (head.getEnchantmentLevel(enchantments.getOrThrow(Enchantments.RESPIRATION)) > 0) return true;
		}

		ItemStack feet = player.getItemBySlot(EquipmentSlot.FEET);
		return !feet.isEmpty() && feet.getEnchantmentLevel(enchantments.getOrThrow(Enchantments.DEPTH_STRIDER)) > 0;
	}

	public static @NotNull ParagliderItemCapability getCaps(@NotNull ItemStack stack) {
		var p = stack.getCapability(ParagliderItemCapability.CAPABILITY);
		return p != null ? p : ParagliderItemCapability.defaultImpl();
	}

	public static int countIngredient(Player player, Ingredient ingredient) {
		int count = 0;
		for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
			ItemStack stack = player.getInventory().getItem(i);
			if (stack.isEmpty() || !ingredient.test(stack)) continue;
			count += stack.getCount();
		}
		return count;
	}

	public static double refreshAttribute(Player player, Holder<Attribute> attribute, double value, ResourceLocation id) {
		AttributeInstance attrib = player.getAttribute(attribute);
		if (attrib == null) return 0;

		AttributeModifier prev = attrib.getModifier(id);
		if (prev != null) attrib.removeModifier(prev);
		if (value != 0) {
			attrib.addPermanentModifier(new AttributeModifier(id, value, AttributeModifier.Operation.ADD_VALUE));
		}

		return value - (prev != null ? prev.amount() : 0);
	}

	public static void playParagliderDeploySound(Player player) {
		player.level().playSound(player, player.getX(), player.getY(), player.getZ(),
				SoundEvents.ARMOR_EQUIP_LEATHER, SoundSource.PLAYERS,
				1.0f, .85f);
	}
}
