package tictim.paraglider.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;

public record StatueDialogMsg(Component text){
	public static StatueDialogMsg read(FriendlyByteBuf buf){
		return new StatueDialogMsg(buf.readComponent());
	}

	public void write(FriendlyByteBuf buf){
		buf.writeComponent(text);
	}
}
