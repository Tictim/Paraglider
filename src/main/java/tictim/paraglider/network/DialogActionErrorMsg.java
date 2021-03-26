package tictim.paraglider.network;

import net.minecraft.network.PacketBuffer;
import tictim.paraglider.dialog.DialogActionException;

public class DialogActionErrorMsg{
	public static DialogActionErrorMsg read(PacketBuffer buf){
		return new DialogActionErrorMsg(buf.readString(), buf.readString());
	}

	public final String id;
	public final String error;

	public DialogActionErrorMsg(String id, Exception ex){
		this(id, ex instanceof DialogActionException ? ex.getMessage() : ex.toString());
	}
	public DialogActionErrorMsg(String id, String error){
		this.id = id;
		this.error = error;
	}

	public void write(PacketBuffer buf){
		buf.writeString(id);
		buf.writeString(error);
	}
}
