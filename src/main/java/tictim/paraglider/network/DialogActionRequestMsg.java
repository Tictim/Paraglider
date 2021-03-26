package tictim.paraglider.network;

import net.minecraft.network.PacketBuffer;

public class DialogActionRequestMsg{
	public static DialogActionRequestMsg read(PacketBuffer buf){
		return new DialogActionRequestMsg(buf.readString());
	}

	public final String id;

	public DialogActionRequestMsg(String id){
		this.id = id;
	}

	public void write(PacketBuffer buf){
		buf.writeString(id);
	}
}
