package tictim.paraglider.fabric.event;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import tictim.paraglider.ParagliderMod;
import tictim.paraglider.api.movement.Movement;
import tictim.paraglider.bargain.BargainHandler;
import tictim.paraglider.fabric.impl.PlayerMovementAccess;
import tictim.paraglider.impl.movement.PlayerMovement;
import tictim.paraglider.network.ParagliderNetwork;

import static tictim.paraglider.api.movement.ParagliderPlayerStates.Flags.FLAG_PARAGLIDING;

public final class ParagliderEventHandler{
	private ParagliderEventHandler(){}

	public static InteractionResult beforeInteraction(Player player){
		return Movement.get(player).state().has(FLAG_PARAGLIDING) ? InteractionResult.FAIL : InteractionResult.PASS;
	}

	public static InteractionResultHolder<ItemStack> beforeUseItem(Player player, InteractionHand hand){
		ItemStack stack = player.getItemInHand(hand);
		return Movement.get(player).state().has(FLAG_PARAGLIDING) ? InteractionResultHolder.fail(stack) : InteractionResultHolder.pass(stack);
	}

	public static void onPlayerCopy(ServerPlayer oldPlayer, ServerPlayer newPlayer, boolean alive){
		PlayerMovement mOld = ((PlayerMovementAccess)oldPlayer).paragliderPlayerMovement();
		PlayerMovement mNew = ((PlayerMovementAccess)newPlayer).paragliderPlayerMovement();
		mNew.copyFrom(mOld);
		if(!alive) mNew.stamina().setStamina(mNew.stamina().maxStamina());
	}

	public static void onStartTracking(Entity tracking, ServerPlayer player){
		if(!(tracking instanceof Player trackingPlayer)) return;
		ParagliderNetwork.get().syncRemoteMovement(trackingPlayer, player, Movement.get(trackingPlayer).state().id());
	}

	public static void afterServerTick(){
		BargainHandler.update();
	}

	public static void onLogin(ServerGamePacketListenerImpl listener){
		ParagliderNetwork.get().syncStateMap(listener.player, ParagliderMod.instance().getLocalPlayerStateMap());
	}
}
