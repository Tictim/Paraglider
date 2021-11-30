package tictim.paraglider.utils;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import tictim.paraglider.ModCfg;
import tictim.paraglider.capabilities.ServerPlayerMovement;
import tictim.paraglider.contents.Contents;

import javax.annotation.Nullable;

public final class ParagliderUtils{
	private ParagliderUtils(){}

	/**
	 * Give {@code stack} to {@code player}. If there's no more room left, the item will be dropped in the world as entity.<br>
	 * Accounts for ItemStacks with count more than its maximum stack size.
	 *
	 * @param player Who will receive the item
	 * @param stack  The item to be given
	 * @see net.minecraft.world.entity.player.Inventory#placeItemBackInInventory(ItemStack, boolean)
	 */
	public static void giveItem(Player player, ItemStack stack){
		if(player.level.isClientSide) return;
		while(!stack.isEmpty()){
			int slot = player.getInventory().getSlotWithRemainingSpace(stack);
			if(slot==-1) slot = player.getInventory().getFreeSlot();

			if(slot==-1){
				while(!stack.isEmpty()){
					ItemEntity itemEntity = new ItemEntity(player.level, player.getX(), player.getY(.5), player.getZ(), stack.split(stack.getMaxStackSize()));
					itemEntity.setPickUpDelay(40);
					itemEntity.setDeltaMovement(0, 0, 0);
					player.level.addFreshEntity(itemEntity);
				}
				break;
			}

			int count = stack.getMaxStackSize()-player.getInventory().getItem(slot).getCount();
			if(player.getInventory().add(slot, stack.split(count)))
				((ServerPlayer)player).connection.send(new ClientboundContainerSetSlotPacket(-2, 0, slot, player.getInventory().getItem(slot)));
		}
	}

	/**
	 * Attempt to take specific quantity of Heart Containers from the player. Does not continue the action if the player doesn't have enough.
	 *
	 * @param player   Who will provide the Heart Container
	 * @param quantity Quantity of Heart Containers to be taken
	 * @param simulate If {@code true}, the result of the action will not be applied to the player.
	 * @param effect   If {@code true}, effects such as particles will be spawned. Does nothing if {@code simulate == true}.
	 * @return Whether or not the action was successful
	 */
	public static boolean takeHeartContainers(Player player, int quantity, boolean simulate, boolean effect){
		if(quantity<=0) return true;
		ServerPlayerMovement m = ServerPlayerMovement.of(player);
		if(m==null) return false;
		if(m.getHeartContainers()<quantity) return false;
		if(!simulate&&!player.level.isClientSide){
			m.setHeartContainers(m.getHeartContainers()-quantity);
			// if(effect){}
		}
		return true;
	}

	/**
	 * Attempt to take specific quantity of Stamina Vessels from the player. Does not continue the action if the player doesn't have enough.
	 *
	 * @param player   Who will provide the Stamina Vessel
	 * @param quantity Quantity of Stamina Vessels to be taken
	 * @param simulate If {@code true}, the result of the action will not be applied to the player.
	 * @param effect   If {@code true}, effects such as particles will be spawned. Does nothing if {@code simulate == true}.
	 * @return Whether or not the action was successful
	 */
	public static boolean takeStaminaVessels(Player player, int quantity, boolean simulate, boolean effect){
		if(quantity<=0) return true;
		ServerPlayerMovement m = ServerPlayerMovement.of(player);
		if(m==null) return false;
		if(m.getStaminaVessels()<quantity) return false;
		if(!simulate&&!player.level.isClientSide){
			m.setStaminaVessels(m.getStaminaVessels()-quantity);
			// if(effect){}
		}
		return true;
	}

	/**
	 * Attempt to take specific quantity of Essences from the player. Does not continue the action if the player doesn't have enough.
	 *
	 * @param player   Who will provide the Essence
	 * @param quantity Quantity of Essences to be taken
	 * @param simulate If {@code true}, the result of the action will not be applied to the player.
	 * @param effect   If {@code true}, effects such as particles will be spawned. Does nothing if {@code simulate == true}.
	 * @return Whether or not the action was successful
	 */
	public static boolean takeEssences(Player player, int quantity, boolean simulate, boolean effect){
		if(quantity<=0) return true;
		ServerPlayerMovement m = ServerPlayerMovement.of(player);
		if(m==null) return false;
		if(m.getEssence()<quantity) return false;
		if(!simulate&&!player.level.isClientSide){
			m.setEssence(m.getEssence()-quantity);
			// if(effect){}
		}
		return true;
	}

	/**
	 * Attempt to give specific quantity of Heart Containers to the player. Does not continue the action if the player can't take all of it.
	 *
	 * @param player   Who will take the Heart Container
	 * @param quantity Quantity of Heart Containers to be given
	 * @param simulate If {@code true}, the result of the action will not be applied to the player.
	 * @param effect   If {@code true}, effects such as particles will be spawned. Does nothing if {@code simulate == true}.
	 * @return Whether or not the action was successful
	 */
	public static boolean giveHeartContainers(Player player, int quantity, boolean simulate, boolean effect){
		if(quantity<=0) return true;
		ServerPlayerMovement m = ServerPlayerMovement.of(player);
		if(m==null) return false;
		if(ModCfg.maxHeartContainers()-m.getHeartContainers()<quantity) return false;
		if(!simulate&&!player.level.isClientSide){
			m.setHeartContainers(m.getHeartContainers()+quantity);
			player.setHealth(player.getMaxHealth());
			if(effect) spawnParticle(player, ParticleTypes.HEART, 5+5*quantity);
		}
		return true;
	}

	/**
	 * Attempt to give specific quantity of Stamina Vessels to the player. Does not continue the action if the player can't take all of it.
	 *
	 * @param player   Who will take the Stamina Vessel
	 * @param quantity Quantity of Stamina Vessels to be given
	 * @param simulate If {@code true}, the result of the action will not be applied to the player.
	 * @param effect   If {@code true}, effects such as particles will be spawned. Does nothing if {@code simulate == true}.
	 * @return Whether or not the action was successful
	 */
	public static boolean giveStaminaVessels(Player player, int quantity, boolean simulate, boolean effect){
		if(quantity<=0) return true;
		ServerPlayerMovement m = ServerPlayerMovement.of(player);
		if(m==null) return false;
		if(ModCfg.maxStaminaVessels()-m.getStaminaVessels()<quantity) return false;
		if(!simulate&&!player.level.isClientSide){
			m.setStaminaVessels(m.getStaminaVessels()+quantity);
			m.setStamina(m.getMaxStamina());
			if(effect) spawnParticle(player, ParticleTypes.HAPPY_VILLAGER, 7+7*quantity);
		}
		return true;
	}

	/**
	 * Attempt to give specific quantity of Essences to the player. Does not continue the action if the player can't take all of it.
	 *
	 * @param player   Who will take the Essence
	 * @param quantity Quantity of Essences to be given
	 * @param simulate If {@code true}, the result of the action will not be applied to the player.
	 * @param effect   If {@code true}, effects such as particles will be spawned. Does nothing if {@code simulate == true}.
	 * @return Whether or not the action was successful
	 */
	public static boolean giveEssences(Player player, int quantity, boolean simulate, boolean effect){
		if(quantity<=0) return true;
		ServerPlayerMovement m = ServerPlayerMovement.of(player);
		if(m==null) return false;
		if(Integer.MAX_VALUE-m.getEssence()<quantity) return false;
		if(!simulate&&!player.level.isClientSide){
			m.setEssence(m.getEssence()+quantity);
			// if(effect){}
		}
		return true;
	}

	private static void spawnParticle(Player player, ParticleOptions particle, int count){
		ServerLevel world = player.level instanceof ServerLevel ? ((ServerLevel)player.level) : null;
		if(world==null) return;
		world.sendParticles(particle, player.getX(), player.getY(.5), player.getZ(), count, 1, 2, 1, 0);
	}

	/**
	 * Returns Heart Container, Stamina Vessel or nothing based on config value.
	 */
	@Nullable public static Item getAppropriateVessel(){
		return ModCfg.enableHeartContainers() ? Contents.HEART_CONTAINER.get() :
				ModCfg.enableSpiritOrbGens() ? Contents.SPIRIT_ORB.get() :
						null;
	}
}
