package tictim.paraglider.network;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.jetbrains.annotations.NotNull;
import tictim.paraglider.ParagliderMod;
import tictim.paraglider.ParagliderUtils;
import tictim.paraglider.api.bargain.Bargain;
import tictim.paraglider.api.bargain.BargainResult;
import tictim.paraglider.bargain.BargainContext;
import tictim.paraglider.bargain.BargainHandler;
import tictim.paraglider.network.message.BargainEndMsg;
import tictim.paraglider.network.message.BargainMsg;
import tictim.paraglider.network.message.Msg;

import static tictim.paraglider.ParagliderUtils.DIALOG_RNG;

public final class ServerPacketHandler{
	private ServerPacketHandler(){}

	public static void handleBargain(@NotNull ServerPlayer player, @NotNull BargainMsg msg){
		trace(Kind.BARGAIN, player, msg);
		BargainContext ctx = BargainHandler.getBargain(player);
		if(ctx==null||ctx.sessionId()!=msg.sessionId()) return;

		RecipeHolder<Bargain> bargainHolder = ctx.bargains().get(msg.bargain());
		if(bargainHolder==null) return;

		Bargain bargain = bargainHolder.value();
		BargainResult result = bargain.bargain(player, false);
		if(result.isSuccess()){
			ResourceLocation advancement = ctx.advancement();
			if(advancement!=null) ParagliderUtils.giveAdvancement(player, advancement, "bargain");
		}

		Component c = result.isSuccess() ?
				ctx.type().dialog().randomSuccessDialog(DIALOG_RNG, bargain.getBargainTags()) :
				ctx.type().dialog().randomFailDialog(DIALOG_RNG, bargain.getBargainTags(), result.failReasons());
		if(c!=null) ParagliderNetwork.get().displayBargainDialog(ctx, c);
	}

	public static void handleBargainEnd(@NotNull ServerPlayer player, @NotNull BargainEndMsg msg){
		trace(Kind.BARGAIN, player, msg);
		BargainContext ctx = BargainHandler.getBargain(player);
		if(ctx!=null&&ctx.sessionId()==msg.sessionId()) ctx.markFinished();
	}

	private static void trace(@NotNull Kind kind, @NotNull ServerPlayer player, @NotNull Msg msg){
		if(kind.isTraceEnabled()) ParagliderMod.LOGGER.debug("Received {} from client {}", msg, player);
	}
}
