package tictim.paraglider.bargain;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public record BargainCatalog(
		@NotNull ResourceLocation bargain,
		int @NotNull [] demandCounts,
		boolean canBargain
){
	@NotNull public static BargainCatalog read(@NotNull FriendlyByteBuf buffer){
		return new BargainCatalog(buffer.readResourceLocation(), buffer.readVarIntArray(), buffer.readBoolean());
	}

	public int getCount(int index){
		return index<0||index>=demandCounts.length ? 0 : demandCounts[index];
	}

	public void write(@NotNull FriendlyByteBuf buffer){
		buffer.writeResourceLocation(bargain);
		buffer.writeVarIntArray(demandCounts);
		buffer.writeBoolean(canBargain);
	}
}
