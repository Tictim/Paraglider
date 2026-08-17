package tictim.paraglider.client.screen;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NullMarked;
import tictim.paraglider.ParagliderClientMod;
import tictim.paraglider.client.render.SettingsWidgetStaminaWheelRenderer;
import tictim.paraglider.client.settings.ExtraWheelAttachment;
import tictim.paraglider.client.settings.ParagliderClientSettings;
import tictim.paraglider.client.settings.StaminaWheelPosition;
import tictim.paraglider.client.settings.StaminaWheelPosition.Dir8;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static tictim.paraglider.client.render.StaminaWheelConstants.WHEEL_RADIUS;
import static tictim.paraglider.client.render.StaminaWheelConstants.wheelColor;

@NullMarked
@SuppressWarnings("NotNullFieldNotInitialized")
public class StaminaWheelSettingScreen extends Screen implements DisableStaminaRender {
	private static final DecimalFormat PERCENTAGE = new DecimalFormat("#.#%");
	private static final int ANCHOR_BUTTON_SIZE = 12;
	private static final int EXTRA_WHEEL_ATTACHMENT_BUTTON_SIZE = 20;
	private static final int PRESET_BUTTON_WIDTH = 64;
	private static final int PRESET_BUTTON_HEIGHT = 20;

	private final SettingsWidgetStaminaWheelRenderer wheelRenderer = new SettingsWidgetStaminaWheelRenderer();
	private final @Nullable ParagliderSettingsScreen parent;

	private final Component anchorText = Component.translatable("paraglider.settings.stamina_wheel_settings.anchor");
	private final Component extraWheelText = Component.translatable("paraglider.settings.stamina_wheel_settings.extra_wheel_attachment");
	private final Component presetText = Component.translatable("paraglider.settings.stamina_wheel_settings.preset");

	private StaminaWheelWidget staminaWheelWidget;
	private Button saveButton;
	private Button cancelButton;
	private List<Button> anchorButtons;
	private ExtraWheelAttachmentButton extraWheelAttachmentCycleButton;
	private List<Button> presetButtons;

	private Component @Nullable [] helpText;

	private final StaminaWheelPosition initialPos;
	private ExtraWheelAttachment extraWheelAttachment;

	private @Nullable Dir8 anchor;

	protected StaminaWheelSettingScreen(@Nullable ParagliderSettingsScreen parent) {
		super(Component.empty());
		this.parent = parent;

		ParagliderClientSettings settings = ParagliderClientSettings.get();
		this.initialPos = settings.staminaWheelPosition();
		this.extraWheelAttachment = settings.extraWheelAttachment();
		this.anchor = getAnchor(settings.staminaWheelPosition());
	}

	@Override protected void init() {
		//noinspection ConstantValue
		if (this.staminaWheelWidget == null) {
			this.staminaWheelWidget = new StaminaWheelWidget(this);
			this.staminaWheelWidget.setWheelPos(this.initialPos);
		}

		addRenderableWidget(this.staminaWheelWidget);

		this.saveButton = addRenderableWidget(Button.builder(Component.translatable("paraglider.settings.stamina_wheel_settings.save"), button -> {
					ParagliderClientSettings settings = ParagliderClientSettings.get();
					ParagliderClientSettings newSettings = new ParagliderClientSettings(
							this.staminaWheelWidget.toStaminaWheelPosition(this.anchor),
							settings.windParticleFrequency(),
							this.extraWheelAttachment,
							settings.autoParagliding()
					);

					ParagliderClientMod.instance().setSettings(newSettings);
					if (this.parent != null) this.parent.saveSettings();
					onClose();
				})
				.bounds(0, 0, 48, 20)
				.build());

		this.cancelButton = addRenderableWidget(Button.builder(Component.translatable("paraglider.settings.stamina_wheel_settings.cancel"), button -> onClose())
				.bounds(0, 0, 48, 20)
				.build());

		this.anchorButtons = List.of(
				anchorButton(Dir8.UL), anchorButton(Dir8.U), anchorButton(Dir8.UR),
				anchorButton(Dir8.L), anchorButton(null), anchorButton(Dir8.R),
				anchorButton(Dir8.DL), anchorButton(Dir8.D), anchorButton(Dir8.DR));

		this.extraWheelAttachmentCycleButton = addRenderableWidget(
				new ExtraWheelAttachmentButton(0, 0, Component.empty(),
						() -> this.extraWheelAttachment,
						a -> this.extraWheelAttachment = a)
		);

		this.presetButtons = List.of(
				presetButton("default", StaminaWheelPosition.DEFAULT, ExtraWheelAttachment.LEFT),
				presetButton("hud_center", new StaminaWheelPosition.Anchored(Dir8.D, 0, -42), ExtraWheelAttachment.TOP),
				presetButton("hud_left", new StaminaWheelPosition.Anchored(Dir8.D, -109, -35), ExtraWheelAttachment.LEFT),
				presetButton("hud_right", new StaminaWheelPosition.Anchored(Dir8.D, 109, -35), ExtraWheelAttachment.RIGHT)
		);

		this.helpText = new Component[]{
				Component.translatable("paraglider.settings.stamina_wheel_settings.guide.0"),
				Component.translatable("paraglider.settings.stamina_wheel_settings.guide.1"),
				Component.translatable("paraglider.settings.stamina_wheel_settings.guide.1.2"),
				Component.translatable("paraglider.settings.stamina_wheel_settings.guide.2",
						this.minecraft.options.keyInventory.getTranslatedKeyMessage(),
						ParagliderClientMod.instance().getParagliderSettingsKey().getTranslatedKeyMessage())
		};
	}

	private Button anchorButton(@Nullable Dir8 value) {
		Button button = Button
				.builder(Component.empty(), btn -> this.anchor = value)
				.bounds(0, 0, ANCHOR_BUTTON_SIZE, ANCHOR_BUTTON_SIZE)
				.build();
		button.setOverrideRenderHighlightedSprite(() -> button.isHoveredOrFocused() || this.anchor == value);
		return addRenderableWidget(button);
	}

	private Button presetButton(
			String langKey,
			StaminaWheelPosition staminaWheelPosition,
			ExtraWheelAttachment extraWheelAttachment) {
		return addRenderableWidget(Button
				.builder(Component.translatable("paraglider.settings.stamina_wheel_settings.preset." + langKey),
						button -> applyPreset(staminaWheelPosition, extraWheelAttachment))
				.bounds(0, 0, PRESET_BUTTON_WIDTH, PRESET_BUTTON_HEIGHT)
				.build());
	}

	@Override public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
		boolean wheelAtRight = this.staminaWheelWidget.wheelX >= this.width / 2.0;
		boolean wheelAtDown = this.staminaWheelWidget.wheelY >= this.height / 2.0;

		// main button widget

		int anchorWidgetWidth = Math.max(ANCHOR_BUTTON_SIZE * 3, this.font.width(this.anchorText));
		int anchorWidgetHeight = this.font.lineHeight + 2 + ANCHOR_BUTTON_SIZE * 3;

		int extraWheelWidth = Math.max(EXTRA_WHEEL_ATTACHMENT_BUTTON_SIZE, this.font.width(this.extraWheelText));
		int extraWheelHeight = this.font.lineHeight + 2 + EXTRA_WHEEL_ATTACHMENT_BUTTON_SIZE;

		int presetButtonWidth = Math.max(PRESET_BUTTON_WIDTH, this.font.width(this.presetText));
		int presetButtonHeight = this.font.lineHeight + 2 + this.presetButtons.size() * PRESET_BUTTON_HEIGHT;

		int topWidgetWidth = 2 + Math.max(anchorWidgetWidth, extraWheelWidth) + 4 + presetButtonWidth + 2;
		int topWidgetHeight = 2 + Math.max(anchorWidgetHeight + 4 + extraWheelHeight, presetButtonHeight) + 2;
		int topWidgetX = wheelAtRight ? 0 : this.width - topWidgetWidth;
		int topWidgetY = wheelAtDown ? this.height - topWidgetHeight : 0;

		for (int i = 0; i < this.anchorButtons.size(); i++) {
			Button button = this.anchorButtons.get(i);
			button.setPosition(topWidgetX + 2 + ANCHOR_BUTTON_SIZE * (i % 3),
					topWidgetY + 2 + this.font.lineHeight + 2 + ANCHOR_BUTTON_SIZE * (i / 3));
		}

		this.extraWheelAttachmentCycleButton.setPosition(topWidgetX + 2,
				topWidgetY + 2 + anchorWidgetHeight + 4 + this.font.lineHeight + 2);

		for (int i = 0; i < this.presetButtons.size(); i++) {
			Button button = this.presetButtons.get(i);
			button.setPosition(topWidgetX + 2 + Math.max(anchorWidgetWidth, extraWheelWidth) + 4,
					topWidgetY + 2 + this.font.lineHeight + 2 + i * 20);
		}

		// help / save button widget

		//noinspection DataFlowIssue
		int textWidth = Arrays.stream(this.helpText).mapToInt(e -> this.font.width(e)).max().orElse(0) + 6 + 48;
		int textHeight = Math.max(this.helpText.length * this.font.lineHeight, 40 + 2) + 4;
		int textX = wheelAtRight ? 0 : this.width - textWidth;
		int textY = wheelAtDown ? 0 : this.height - textHeight;

		this.saveButton.setX(textX + textWidth - this.saveButton.getWidth() - 2);
		this.saveButton.setY(textY + textHeight - this.saveButton.getHeight() - 2);
		this.cancelButton.setX(textX + textWidth - this.cancelButton.getWidth() - 2);
		this.cancelButton.setY(textY + textHeight - this.saveButton.getHeight() - this.cancelButton.getHeight() - 4);

		graphics.fillGradient(topWidgetX, topWidgetY, topWidgetX + topWidgetWidth, topWidgetY + topWidgetHeight, 0x80000000, 0x80000000);
		graphics.fillGradient(textX, textY, textX + textWidth, textY + textHeight, 0x80000000, 0x80000000);

		super.extractRenderState(graphics, mouseX, mouseY, a);

		graphics.text(this.font, this.anchorText,
				topWidgetX + 2, topWidgetY + 2, -1);
		graphics.text(this.font, this.extraWheelText,
				topWidgetX + 2, topWidgetY + 2 + anchorWidgetHeight + 4, -1);
		graphics.text(this.font, this.presetText,
				topWidgetX + 2 + Math.max(anchorWidgetWidth, extraWheelWidth) + 4, topWidgetY + 2, -1);

		int y = textY + 2;
		for (Component t : this.helpText) {
			graphics.text(this.font, t, textX + 2, y, 0xFF00DF53);
			y += this.font.lineHeight;
		}
	}

	// no background
	@Override public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {}

	@Override public boolean keyPressed(KeyEvent event) {
		if (super.keyPressed(event)) return true;
		var key = com.mojang.blaze3d.platform.InputConstants.getKey(event);
		if (this.minecraft.options.keyInventory.isActiveAndMatches(key) ||
				ParagliderClientMod.instance().getParagliderSettingsKey().getKey().equals(key)) {
			onClose();
			return true;
		} else for (int i = 0; i < 4; i++) {
			if (!this.minecraft.options.keyHotbarSlots[i].matches(event)) continue;
			if (i == 3) this.wheelRenderer.setExtraWheels((this.wheelRenderer.extraWheels() + 1) % 3);
			else this.wheelRenderer.setWheels(i + 1);
			return true;
		}
		return false;
	}

	@Override public void resize(int width, int height) {
		this.staminaWheelWidget.onScreenResize(width, height);
		super.resize(width, height);
	}

	@Override public void onClose() {
		this.minecraft.gui.setScreen(this.parent);
	}

	private void applyPreset(StaminaWheelPosition staminaWheelPosition, ExtraWheelAttachment extraWheelAttachment) {
		this.staminaWheelWidget.setWheelPos(staminaWheelPosition);
		this.anchor = getAnchor(staminaWheelPosition);
		this.extraWheelAttachment = extraWheelAttachment;
	}

	private static @Nullable Dir8 getAnchor(StaminaWheelPosition staminaWheelPosition) {
		return switch (staminaWheelPosition) {
			case StaminaWheelPosition.Anchored anchored -> anchored.anchor();
			case StaminaWheelPosition.ScreenProportion ignored -> null;
		};
	}

	public static class ExtraWheelAttachmentButton extends AbstractButton {
		private final ExtraWheelAttachment[] values = ExtraWheelAttachment.values();
		private final Supplier<ExtraWheelAttachment> value;
		private final Consumer<ExtraWheelAttachment> setValue;

		public ExtraWheelAttachmentButton(
				int x, int y, Component message,
				Supplier<ExtraWheelAttachment> value,
				Consumer<ExtraWheelAttachment> setValue) {
			super(x, y, EXTRA_WHEEL_ATTACHMENT_BUTTON_SIZE, EXTRA_WHEEL_ATTACHMENT_BUTTON_SIZE, message);

			this.value = value;
			this.setValue = setValue;
		}

		@Override public void onPress(InputWithModifiers input) {
			ExtraWheelAttachment newValue = this.values[(this.value.get().ordinal() + 1) % this.values.length];
			this.setValue.accept(newValue);
		}

		@Override protected void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
			extractDefaultSprite(graphics);
			graphics.blitSprite(
					RenderPipelines.GUI_TEXTURED,
					this.value.get().buttonIconPath(),
					getX() + 1,
					getY() + 1,
					getWidth() - 2,
					getHeight() - 2,
					this.alpha
			);
		}

		@Override protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {}
	}

	public static class StaminaWheelWidget extends AbstractWidget {
		private final StaminaWheelSettingScreen screen;

		private double wheelX, wheelY;
		private boolean dragging;
		private double dragStartX, dragStartY;
		private double dragDeltaX, dragDeltaY;

		public StaminaWheelWidget(StaminaWheelSettingScreen screen) {
			super(0, 0, WHEEL_RADIUS * 2, WHEEL_RADIUS * 2, Component.empty());
			this.screen = screen;
		}

		public StaminaWheelPosition toStaminaWheelPosition(@Nullable Dir8 anchor) {
			if (anchor == null) {
				return new StaminaWheelPosition.ScreenProportion(
						this.wheelX / this.screen.width,
						this.wheelY / this.screen.height);
			} else {
				return new StaminaWheelPosition.Anchored(
						anchor,
						this.wheelX - anchor.anchorX(this.screen.width),
						this.wheelY - anchor.anchorY(this.screen.height));
			}
		}

		public void onScreenResize(int newWidth, int newHeight) {
			Dir8 anchor = this.screen.anchor;
			if (anchor == null) {
				setWheelPos(this.wheelX / (double)this.screen.width * newWidth,
						this.wheelY / (double)this.screen.height * newHeight,
						newWidth, newHeight);
			} else {
				setWheelPos(this.wheelX - anchor.anchorX(this.screen.width) + anchor.anchorX(newWidth),
						this.wheelY - anchor.anchorY(this.screen.height) + anchor.anchorY(newHeight));
			}
		}

		@Override protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
			if (!this.visible) return;

			// draw rectangle lines as an indicator for the stamina wheel
			int color = wheelColor(0);
			graphics.fill(getX() - 1, getY() - 1, getX() + width + 1, getY(), color);
			graphics.fill(getX() - 1, getY() + height, getX() + width + 1, getY() + height + 1, color);
			graphics.fill(getX() - 1, getY() - 1, getX(), getY() + height + 1, color);
			graphics.fill(getX() + width, getY() - 1, getX() + width + 1, getY() + height + 1, color);

			String s = Math.floor(this.wheelX) + ", " + Math.floor(this.wheelY);
			Dir8 anchor = this.screen.anchor;
			String s2;
			if (anchor == null) {
				s2 = PERCENTAGE.format(this.wheelX / (double)this.screen.width) + ", " +
						PERCENTAGE.format(this.wheelY / (double)this.screen.height);
			} else {
				s2 = anchor + ": " +
						Math.floor(this.wheelX - anchor.anchorX(this.screen.width)) + ", " +
						Math.floor(this.wheelY - anchor.anchorY(this.screen.height));
			}

			Font font = this.screen.font;
			int sw = font.width(s);

			int textX = Math.min(getX(), this.screen.width - sw - 3);
			int textY = this.wheelY >= this.screen.height / 2.0 ?
					getY() - 1 - font.lineHeight * 2 :
					getY() + this.height + 2;
			graphics.text(font, s, textX, textY, color);
			graphics.text(font, s2, textX, textY + font.lineHeight, color);

			this.screen.wheelRenderer.staminaWheel(graphics, getX() + WHEEL_RADIUS, getY() + WHEEL_RADIUS, a,
					this.screen.extraWheelAttachment);
		}

		@Override public boolean mouseClicked(MouseButtonEvent event, boolean isDoubleClick) {
			if (this.active && this.visible && this.dragging &&
					event.button() == InputConstants.MOUSE_BUTTON_RIGHT &&
					isMouseOver(event.x(), event.y())) {
				this.dragging = false;
				setWheelPos(this.dragStartX, this.dragStartY);
				return true;
			} else return super.mouseClicked(event, isDoubleClick);
		}

		@Override public void onClick(MouseButtonEvent event, boolean isDoubleClick) {
			this.dragStartX = this.wheelX;
			this.dragStartY = this.wheelY;
			this.dragDeltaX = this.dragDeltaY = 0;
			this.dragging = true;
		}

		@Override protected void onDrag(MouseButtonEvent event, double dragX, double dragY) {
			this.dragDeltaX += dragX;
			this.dragDeltaY += dragY;
			setWheelPos(this.dragStartX + this.dragDeltaX, this.dragStartY + this.dragDeltaY);
		}

		@Override public void onRelease(MouseButtonEvent event) {
			if (this.dragging) {
				setWheelPos(this.dragStartX + this.dragDeltaX, this.dragStartY + this.dragDeltaY);
				this.dragging = false;
			}
		}

		@Override public void playDownSound(SoundManager handler) {}
		@Override protected void updateWidgetNarration(NarrationElementOutput o) {}

		private void setWheelPos(StaminaWheelPosition pos) {
			setWheelPosUncapped(pos.x(this.screen.width), pos.y(this.screen.height));
		}

		private void setWheelPos(double x, double y) {
			setWheelPos(x, y, this.screen.width, this.screen.height);
		}

		private void setWheelPos(double x, double y, double width, double height) {
			setWheelPosUncapped(
					Math.clamp(x, WHEEL_RADIUS, width - WHEEL_RADIUS),
					Math.clamp(y, WHEEL_RADIUS, height - WHEEL_RADIUS));
		}

		private void setWheelPosUncapped(double x, double y) {
			this.wheelX = x;
			this.wheelY = y;
			setX((int)Math.floor(x) - WHEEL_RADIUS);
			setY((int)Math.floor(y) - WHEEL_RADIUS);
		}
	}
}
