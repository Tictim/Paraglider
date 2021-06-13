package tictim.paraglider.utils;

import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.SSetSlotPacket;
import net.minecraft.particles.IParticleData;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.world.server.ServerWorld;
import tictim.paraglider.ModCfg;
import tictim.paraglider.capabilities.ServerPlayerMovement;

public final class ParagliderUtils{
	private ParagliderUtils(){}

	/**
	 * Give {@code stack} to {@code player}. If there's no more room left, the item will be dropped in the world as entity.<br>
	 * Accounts for ItemStacks with count more than its maximum stack size.
	 *
	 * @param player Who will receive the item
	 * @param stack  The item to be given
	 */
	public static void giveItem(PlayerEntity player, ItemStack stack){
		if(player.world.isRemote) return;
		while(!stack.isEmpty()){
			int slot = player.inventory.storeItemStack(stack);
			if(slot==-1) slot = player.inventory.getFirstEmptyStack();

			if(slot==-1){
				while(!stack.isEmpty()){
					ItemEntity itemEntity = new ItemEntity(player.world, player.getPosX(), player.getPosYHeight(.5), player.getPosZ(), stack.split(stack.getMaxStackSize()));
					itemEntity.setPickupDelay(40);
					itemEntity.setMotion(0, 0, 0);
					player.world.addEntity(itemEntity);
				}
				break;
			}

			int count = stack.getMaxStackSize()-player.inventory.getStackInSlot(slot).getCount();
			if(player.inventory.add(slot, stack.split(count)))
				((ServerPlayerEntity)player).connection.sendPacket(new SSetSlotPacket(-2, slot, player.inventory.getStackInSlot(slot)));
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
	public static boolean takeHeartContainers(PlayerEntity player, int quantity, boolean simulate, boolean effect){
		if(quantity<=0) return true;
		ServerPlayerMovement m = ServerPlayerMovement.of(player);
		if(m==null) return false;
		if(m.getHeartContainers()<quantity) return false;
		if(!simulate&&!player.world.isRemote){
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
	public static boolean takeStaminaVessels(PlayerEntity player, int quantity, boolean simulate, boolean effect){
		if(quantity<=0) return true;
		ServerPlayerMovement m = ServerPlayerMovement.of(player);
		if(m==null) return false;
		if(m.getStaminaVessels()<quantity) return false;
		if(!simulate&&!player.world.isRemote){
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
	public static boolean takeEssences(PlayerEntity player, int quantity, boolean simulate, boolean effect){
		if(quantity<=0) return true;
		ServerPlayerMovement m = ServerPlayerMovement.of(player);
		if(m==null) return false;
		if(m.getEssence()<quantity) return false;
		if(!simulate&&!player.world.isRemote){
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
	public static boolean giveHeartContainers(PlayerEntity player, int quantity, boolean simulate, boolean effect){
		if(quantity<=0) return true;
		ServerPlayerMovement m = ServerPlayerMovement.of(player);
		if(m==null) return false;
		if(ModCfg.maxHeartContainers()-m.getHeartContainers()<quantity) return false;
		if(!simulate&&!player.world.isRemote){
			m.setHeartContainers(m.getHeartContainers()+quantity);
			player.setHealth(player.getMaxHealth()+quantity);
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
	public static boolean giveStaminaVessels(PlayerEntity player, int quantity, boolean simulate, boolean effect){
		if(quantity<=0) return true;
		ServerPlayerMovement m = ServerPlayerMovement.of(player);
		if(m==null) return false;
		if(ModCfg.maxStaminaVessels()-m.getStaminaVessels()<quantity) return false;
		if(!simulate&&!player.world.isRemote){
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
	public static boolean giveEssences(PlayerEntity player, int quantity, boolean simulate, boolean effect){
		if(quantity<=0) return true;
		ServerPlayerMovement m = ServerPlayerMovement.of(player);
		if(m==null) return false;
		if(Integer.MAX_VALUE-m.getEssence()<quantity) return false;
		if(!simulate&&!player.world.isRemote){
			m.setEssence(m.getEssence()+quantity);
			// if(effect){}
		}
		return true;
	}


	private static void spawnParticle(PlayerEntity player, IParticleData particle, int count){
		ServerWorld world = player.world instanceof ServerWorld ? ((ServerWorld)player.world) : null;
		if(world==null) return;
		world.spawnParticle(particle, player.getPosX(), player.getPosYHeight(.5), player.getPosZ(), count, 1, 2, 1, 0);
	}
}
