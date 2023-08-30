package tictim.paraglider.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tictim.paraglider.api.vessel.VesselContainer;
import tictim.paraglider.bargain.BargainContext;
import tictim.paraglider.bargain.BargainHandler;
import tictim.paraglider.contents.BargainTypeRegistry;

import static com.mojang.brigadier.arguments.IntegerArgumentType.getInteger;
import static com.mojang.brigadier.arguments.IntegerArgumentType.integer;
import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;
import static net.minecraft.commands.arguments.EntityArgument.getPlayer;
import static net.minecraft.commands.arguments.EntityArgument.player;
import static net.minecraft.commands.arguments.ResourceLocationArgument.getId;
import static net.minecraft.commands.arguments.ResourceLocationArgument.id;
import static net.minecraft.commands.arguments.coordinates.BlockPosArgument.blockPos;
import static net.minecraft.commands.arguments.coordinates.BlockPosArgument.getBlockPos;
import static net.minecraft.commands.arguments.coordinates.Vec3Argument.getVec3;
import static net.minecraft.commands.arguments.coordinates.Vec3Argument.vec3;

public final class ParagliderCommands{
	private ParagliderCommands(){}

	public static LiteralArgumentBuilder<CommandSourceStack> register(){
		return literal("paraglider")
				.then(queryVessel())
				.then(setVessel(SetType.set))
				.then(setVessel(SetType.give))
				.then(setVessel(SetType.take))
				.then(bargain());
	}

	private static LiteralArgumentBuilder<CommandSourceStack> queryVessel(){
		return literal("query")
				.requires(s -> s.hasPermission(1))
				.then(literal(ResourceType.HEART.name)
						.then(argument("player", player())
								.executes(ctx -> ResourceType.HEART.tell(ctx.getSource(), getPlayer(ctx, "player")))))
				.then(literal(ResourceType.STAMINA.name)
						.then(argument("player", player())
								.executes(ctx -> ResourceType.STAMINA.tell(ctx.getSource(), getPlayer(ctx, "player")))))
				.then(literal(ResourceType.ESSENCE.name)
						.then(argument("player", player())
								.executes(ctx -> ResourceType.ESSENCE.tell(ctx.getSource(), getPlayer(ctx, "player")))));
	}

	private static LiteralArgumentBuilder<CommandSourceStack> setVessel(@NotNull SetType type){
		return literal(type.name())
				.requires(s -> s.hasPermission(2))
				.then(literal(ResourceType.HEART.name)
						.then(argument("player", player())
								.then(argument("amount", integer(0))
										.executes(ctx -> ResourceType.HEART.run(
												ctx.getSource(),
												getPlayer(ctx, "player"),
												getInteger(ctx, "amount"),
												type)))))
				.then(literal(ResourceType.STAMINA.name)
						.then(argument("player", player())
								.then(argument("amount", integer(0))
										.executes(ctx -> ResourceType.STAMINA.run(
												ctx.getSource(),
												getPlayer(ctx, "player"),
												getInteger(ctx, "amount"),
												type)))))
				.then(literal(ResourceType.ESSENCE.name)
						.then(argument("player", player())
								.then(argument("amount", integer(0))
										.executes(ctx -> ResourceType.ESSENCE.run(
												ctx.getSource(),
												getPlayer(ctx, "player"),
												getInteger(ctx, "amount"),
												type)))));
	}

	private static LiteralArgumentBuilder<CommandSourceStack> bargain(){
		return literal("bargain")
				.requires(s -> s.hasPermission(2))
				.then(literal("start")
						.then(argument("player", player())
								.then(argument("bargainType", id())
										.executes(ctx -> startBargain(ctx.getSource(),
												getPlayer(ctx, "player"),
												getId(ctx, "bargainType"),
												null, null, null))
										.then(argument("pos", blockPos())
												.executes(ctx -> startBargain(ctx.getSource(),
														getPlayer(ctx, "player"),
														getId(ctx, "bargainType"),
														getBlockPos(ctx, "pos"),
														null, null))
												.then(argument("advancement", id())
														.executes(ctx -> startBargain(ctx.getSource(),
																getPlayer(ctx, "player"),
																getId(ctx, "bargainType"),
																getBlockPos(ctx, "pos"),
																getId(ctx, "advancement"),
																null))
														.then(argument("lookAt", vec3())
																.executes(ctx -> startBargain(ctx.getSource(),
																		getPlayer(ctx, "player"),
																		getId(ctx, "bargainType"),
																		getBlockPos(ctx, "pos"),
																		getId(ctx, "advancement"),
																		getVec3(ctx, "lookAt")))))))))
				.then(literal("end")
						.then(argument("player", player())
								.executes(ctx -> endBargain(ctx.getSource(), getPlayer(ctx, "player")))));
	}

	private static int startBargain(@NotNull CommandSourceStack source,
	                                @NotNull ServerPlayer player,
	                                @NotNull ResourceLocation bargainType,
	                                @Nullable BlockPos pos,
	                                @Nullable ResourceLocation advancement,
	                                @Nullable Vec3 lookAt){
		if(BargainTypeRegistry.get().getFromID(player.serverLevel(), bargainType)==null){
			source.sendFailure(Component.translatable("commands.paraglider.bargain.start.invalid_bargain_type", bargainType));
			return -1;
		}
		if(BargainHandler.initiate(player, bargainType, pos, advancement, lookAt)){
			source.sendSuccess(() -> Component.translatable("commands.paraglider.bargain.start.success", player.getDisplayName(), bargainType), true);
			return 1;
		}else{
			source.sendFailure(Component.translatable("commands.paraglider.bargain.start.no_bargain", player.getDisplayName(), bargainType, pos));
			return 0;
		}
	}

	private static int endBargain(@NotNull CommandSourceStack source, @NotNull ServerPlayer player){
		BargainContext bargain = BargainHandler.getBargain(player);
		if(bargain!=null){
			if(bargain.isFinished()){
				source.sendFailure(Component.translatable("command.paraglider.bargain.end.already_finished", player.getDisplayName()));
				return 0;
			}
			source.sendSuccess(() -> Component.translatable("command.paraglider.bargain.end.success", player.getDisplayName()), true);
			bargain.markFinished();
			return 1;
		}else{
			source.sendFailure(Component.translatable("command.paraglider.bargain.end.no_bargain", player.getDisplayName()));
			return -1;
		}
	}

	private enum SetType{
		set, give, take
	}

	private enum ResourceType{
		HEART("heart_container"),
		STAMINA("stamina_vessel"),
		ESSENCE("essence");

		private final String name;

		private final String getResult;
		private final String setSuccess;
		private final String setNoChange;
		private final String giveSuccess;
		private final String takeSuccess;

		private final String setTooHigh;
		private final String setTooLow;
		private final String setFail;
		private final String giveFail;
		private final String takeFail;

		ResourceType(String name){
			this.name = name;
			this.getResult = "commands.paraglider.get."+name+".result";
			this.setSuccess = "commands.paraglider.set."+name+".success";
			this.setNoChange = "commands.paraglider.set."+name+".no_change";
			this.giveSuccess = "commands.paraglider.give."+name+".success";
			this.takeSuccess = "commands.paraglider.take."+name+".success";

			this.setTooHigh = "commands.paraglider.set."+name+".too_high";
			this.setTooLow = "commands.paraglider.set."+name+".too_low";
			this.setFail = "commands.paraglider.set."+name+".fail";
			this.giveFail = "commands.paraglider.give."+name+".fail";
			this.takeFail = "commands.paraglider.take."+name+".fail";
		}

		private int tell(@NotNull CommandSourceStack source, @NotNull Player player){
			VesselContainer vessels = VesselContainer.get(player);
			int value = switch(this){
				case HEART -> vessels.heartContainer();
				case STAMINA -> vessels.staminaVessel();
				case ESSENCE -> vessels.essence();
			};
			source.sendSuccess(() -> Component.translatable(getResult, player.getDisplayName(), value), false);
			return value;
		}

		private int run(@NotNull CommandSourceStack source,
		                @NotNull Player player,
		                int amount,
		                @NotNull SetType type){
			VesselContainer vessels = VesselContainer.get(player);
			switch(type){
				case set -> {
					switch(set(vessels, amount, false, true)){
						case OK -> {
							source.sendSuccess(() -> Component.translatable(setSuccess, player.getDisplayName(), amount), true);
							return 1;
						}
						case NO_CHANGE -> {
							source.sendSuccess(() -> Component.translatable(setNoChange, player.getDisplayName(), amount), true);
							return 0;
						}
						case TOO_HIGH -> {
							source.sendFailure(Component.translatable(setTooHigh, amount));
							return -1;
						}
						case TOO_LOW -> {
							source.sendFailure(Component.translatable(setTooLow, amount));
							return -1;
						}
						case FAIL -> {
							source.sendFailure(Component.translatable(setFail));
							return -1;
						}
					}
				}
				case give -> {
					if(give(vessels, amount, true, false)!=amount){
						source.sendFailure(Component.translatable(giveFail, player.getDisplayName(), amount));
						return 0;
					}
					source.sendSuccess(() -> Component.translatable(giveSuccess, player.getDisplayName(), amount), true);
					return give(vessels, amount, false, true);
				}
				case take -> {
					if(take(vessels, amount, true, false)!=amount){
						source.sendFailure(Component.translatable(takeFail, player.getDisplayName(), amount));
						return 0;
					}
					source.sendSuccess(() -> Component.translatable(takeSuccess, player.getDisplayName(), amount), true);
					return take(vessels, amount, false, true);
				}
			}
			throw new IllegalStateException("Unreachable");
		}

		@NotNull private VesselContainer.SetResult set(@NotNull VesselContainer vessels, int amount, boolean simulate, boolean playEffect){
			return switch(this){
				case HEART -> vessels.setHeartContainer(amount, simulate, playEffect);
				case STAMINA -> vessels.setStaminaVessel(amount, simulate, playEffect);
				case ESSENCE -> vessels.setEssence(amount, simulate, playEffect);
			};
		}

		private int give(@NotNull VesselContainer vessels, int amount, boolean simulate, boolean playEffect){
			return switch(this){
				case HEART -> vessels.giveHeartContainers(amount, simulate, playEffect);
				case STAMINA -> vessels.giveStaminaVessels(amount, simulate, playEffect);
				case ESSENCE -> vessels.giveEssences(amount, simulate, playEffect);
			};
		}

		private int take(@NotNull VesselContainer vessels, int amount, boolean simulate, boolean playEffect){
			return switch(this){
				case HEART -> vessels.takeHeartContainers(amount, simulate, playEffect);
				case STAMINA -> vessels.takeStaminaVessels(amount, simulate, playEffect);
				case ESSENCE -> vessels.takeEssences(amount, simulate, playEffect);
			};
		}
	}
}
