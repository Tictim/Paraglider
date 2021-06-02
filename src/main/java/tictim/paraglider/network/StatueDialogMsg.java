package tictim.paraglider.network;

import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.ITextComponent;

public final class StatueDialogMsg{
	public static StatueDialogMsg read(PacketBuffer buf){
		return new StatueDialogMsg(buf.readTextComponent());
	}

	public final ITextComponent text;

	public StatueDialogMsg(ITextComponent text){
		this.text = text;
	}

	public void write(PacketBuffer buf){
		buf.writeTextComponent(text);
	}
}
