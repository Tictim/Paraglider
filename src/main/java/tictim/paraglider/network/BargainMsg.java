package tictim.paraglider.network;

import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;

public class BargainMsg{
	public static BargainMsg read(PacketBuffer buf){
		return new BargainMsg(buf.readResourceLocation());
	}

	public final ResourceLocation bargain;

	public BargainMsg(ResourceLocation bargain){
		this.bargain = bargain;
	}

	public void write(PacketBuffer buf){
		buf.writeResourceLocation(bargain);
	}
}
