// The reason a goddamn util class is in main package is because (1) It's the only util class so no reason to make one
// but more importantly (2) Architectury's package constraint introduced by @ExpectPlatform is so cringe that I HAVE to
// do this if I want to have some sensible package structure
package tictim.paraglider;

import com.mojang.blaze3d.platform.InputConstants;
import dev.architectury.injectables.annotations.ExpectPlatform;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectAVLTreeMap;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.advancements.Advancement;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.ServerAdvancementManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tictim.paraglider.api.bargain.Bargain;
import tictim.paraglider.api.movement.Movement;
import tictim.paraglider.api.movement.PlayerState;
import tictim.paraglider.api.stamina.Stamina;
import tictim.paraglider.api.vessel.VesselContainer;
import tictim.paraglider.bargain.preview.QuantifiedIngredient;
import tictim.paraglider.client.ParagliderClientSettings;
import tictim.paraglider.config.FeatureCfg;
import tictim.paraglider.contents.BargainTypeRegistry;
import tictim.paraglider.contents.Contents;
import tictim.paraglider.impl.movement.ClientPlayerMovement;
import tictim.paraglider.impl.movement.PlayerMovement;
import tictim.paraglider.impl.movement.RemotePlayerMovement;
import tictim.paraglider.impl.movement.ServerPlayerMovement;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public final class ParagliderUtils{
	private ParagliderUtils(){}

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
	public static void giveItem(@NotNull Player player, @NotNull ItemStack stack){
		if(player.level().isClientSide) return;
		while(!stack.isEmpty()){
			int slot = player.getInventory().getSlotWithRemainingSpace(stack);
			if(slot==-1) slot = player.getInventory().getFreeSlot();

			if(slot==-1){
				while(!stack.isEmpty()){
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

			int count = stack.getMaxStackSize()-player.getInventory().getItem(slot).getCount();
			if(player.getInventory().add(slot, stack.split(count))&&player instanceof ServerPlayer serverPlayer)
				serverPlayer.connection.send(
						new ClientboundContainerSetSlotPacket(-2, 0, slot, player.getInventory().getItem(slot)));
		}
	}

	/**
	 * Returns Heart Container, Stamina Vessel or nothing based on config value.
	 */
	@Nullable public static Item getAppropriateVessel(){
		FeatureCfg cfg = FeatureCfg.get();
		return cfg.enableHeartContainers() ? Contents.get().heartContainer() :
				cfg.enableStaminaVessels() ? Contents.get().staminaVessel() :
						null;
	}

	private static final UUID EXHAUSTION_ATTRIBUTE_UUID = UUID.fromString("65ed2ca4-ceb3-4521-8552-73006dcba58d");
	private static final double EXHAUSTION_AMOUNT = -0.30;

	public static void addExhaustion(@NotNull LivingEntity entity){
		AttributeInstance attr = entity.getAttribute(Attributes.MOVEMENT_SPEED);
		if(attr==null||attr.getModifier(EXHAUSTION_ATTRIBUTE_UUID)!=null) return;
		attr.addTransientModifier(new AttributeModifier(
				EXHAUSTION_ATTRIBUTE_UUID,
				"Exhaustion",
				EXHAUSTION_AMOUNT,
				AttributeModifier.Operation.MULTIPLY_TOTAL));
	}

	public static void removeExhaustion(@NotNull LivingEntity entity){
		AttributeInstance attr = entity.getAttribute(Attributes.MOVEMENT_SPEED);
		if(attr==null) return;
		attr.removeModifier(EXHAUSTION_ATTRIBUTE_UUID);
	}

	@SuppressWarnings("UnusedReturnValue")
	public static boolean giveAdvancement(@NotNull ServerPlayer player,
	                                      @NotNull ResourceLocation advancementName,
	                                      @NotNull String criterion){
		PlayerAdvancements advancements = player.getAdvancements();
		ServerAdvancementManager advancementManager = player.server.getAdvancements();
		Advancement advancement = advancementManager.getAdvancement(advancementName);
		return advancement!=null&&advancements.award(advancement, criterion);
	}

	public static void damageItemWithoutBreaking(@NotNull Player player, @NotNull ItemStack stack){
		int prevCount = stack.getCount();
		stack.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(InteractionHand.MAIN_HAND));
		if(stack.getCount()<prevCount){
			stack.setCount(prevCount);
			stack.setDamageValue(stack.getMaxDamage());
		}
	}

	public static void checkBargainRecipes(@NotNull MinecraftServer server){
		List<Bargain> recipes = server.getRecipeManager().getAllRecipesFor(Contents.get().bargainRecipeType());
		BargainTypeRegistry typeRegistry = BargainTypeRegistry.get();
		Map<ResourceLocation, List<ResourceLocation>> missingBargainTypes = new Object2ObjectAVLTreeMap<>();
		int count = 0;
		for(Bargain b : recipes){
			ResourceLocation bargainType = b.getBargainType();
			if(typeRegistry.getFromID(server, Objects.requireNonNull(bargainType))==null){
				missingBargainTypes.computeIfAbsent(bargainType, s -> new ArrayList<>())
						.add(Objects.requireNonNull(b.getId()));
				count++;
			}
		}
		if(count>0){
			ParagliderMod.LOGGER.error("Found {} issues in bargain recipes:\n  {}", count,
					missingBargainTypes.entrySet().stream()
							.map(e -> "Cannot resolve bargain type "+e.getKey()+(e.getValue().size()==1 ?
									" for bargain recipe "+e.getValue().get(0) :
									" for bargain recipes "+e.getValue().stream()
											.map(Object::toString)
											.collect(Collectors.joining(", "))))
							.collect(Collectors.joining("\n  ")));
		}
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
	                                           @NotNull Int2IntOpenHashMap consumptions){
		int amountLeft = ingredient.quantity();
		for(int i = 0; amountLeft>0&&i<inventory.getContainerSize(); i++){
			ItemStack stack = inventory.getItem(i);
			int consumption = consumptions.get(i);
			// already consumed entirety of the stack, or not a valid input
			if(consumption>=stack.getCount()||!ingredient.test(stack)) continue;
			int amountToConsume = Math.min(amountLeft, stack.getCount()-consumption);
			amountLeft -= amountToConsume;
			consumptions.put(i, consumption+amountToConsume);
		}
		return amountLeft<=0;
	}

	private static final DecimalFormat PERCENTAGE = new DecimalFormat("#.#%");

	public static void addDebugText(@NotNull Player p, @NotNull List<String> list){
		Movement movement = Movement.get(p);
		Stamina stamina = Stamina.get(p);
		VesselContainer vessels = VesselContainer.get(p);
		ParagliderClientSettings clientSettings = ParagliderClientSettings.get();

		if(!list.isEmpty()) list.add("");
		PlayerState state = movement.state();
		list.add("State: "+state.id()+" (staminaDelta="+state.staminaDelta()+", recoveryDelay="+state.recoveryDelay()+(
				state.flags().isEmpty() ? "" : ", flags="+state.flags().stream()
						.map(Object::toString)
						.collect(Collectors.joining(" "))
		)+")");
		list.add((stamina.isDepleted() ? ChatFormatting.RED : "")+"Stamina: "+stamina.stamina()+" / "+stamina.maxStamina());
		list.add(vessels.staminaVessel()+" Stamina Vessels, "+vessels.heartContainer()+" Heart Containers");
		list.add(movement.recoveryDelay()+" Recovery Delay");
		list.add("Stamina Wheel X: "+PERCENTAGE.format(clientSettings.staminaWheelX())+
				", Stamina Wheel Y: "+PERCENTAGE.format(clientSettings.staminaWheelY()));
	}

	/**
	 * Milliseconds. I'm too lazy to type out nanoTime()/1000000 10 times ok
	 *
	 * @return Milliseconds
	 */
	public static long ms(){
		return System.nanoTime()/1_000_000;
	}

	@NotNull public static PlayerMovement createPlayerMovement(@NotNull Player player){
		return player instanceof ServerPlayer serverPlayer ? new ServerPlayerMovement(serverPlayer) :
				isClient() ? ClientImpl.createPlayerMovement(player) :
						new RemotePlayerMovement(player);
	}

	// some part of those methods have to use shitty forge api so uh uhh

	@ExpectPlatform
	public static boolean canBreatheUnderwater(@NotNull Player player){
		throw new AssertionError();
	}

	@ExpectPlatform
	public static boolean hasTag(@NotNull Block block, @NotNull TagKey<Block> tagKey){
		throw new AssertionError();
	}

	@ExpectPlatform
	@NotNull public static Item getItem(@NotNull ResourceLocation id){
		throw new AssertionError();
	}

	@ExpectPlatform
	@Nullable public static ResourceLocation getKey(@NotNull Item item){
		throw new AssertionError();
	}

	@ExpectPlatform
	@NotNull public static Block getBlock(@NotNull ResourceLocation id){
		throw new AssertionError();
	}

	@ExpectPlatform
	public static void forRemainingItem(@NotNull ItemStack stack, @NotNull Consumer<@NotNull ItemStack> forRemainingItem){
		throw new AssertionError();
	}

	@ExpectPlatform
	@Environment(EnvType.CLIENT)
	@NotNull public static InputConstants.Key getKey(@NotNull KeyMapping keyMapping){
		throw new AssertionError();
	}

	@ExpectPlatform
	@Environment(EnvType.CLIENT)
	public static boolean isActiveAndMatches(@NotNull KeyMapping keyMapping, @NotNull InputConstants.Key key){
		throw new AssertionError();
	}

	@ExpectPlatform
	private static boolean isClient(){
		throw new AssertionError();
	}

	private static final class ClientImpl{
		@NotNull static PlayerMovement createPlayerMovement(@NotNull Player player){
			return player instanceof LocalPlayer localPlayer ? new ClientPlayerMovement(localPlayer) :
					new RemotePlayerMovement(player);
		}
	}
}
