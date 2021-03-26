package tictim.paraglider.dialog;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraftforge.fml.network.PacketDistributor;
import tictim.paraglider.ParagliderMod;
import tictim.paraglider.network.DialogActionErrorMsg;
import tictim.paraglider.network.DialogActionResponseMsg;
import tictim.paraglider.network.ModNet;

public final class DialogActionArgs{
	private final DialogAction dialogAction;
	private final ServerPlayerEntity player;
	private final DialogContainer container;

	private boolean responded;

	public DialogActionArgs(DialogAction dialogAction, ServerPlayerEntity player, DialogContainer container){
		this.dialogAction = dialogAction;
		this.player = player;
		this.container = container;
	}

	public DialogContainer getContainer(){
		return container;
	}
	public ServerPlayerEntity getPlayer(){
		return player;
	}

	public void markCheated(){
		container.setCheated(true);
	}

	public void respond(boolean result){
		checkResponded();
		ParagliderMod.LOGGER.info("Responding "+result);
		ModNet.NET.send(
				PacketDistributor.PLAYER.with(() -> player),
				new DialogActionResponseMsg(dialogAction.getId(), result));
	}

	public void respond(Exception ex){
		checkResponded();
		ParagliderMod.LOGGER.info("Responding "+ex);
		ModNet.NET.send(
				PacketDistributor.PLAYER.with(() -> player),
				new DialogActionErrorMsg(dialogAction.getId(), ex));
	}

	private void checkResponded(){
		if(responded) throw new IllegalStateException("Are you sure you want to respond more than twice?");
		responded = true;
	}
}
