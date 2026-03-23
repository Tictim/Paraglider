package tictim.paraglider.client.render;

import it.unimi.dsi.fastutil.floats.Float2IntMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2fStack;
import tictim.paraglider.api.ParagliderAPI;
import tictim.paraglider.api.stamina.Stamina;
import tictim.paraglider.client.render.state.StaminaWheelRenderState;
import tictim.paraglider.client.settings.ExtraWheelAttachment;
import tictim.paraglider.config.DebugCfg;
import tictim.paraglider.contents.ParagliderTags;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import static net.minecraft.util.ARGB.alpha;
import static tictim.paraglider.client.render.StaminaWheelConstants.*;

public abstract class StaminaWheelRenderer {
	static final DecimalFormat DEBUG = new DecimalFormat("#.00");

	private static final Style SMALL_NUMBER_STYLE = Style.EMPTY.withFont(
			new FontDescription.Resource(ParagliderAPI.id("small_numbers")));
	private static final int FONT_HEIGHT = 7;

	protected final StaminaWheelState mainWheel = new StaminaWheelState();
	protected final StaminaWheelState extraWheel = new StaminaWheelState();

	private boolean debug;
	private @Nullable List<EffectTimer> debugAnims;
	private @Nullable List<String> debugAnimNames;

	/**
	 * Draw stamina wheel with center at (x, y).
	 */
	public void staminaWheel(@NotNull GuiGraphicsExtractor graphics, float x, float y, float partialTicks,
	                         @Nullable ExtraWheelAttachment extraWheelAttachment) {
		LocalPlayer player = Minecraft.getInstance().player;
		if (player == null) return;
		this.debug = isDebugEnabled(player);

		makeWheel(player, partialTicks);
		extract(graphics, x, y, isDebugEnabled(player), extraWheelAttachment);

		this.mainWheel.reset();
		this.extraWheel.reset();

		if (this.debug) {
			this.debug = false;
			if (this.debugAnims != null) this.debugAnims.clear();
			if (this.debugAnimNames != null) this.debugAnimNames.clear();
		}
	}

	// by default, holding a paraglider offhand enables stamina wheel debug info
	protected boolean isDebugEnabled(@NotNull Player player) {
		return DebugCfg.get().debugPlayerMovement() && player.getOffhandItem().is(ParagliderTags.PARAGLIDERS);
	}

	protected void debugAnim(String name, EffectTimer anim) {
		if (!this.debug) return;
		if (this.debugAnims == null) this.debugAnims = new ArrayList<>();
		if (this.debugAnimNames == null) this.debugAnimNames = new ArrayList<>();
		this.debugAnims.add(anim);
		this.debugAnimNames.add(name);
	}

	protected abstract void makeWheel(@NotNull Player player, float partialTicks);

	protected void extract(@NotNull GuiGraphicsExtractor graphics,
	                       float x, float y, boolean debug,
	                       @Nullable ExtraWheelAttachment extraWheelAttachment) {
		if (debug) {
			Font font = Minecraft.getInstance().font;
			int lines = 0;

			if (this.debugAnimNames != null && this.debugAnims != null) {
				for (int i = 0; i < this.debugAnimNames.size(); i++) {
					String name = this.debugAnimNames.get(i);
					EffectTimer anim = this.debugAnims.get(i);

					graphics.text(font,
							name + ": " + (anim.isActive() ? "active " + anim.activeDuration() : "inactive"),
							20, 10 + font.lineHeight * lines++,
							0xFFFFFFFF);
				}
			}

			if (lines > 0) lines++;

			int lineStart = lines;
			int maxWidth = 0;

			for (Float2IntMap.Entry e : this.mainWheel.segments().float2IntEntrySet()) {
				float point = e.getFloatKey();
				String segmentString = DEBUG.format(point) + ": ";

				graphics.text(font, segmentString,
						20, 10 + font.lineHeight * lines++,
						0xFFFFFFFF);
				maxWidth = Math.max(maxWidth, font.width(segmentString));
			}

			lines = lineStart;

			for (Float2IntMap.Entry e : this.mainWheel.segments().float2IntEntrySet()) {
				int color = e.getIntValue();
				graphics.text(font, String.format("#%X", color),
						20 + maxWidth, 10 + font.lineHeight * lines++,
						ARGB.color(Math.max(255, alpha(color) * 2), color));
			}
		}

		Matrix3x2fStack pose = graphics.pose();
		pose.pushMatrix();
		pose.translate(x, y, pose);

		wheels(graphics, extraWheelAttachment);

		pose.popMatrix();
	}

	protected void wheels(@NotNull GuiGraphicsExtractor graphics, @Nullable ExtraWheelAttachment extraWheelAttachment) {
		StaminaWheelRenderState.drawMainWheel(graphics, this.mainWheel, this.debug);

		if (this.extraWheel.alpha() > 0 && this.extraWheel.stamina() > 0 && extraWheelAttachment != null) {
			Matrix3x2fStack pose = graphics.pose();

			int wheels = Math.min(3, (int)Math.ceil(toWheelPos(this.mainWheel.maxStamina())));
			boolean hasTwoExtraWheels = this.extraWheel.stamina() > Stamina.STAMINA_PER_WHEEL;

			pose.pushMatrix();
			pose.translate(
					extraWheelOffsetX(extraWheelAttachment, wheels, hasTwoExtraWheels, 0),
					extraWheelOffsetY(extraWheelAttachment, wheels, hasTwoExtraWheels, 0),
					pose);
			StaminaWheelRenderState.drawExtraWheel(graphics, this.extraWheel, 0, this.debug);
			pose.popMatrix();

			if (hasTwoExtraWheels) {
				pose.pushMatrix();
				pose.translate(
						extraWheelOffsetX(extraWheelAttachment, wheels, true, 1),
						extraWheelOffsetY(extraWheelAttachment, wheels, true, 1),
						pose);
				StaminaWheelRenderState.drawExtraWheel(graphics, this.extraWheel, 1, this.debug);
				pose.popMatrix();
			}
		}

		int color = this.mainWheel.indicatorColorWithAlpha();
		if (alpha(color) >= 4) {
			text(graphics, "+" + Math.max(1, (int)(Math.ceil(this.mainWheel.staminaWheelPos()) - 3)),
					false, WHEEL_RADIUS - 1, 1 - WHEEL_RADIUS, color);
		}

		if (this.extraWheel.stamina() > Stamina.STAMINA_PER_WHEEL * 2 && extraWheelAttachment != null) {
			color = this.extraWheel.indicatorColorWithAlpha();
			if (alpha(color) >= 4) {
				text(graphics, "+" + Math.max(1, (int)(Math.ceil(toWheelPos(this.extraWheel.stamina())) - 2)),
						true, 1 - WHEEL_RADIUS, 1 - WHEEL_RADIUS, color);
			}
		}
	}

	protected float extraWheelOffsetX(ExtraWheelAttachment extraWheelAttachment, int wheels, boolean hasTwoExtraWheels, int index) {
		final float extraWheelMargin = EXTRA_WHEEL_RADIUS * 2 + 2f;

		return switch (extraWheelAttachment) {
			case TOP, BOTTOM -> {
				if (!hasTwoExtraWheels) yield 0.0f;
				else yield (index == 0 ? .5f : -.5f) * extraWheelMargin;
			}
			case LEFT, RIGHT -> {
				float offset = -WHEEL_RADIUS - EXTRA_WHEEL_RADIUS - wheels - 0.5f;
				if (index == 1) offset -= extraWheelMargin;
				if (extraWheelAttachment == ExtraWheelAttachment.RIGHT) offset = -offset;
				yield offset;
			}
		};
	}

	protected float extraWheelOffsetY(ExtraWheelAttachment extraWheelAttachment, int wheels, boolean hasTwoExtraWheels, int index) {
		return switch (extraWheelAttachment) {
			case LEFT, RIGHT -> 0;
			case TOP, BOTTOM -> {
				float offset = -WHEEL_RADIUS - EXTRA_WHEEL_RADIUS - 1 - wheels;
				if (hasTwoExtraWheels) offset += 2;
				if (extraWheelAttachment == ExtraWheelAttachment.BOTTOM) offset = -offset;
				yield offset;
			}
		};
	}

	protected void text(GuiGraphicsExtractor graphics, String text, boolean alignRight,
	                    int x, int y, int color) {
		Font font = Minecraft.getInstance().font;
		var pose = graphics.pose();

		pose.pushMatrix();
		pose.translate(x, y, pose);
		pose.scale(.5f, .5f, pose);

		MutableComponent component = Component.literal(text).setStyle(SMALL_NUMBER_STYLE);
		graphics.text(font, component,
				alignRight ? -font.width(component) : 0, -FONT_HEIGHT / 2, color);

		pose.popMatrix();
	}

	public enum WheelLevel {
		FIRST,
		SECOND,
		THIRD,
		EXTRA_1,
		EXTRA_2,
	}
}
