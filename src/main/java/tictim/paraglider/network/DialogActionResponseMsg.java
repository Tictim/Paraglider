package tictim.paraglider.network;

import net.minecraft.network.PacketBuffer;

public class DialogActionResponseMsg{
	public static DialogActionResponseMsg read(PacketBuffer buf){
		return new DialogActionResponseMsg(buf.readString(), buf.readBoolean());
	}

	public final String id;
	public final boolean result;

	public DialogActionResponseMsg(String id, boolean result){
		this.id = id;
		this.result = result;
	}

	public void write(PacketBuffer buf){
		buf.writeString(id);
		buf.writeBoolean(result);
	}
}
