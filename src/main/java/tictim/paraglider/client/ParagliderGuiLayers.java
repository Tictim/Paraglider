package tictim.paraglider.client;

import net.minecraft.ChatFormatting;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.player.Player;
import tictim.paraglider.ParagliderUtils;
import tictim.paraglider.api.movement.Movement;
import tictim.paraglider.api.movement.ParagliderPlayerStates;
import tictim.paraglider.api.movement.PlayerState;
import tictim.paraglider.api.stamina.Stamina;
import tictim.paraglider.api.vessel.VesselContainer;
import tictim.paraglider.client.render.InGameStaminaWheelRenderer;
import tictim.paraglider.client.screen.DisableStaminaRender;
import tictim.paraglider.client.settings.ParagliderClientSettings;
import tictim.paraglider.client.settings.StaminaWheelPosition;
import tictim.paraglider.config.DebugCfg;
import tictim.paraglider.impl.movement.ClientPlayerMovement;
import tictim.paraglider.wind.Wind;

import java.text.DecimalFormat;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public final class ParagliderGuiLayers {
	private ParagliderGuiLayers() {}

	public static void renderStaminaWheel(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.player == null ||
				mc.screen instanceof DisableStaminaRender ||
				!ParagliderUtils.renderStaminaWheel(mc.player)) return;

		ParagliderClientSettings settings = ParagliderClientSettings.get();
		StaminaWheelPosition pos = settings.staminaWheelPosition();
		int x = (int)Math.floor(pos.x(guiGraphics.guiWidth()));
		int y = (int)Math.floor(pos.y(guiGraphics.guiHeight()));

		InGameStaminaWheelRenderer.get().render(guiGraphics, x, y, 25,
				deltaTracker.getGameTimeDeltaPartialTick(false),
				settings.extraWheelAttachment());
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

	private static final DecimalFormat D1 = new DecimalFormat("0.0");
	private static final DecimalFormat D2 = new DecimalFormat("0.00");
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
		consumer.accept("Recovery Delay: " + state.recoveryDelay());
		if (state.hasFlag(ParagliderPlayerStates.Flags.ASCENDING)) {
			consumer.accept("Wind height above: " + D2.format(Wind.getWindAbove(p.level(), p.getBoundingBox())));
		}

		String staminaText = (stamina.isDepleted() ? ChatFormatting.RED : "") + "Stamina: " +
				D1.format(stamina.stamina()) + " / " + D1.format(stamina.maxStamina());
		if (stamina.extraStamina() > 0) {
			staminaText += " + " + stamina.extraStamina();
		}
		consumer.accept(staminaText);

		StringBuilder stb = new StringBuilder().append("Stamina Delta: ");
		stb.append(D1.format(staminaDelta));

		double baseStaminaDelta = state.staminaDelta();
		if (baseStaminaDelta != staminaDelta) {
			double diff = (staminaDelta - baseStaminaDelta) / baseStaminaDelta;
			stb.append(" (").append(PERCENTAGE_SIGNED.format(diff)).append(")");
		}

		if (movement instanceof ClientPlayerMovement cpm) {
			double efficiency = cpm.staminaEfficiency();
			if (efficiency != 0) stb.append(" E: ").append(D2.format(efficiency));
		}

		consumer.accept(stb.toString());

		consumer.accept(vessels.staminaVessel() + " Stamina Vessels, " + vessels.heartContainer() + " Heart Containers");
		consumer.accept(movement.recoveryDelay() + " Recovery Delay");
	}
}
