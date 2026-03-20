package tictim.paraglider.network;

import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;
import tictim.paraglider.ParagliderMod;
import tictim.paraglider.ParagliderUtils;
import tictim.paraglider.api.bargain.Bargain;
import tictim.paraglider.api.bargain.BargainResult;
import tictim.paraglider.api.movement.Movement;
import tictim.paraglider.bargain.BargainContext;
import tictim.paraglider.bargain.BargainHandler;
import tictim.paraglider.impl.movement.ServerPlayerMovement;
import tictim.paraglider.network.message.BargainEndMsg;
import tictim.paraglider.network.message.BargainMsg;
import tictim.paraglider.network.message.SetParaglidingMsg;

import static tictim.paraglider.ParagliderUtils.DIALOG_RNG;

public final class ServerPacketHandler {
	private ServerPacketHandler() {}

	public static void handleSetParagliding(SetParaglidingMsg msg, IPayloadContext ctx) {
		if (!(ctx.player() instanceof ServerPlayer player)) return;

		trace(Kind.MOVEMENT, player, msg);
		if (!(Movement.get(player) instanceof ServerPlayerMovement m)) return;

		m.setParagliding(msg.paragliding());
	}

	public static void handleBargain(BargainMsg msg, IPayloadContext ctx) {
		if (!(ctx.player() instanceof ServerPlayer player)) return;

		trace(Kind.BARGAIN, player, msg);
		BargainContext bargainContext = BargainHandler.getBargain(player);
		if (bargainContext == null || bargainContext.sessionId() != msg.sessionId()) return;

		Bargain bargain = bargainContext.bargains().get(msg.bargain());
		if (bargain == null) return;

		BargainResult result = bargain.bargain(player, false);
		if (result.isSuccess()) {
			Identifier advancement = bargainContext.advancement();
			if (advancement != null) ParagliderUtils.giveAdvancement(player, advancement, "bargain");
		}

		Component c = result.isSuccess() ?
				bargainContext.type().dialog().randomSuccessDialog(DIALOG_RNG, bargain.getBargainTags()) :
				bargainContext.type().dialog().randomFailDialog(DIALOG_RNG, bargain.getBargainTags(), result.failReasons());
		if (c != null) ParagliderNetwork.get().displayBargainDialog(bargainContext, c);
	}

	public static void handleBargainEnd(BargainEndMsg msg, IPayloadContext ctx) {
		if (!(ctx.player() instanceof ServerPlayer player)) return;

		trace(Kind.BARGAIN, player, msg);
		BargainContext bargainContext = BargainHandler.getBargain(player);
		if (bargainContext != null && bargainContext.sessionId() == msg.sessionId()) bargainContext.markFinished();
	}

	private static void trace(@NotNull Kind kind, @NotNull ServerPlayer player, @NotNull CustomPacketPayload msg) {
		if (kind.isTraceEnabled()) ParagliderMod.LOGGER.debug("Received {} from client {}", msg, player);
	}
}
