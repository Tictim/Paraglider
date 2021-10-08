package tictim.paraglider.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public record BargainMsg(ResourceLocation bargain){
	public static BargainMsg read(FriendlyByteBuf buf){
		return new BargainMsg(buf.readResourceLocation());
	}

	public void write(FriendlyByteBuf buf){
		buf.writeResourceLocation(bargain);
	}
}
