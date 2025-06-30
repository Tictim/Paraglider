package tictim.paraglider.client;

import net.minecraft.ChatFormatting;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import tictim.paraglider.ParagliderMod;
import tictim.paraglider.api.movement.Movement;
import tictim.paraglider.api.movement.PlayerState;
import tictim.paraglider.api.stamina.Stamina;
import tictim.paraglider.api.vessel.VesselContainer;
import tictim.paraglider.client.render.InGameStaminaWheelRenderer;
import tictim.paraglider.client.screen.DisableStaminaRender;
import tictim.paraglider.config.DebugCfg;
import tictim.paraglider.impl.movement.ClientPlayerMovement;

import java.text.DecimalFormat;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static tictim.paraglider.client.render.StaminaWheelConstants.WHEEL_RADIUS;

public final class ParagliderGuiLayers {
	private ParagliderGuiLayers() {}

	public static void renderStaminaWheel(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.player == null ||
				mc.screen instanceof DisableStaminaRender ||
				!Stamina.get(mc.player).renderStaminaWheel() ||
				!ParagliderMod.instance().getPlayerStateMap().hasStaminaConsumption()) return;
		int w = mc.getWindow().getGuiScaledWidth();
		int h = mc.getWindow().getGuiScaledHeight();

		ParagliderClientSettings settings = ParagliderClientSettings.get();
		int x = Mth.clamp((int)Math.round(settings.staminaWheelX() * w), 1 + WHEEL_RADIUS, w - 2 - WHEEL_RADIUS);
		int y = Mth.clamp((int)Math.round(settings.staminaWheelY() * h), 1 + WHEEL_RADIUS, h - 2 - WHEEL_RADIUS);

		InGameStaminaWheelRenderer.get().render(guiGraphics, x, y, 25,
				deltaTracker.getGameTimeDeltaPartialTick(false));
	}

	private static int yOffset;

	public static void renderMovementDebug(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
		if (!DebugCfg.get().debugPlayerMovement()) return;
		Minecraft mc = Minecraft.getInstance();
		Player p = mc.player;
		if (p == null) return;

		yOffset = 0;

		addDebugText(p, s -> {
			guiGraphics.drawString(mc.font, s,
					guiGraphics.guiWidth() - 4 - mc.font.width(s), 4 + yOffset,
					-1, true);
			yOffset += mc.font.lineHeight + 1;
		});
	}

	private static final DecimalFormat STAMINA = new DecimalFormat("0.#");
	private static final DecimalFormat PERCENTAGE = new DecimalFormat("#.#%");
	private static final DecimalFormat PERCENTAGE_SIGNED = new DecimalFormat("+#.#%;-#.#%");

	private static void addDebugText(Player p, Consumer<String> consumer) {
		Movement movement = Movement.get(p);
		Stamina stamina = Stamina.get(p);
		VesselContainer vessels = VesselContainer.get(p);
		ParagliderClientSettings clientSettings = ParagliderClientSettings.get();

		PlayerState state = movement.state();
		double staminaDelta = movement.staminaDelta();

		if (state.flags().isEmpty()) {
			consumer.accept("State: " + state.id());
		} else {
			consumer.accept("State: " + state.id() + " (" + state.flags().stream()
					.map(Object::toString)
					.collect(Collectors.joining(" ")) + ")");
		}
		consumer.accept((stamina.isDepleted() ? ChatFormatting.RED : "") + "Stamina: " +
				STAMINA.format(stamina.stamina()) + " / " + STAMINA.format(stamina.maxStamina()));

		StringBuilder stb = new StringBuilder().append("Stamina Delta: ");

		int baseStaminaDelta = state.staminaDelta();
		if (baseStaminaDelta != staminaDelta) {
			stb.append(STAMINA.format(baseStaminaDelta));
			double diff = staminaDelta - baseStaminaDelta;
			if (diff > 0) stb.append("+");
			stb.append(STAMINA.format(diff));
		} else {
			stb.append(STAMINA.format(staminaDelta));
		}

		if (movement instanceof ClientPlayerMovement cpm) {
			double efficiency = cpm.staminaEfficiency();
			if (efficiency != 0) stb.append(" (").append(PERCENTAGE_SIGNED.format(efficiency));
		}

		consumer.accept(stb.toString());

		consumer.accept("Recovery Delay: " + state.recoveryDelay());
		consumer.accept(vessels.staminaVessel() + " Stamina Vessels, " + vessels.heartContainer() + " Heart Containers");
		consumer.accept(movement.recoveryDelay() + " Recovery Delay");
		consumer.accept("Stamina Wheel X: " + PERCENTAGE.format(clientSettings.staminaWheelX()) +
				", Stamina Wheel Y: " + PERCENTAGE.format(clientSettings.staminaWheelY()));
	}
}
