package tictim.paraglider.contents;


import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import tictim.paraglider.capabilities.PlayerMovement;
import tictim.paraglider.capabilities.ServerPlayerMovement;
import tictim.paraglider.dialog.Dialog;
import tictim.paraglider.dialog.DialogActionException;
import tictim.paraglider.utils.ParagliderUtils;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

// TODO would be cool if animations existed
public final class Dialogs{
	private Dialogs(){}

	public static void init(){}

	public static final Map<String, Dialog> DIALOGS_BY_NAME;

	public static final Dialog TEST = Dialog.builder("test").build(Contents.CONTAINERS);
	public static final Dialog TEST2 = Dialog.builder("test2")
			.trackInt("time", value -> (int)System.currentTimeMillis())
			.build(Contents.CONTAINERS);

	public static final Dialog GODDESS_STATUE_FULL = Dialog.builder("goddess_statue_full").build(Contents.CONTAINERS);
	public static final Dialog GODDESS_STATUE = Dialog.builder("goddess_statue")
			.addDialogAction("check_cheated", args -> args.respond(args.getContainer().isCheated()))
			.addDialogAction("check_spirit_orb", args -> args.respond(ParagliderUtils.canConsume(args.getPlayer(), Contents.SPIRIT_ORB.get(), 4)))
			.addDialogAction("give_heart_container", args -> {
				ServerPlayerEntity player = args.getPlayer();
				PlayerMovement m = player.getCapability(PlayerMovement.CAP).orElseThrow(() -> noCapability(player));
				if(ParagliderUtils.consume(player, Contents.SPIRIT_ORB.get(), 4)){
					args.respond(m.increaseHeartContainer());
				}else{
					args.markCheated();
					args.respond(false);
				}
			})
			.addDialogAction("give_stamina_vessel", args -> {
				ServerPlayerEntity player = args.getPlayer();
				PlayerMovement m = player.getCapability(PlayerMovement.CAP).orElseThrow(() -> noCapability(player));
				if(ParagliderUtils.consume(player, Contents.SPIRIT_ORB.get(), 4)){
					args.respond(m.increaseStaminaVessel());
				}else{
					args.markCheated();
					args.respond(false);
				}
			})
			.addDialogAction("check_heart_full", args -> {
				ServerPlayerEntity player = args.getPlayer();
				PlayerMovement m = player.getCapability(PlayerMovement.CAP).orElseThrow(() -> noCapability(player));
				args.respond(m.getHeartContainers()>=PlayerMovement.MAX_HEART_CONTAINERS);
			})
			.addDialogAction("check_stamina_full", args -> {
				ServerPlayerEntity player = args.getPlayer();
				PlayerMovement m = player.getCapability(PlayerMovement.CAP).orElseThrow(() -> noCapability(player));
				args.respond(m.getStaminaVessels()>=PlayerMovement.MAX_STAMINA_VESSELS);
			})
			.build(Contents.CONTAINERS);

	public static final Dialog HORNED_STATUE_FIRST_NO_VESSEL = Dialog.builder("horned_statue_first_no_vessel").build(Contents.CONTAINERS);

	public static final Dialog HORNED_STATUE_FIRST = Dialog.builder("horned_statue_first")
			.addDialogAction("check_cheated", args -> args.respond(args.getContainer().isCheated()))
			.addDialogAction("steal", args -> {
				ServerPlayerMovement m = ServerPlayerMovement.of(args.getPlayer());
				if(m==null) throw noCapability(args.getPlayer());
				if(m.getEssenceSoldToStatue()>0){
					args.markCheated();
					args.respond(false);
				}
				if(m.decreaseHeartContainer()||m.decreaseStaminaVessel()){
					m.increaseEssenceSoldToStatue();
					args.respond(true);
				}else throw new DialogActionException("No vessels to take");
			})
			.build(Contents.CONTAINERS);

	public static final Dialog HORNED_STATUE_SECOND = Dialog.builder("horned_statue_second")
			.addDialogAction("check_cheated", args -> args.respond(args.getContainer().isCheated()))
			// Removes essence for one Heart Container.
			// Error if user have no essence.
			// Responds false if user has essence but no space for extra Heart Container.
			// Marks as cheated and responds false if either give_heart_container, give_stamina_vessel, pay is executed before.
			.addDialogAction("give_heart_container", args -> {
				if(args.getContainer().storedFlags.containsKey("done")){
					args.markCheated();
					args.respond(false);
				}
				ServerPlayerMovement m = ServerPlayerMovement.of(args.getPlayer());
				if(m==null) throw noCapability(args.getPlayer());
				if(m.getEssenceSoldToStatue()>0){
					args.getContainer().storedFlags.put("done", true);
					if(m.increaseHeartContainer()){
						m.decreaseEssenceSoldToStatue();
						args.respond(true);
					}else{
						args.respond(false);
					}
				}else throw new DialogActionException("No essence to give");
			})
			// Removes essence for one Stamina Vessel.
			// Error if user have no essence.
			// Responds false if user has essence but no space for extra Stamina Vessel.
			// Marks as cheated and responds false if either give_heart_container, give_stamina_vessel, pay is executed before.
			.addDialogAction("give_stamina_vessel", args -> {
				if(args.getContainer().storedFlags.containsKey("done")){
					args.markCheated();
					args.respond(false);
				}
				ServerPlayerMovement m = ServerPlayerMovement.of(args.getPlayer());
				if(m==null) throw noCapability(args.getPlayer());
				if(m.getEssenceSoldToStatue()>0){
					args.getContainer().storedFlags.put("done", true);
					if(m.increaseStaminaVessel()){
						m.decreaseEssenceSoldToStatue();
						args.respond(true);
					}else{
						args.respond(false);
					}
				}else throw new DialogActionException("No essence to give");
			})
			// Removes essence for one Heart Container.
			// Error if user have no essence.
			// Marks as cheated and responds false if either give_heart_container, give_stamina_vessel, pay is executed before.
			.addDialogAction("pay", args -> {
				if(args.getContainer().storedFlags.containsKey("done")){
					args.markCheated();
					args.respond(false);
				}
				ServerPlayerEntity player = args.getPlayer();
				ServerPlayerMovement m = ServerPlayerMovement.of(player);
				if(m==null) throw noCapability(player);
				if(m.getEssenceSoldToStatue()>0){
					args.getContainer().storedFlags.put("done", true);
					player.inventory.placeItemBackInInventory(player.world, new ItemStack(Items.EMERALD, 5));
					args.respond(true);
				}else throw new DialogActionException("No essence to give");
			})
			.addDialogAction("check_fully_upgraded", args -> {
				ServerPlayerMovement m = ServerPlayerMovement.of(args.getPlayer());
				if(m==null) throw noCapability(args.getPlayer());
				args.respond(m.getHeartContainers()>=PlayerMovement.MAX_HEART_CONTAINERS
						&&m.getStaminaVessels()>=PlayerMovement.MAX_STAMINA_VESSELS);
			})
			.onContainerClosed((container, player) -> {
				if(player instanceof ServerPlayerEntity&&container.storedFlags.containsKey("done")){
					ModAdvancements.give((ServerPlayerEntity)player, ModAdvancements.STATUES_BARGAIN, "code_triggered");
				}
			})
			.build(Contents.CONTAINERS);

	public static final Dialog HORNED_STATUE = Dialog.builder("horned_statue")
			.addDialogAction("check_cheated", args -> args.respond(args.getContainer().isCheated()))
			.addDialogAction("has_essence", args -> {
				ServerPlayerMovement m = ServerPlayerMovement.of(args.getPlayer());
				if(m==null) throw noCapability(args.getPlayer());
				args.respond(m.getEssenceSoldToStatue()>0);
			})
			// Selling things

			// Removes one heart container and gives player 5 emeralds.
			// Returns whether or not this action can be repeated once more.
			// If removing vessel is failed, exception will be thrown.
			.addDialogAction("take_heart_container", args -> todo())
			// Removes one stamina vessel and gives player 5 emeralds.
			// Returns whether or not this action can be repeated once more.
			// If removing vessel is failed, exception will be thrown.
			.addDialogAction("take_stamina_vessel", args -> todo())
			// Returns whether or not take_heart_container will be successful. (e.g. no exception)
			.addDialogAction("can_take_heart_container", args -> {
				ServerPlayerMovement m = ServerPlayerMovement.of(args.getPlayer());
				if(m==null) throw noCapability(args.getPlayer());
				args.respond(m.getHeartContainers()>0);
			})
			// Returns whether or not take_stamina_vessel will be successful. (e.g. no exception)
			.addDialogAction("can_take_stamina_vessel", args -> {
				ServerPlayerMovement m = ServerPlayerMovement.of(args.getPlayer());
				if(m==null) throw noCapability(args.getPlayer());
				args.respond(m.getStaminaVessels()>0);
			})
			// Buying things

			// Removes 1 essence and 6 emeralds, in exchange for one heart container.
			// Returns whether or not this action can be repeated once more.
			// If any of transaction mention above fails with unknown reason, exception will be thrown.
			// Except when the player probably cheated himself, in that case, false will be returned.
			.addDialogAction("give_heart_container", args -> {
				ServerPlayerMovement m = ServerPlayerMovement.of(args.getPlayer());
				if(m==null) throw noCapability(args.getPlayer());
				if(m.getEssenceSoldToStatue()<=0) throw new DialogActionException("No essence to give");
				if(m.isHeartFullyUpgraded()) throw new DialogActionException("Can't give more heart");

				if(!ParagliderUtils.consume(args.getPlayer(), Items.EMERALD, 6)){
					args.markCheated();
					args.respond(false);
				}
				m.decreaseEssenceSoldToStatue();
				m.increaseHeartContainer();
				args.respond(true);
			})
			.addDialogAction("give_stamina_vessel", args -> {
				ServerPlayerMovement m = ServerPlayerMovement.of(args.getPlayer());
				if(m==null) throw noCapability(args.getPlayer());
				if(m.getEssenceSoldToStatue()<=0) throw new DialogActionException("No essence to give");
				if(m.isStaminaFullyUpgraded()) throw new DialogActionException("Can't give more stamina");

				if(!ParagliderUtils.consume(args.getPlayer(), Items.EMERALD, 6)){
					args.markCheated();
					args.respond(false);
				}
				m.decreaseEssenceSoldToStatue();
				m.increaseStaminaVessel();
				args.respond(true);
			})
			.addDialogAction("can_give_heart_container", args -> {
				ServerPlayerMovement m = ServerPlayerMovement.of(args.getPlayer());
				if(m==null) throw noCapability(args.getPlayer());
				args.respond(!m.isHeartFullyUpgraded());
			})
			.addDialogAction("can_give_stamina_vessel", args -> {
				ServerPlayerMovement m = ServerPlayerMovement.of(args.getPlayer());
				if(m==null) throw noCapability(args.getPlayer());
				args.respond(!m.isStaminaFullyUpgraded());
			})
			.addDialogAction("has_enough_money", args -> args.respond(ParagliderUtils.canConsume(args.getPlayer(), Items.EMERALD, 6)))
			.trackInt("essence", player -> {
				ServerPlayerMovement m = ServerPlayerMovement.of(player);
				return m==null ? 0 : m.getEssenceSoldToStatue();
			})
			.build(Contents.CONTAINERS);

	static{
		DIALOGS_BY_NAME = buildMap(TEST,
				TEST2,
				GODDESS_STATUE_FULL,
				GODDESS_STATUE,
				HORNED_STATUE_FIRST_NO_VESSEL,
				HORNED_STATUE_FIRST,
				HORNED_STATUE_SECOND,
				HORNED_STATUE);
	}

	private static Map<String, Dialog> buildMap(Dialog... dialogs){
		LinkedHashMap<String, Dialog> map = new LinkedHashMap<>();
		for(Dialog dialog : dialogs){
			map.put(dialog.getName(), dialog);
		}
		return Collections.unmodifiableMap(map);
	}

	private static DialogActionException noCapability(PlayerEntity player){
		return new DialogActionException(player.getGameProfile().getName()+" is missing capability");
	}
	private static void todo() throws DialogActionException{
		throw new DialogActionException("Not Implemented Yet");
	}
}
