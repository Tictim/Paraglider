package tictim.paraglider.client.screen;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tictim.paraglider.ParagliderClientMod;
import tictim.paraglider.ParagliderMod;
import tictim.paraglider.client.ParagliderClientSettings;
import tictim.paraglider.client.render.SettingsWidgetStaminaWheelRenderer;

import java.text.DecimalFormat;
import java.util.Arrays;

import static tictim.paraglider.client.render.StaminaWheelConstants.WHEEL_RADIUS;
import static tictim.paraglider.client.render.StaminaWheelConstants.wheelColor;

public class StaminaWheelSettingScreen extends Screen implements DisableStaminaRender {
	private static final DecimalFormat PERCENTAGE = new DecimalFormat("#.#%");

	private final @Nullable ParagliderSettingsScreen parent;
	private StaminaWheel staminaWheel;
	private Button saveButton;
	private Button cancelButton;

	private Component[] helpText;

	private double initialStaminaWheelX = ParagliderClientSettings.get().staminaWheelX();
	private double initialStaminaWheelY = ParagliderClientSettings.get().staminaWheelY();

	protected StaminaWheelSettingScreen(@Nullable ParagliderSettingsScreen parent) {
		super(Component.empty());
		this.parent = parent;
	}

	@Override protected void init() {
		this.staminaWheel = addRenderableWidget(new StaminaWheel(initialStaminaWheelX, initialStaminaWheelY));
		this.saveButton = addRenderableWidget(Button.builder(Component.translatable("paraglider.settings.stamina_wheel_settings.save"), button -> {
					ParagliderMod.LOGGER.debug("Save?");
					ParagliderClientSettings clientSettings = ParagliderClientSettings.get();
					clientSettings.setStaminaWheel(staminaWheel.getStaminaWheelX(), staminaWheel.getStaminaWheelY());
					if (parent != null) parent.saveSettings();
					onClose();
				})
				.bounds(0, 0, 48, 20)
				.build());
		this.cancelButton = addRenderableWidget(Button.builder(Component.translatable("paraglider.settings.stamina_wheel_settings.cancel"), button -> onClose())
				.bounds(0, 0, 48, 20)
				.build());
		//noinspection ConstantConditions
		this.helpText = new Component[]{
				Component.translatable("paraglider.settings.stamina_wheel_settings.guide.0"),
				Component.translatable("paraglider.settings.stamina_wheel_settings.guide.1"),
				Component.translatable("paraglider.settings.stamina_wheel_settings.guide.2",
						this.minecraft.options.keyInventory.getTranslatedKeyMessage(),
						ParagliderClientMod.instance().getParagliderSettingsKey().getTranslatedKeyMessage())
		};
	}

	@Override
	public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
		int textWidth = Arrays.stream(this.helpText).mapToInt(e -> this.font.width(e)).max().orElse(0) + 6 + 48;
		int textHeight = Math.max(this.helpText.length * this.font.lineHeight, 40 + 2) + 4;
		int textX = staminaWheel.getX() >= this.width / 2 ? 0 : this.width - textWidth;
		int textY = staminaWheel.getY() >= this.height / 2 ? 0 : this.height - textHeight;

		this.saveButton.setX(textX + textWidth - this.saveButton.getWidth() - 2);
		this.saveButton.setY(textY + textHeight - this.saveButton.getHeight() - 2); // height realms? what?
		this.cancelButton.setX(textX + textWidth - this.cancelButton.getWidth() - 2);
		this.cancelButton.setY(textY + textHeight - this.saveButton.getHeight() - this.cancelButton.getHeight() - 4);

		guiGraphics.fillGradient(textX, textY, textX + textWidth, textY + textHeight, 0x80000000, 0x80000000);
		super.render(guiGraphics, mouseX, mouseY, partialTicks);

		int y = textY + 2;
		for (Component t : this.helpText) {
			guiGraphics.drawString(this.font, t, textX + 2, y, 0xFF00DF53);
			y += this.font.lineHeight;
		}
	}

	// no background
	@Override public void renderBackground(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {}

	@SuppressWarnings("ConstantConditions")
	@Override public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		if (super.keyPressed(keyCode, scanCode, modifiers)) return true;
		InputConstants.Key mouseKey = InputConstants.getKey(keyCode, scanCode);
		if (this.minecraft.options.keyInventory.isActiveAndMatches(mouseKey) ||
				ParagliderClientMod.instance().getParagliderSettingsKey().getKey().equals(mouseKey)) {
			onClose();
			return true;
		} else for (int i = 0; i < 3; i++) {
			if (!this.minecraft.options.keyHotbarSlots[i].matches(keyCode, scanCode)) continue;
			this.staminaWheel.wheel.setWheels(i + 1);
			return true;
		}
		return false;
	}

	@Override public void resize(@NotNull Minecraft minecraft, int width, int height) {
		this.initialStaminaWheelX = this.staminaWheel.getStaminaWheelX();
		this.initialStaminaWheelY = this.staminaWheel.getStaminaWheelY();
		super.resize(minecraft, width, height);
	}

	@SuppressWarnings("ConstantConditions")
	@Override public void onClose() {
		this.minecraft.setScreen(parent);
	}

	private int screenWidth() {
		return this.width;
	}
	private int screenHeight() {
		return this.height;
	}

	public class StaminaWheel extends AbstractWidget {
		private final SettingsWidgetStaminaWheelRenderer wheel = new SettingsWidgetStaminaWheelRenderer();

		private boolean dragging;
		private int dragStartX, dragStartY;
		private double dragDeltaX, dragDeltaY;

		public StaminaWheel(double x, double y) {
			super((int)Math.round(x * (double)screenWidth()) - WHEEL_RADIUS,
					(int)Math.round(y * (double)screenHeight()) - WHEEL_RADIUS,
					WHEEL_RADIUS * 2, WHEEL_RADIUS * 2, Component.empty());
		}

		public double getStaminaWheelX() {
			return (getX() + WHEEL_RADIUS) / (double)screenWidth();
		}
		public double getStaminaWheelY() {
			return (getY() + WHEEL_RADIUS) / (double)screenHeight();
		}

		@Override public void renderWidget(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
			setX(Mth.clamp(getX(), 1, screenWidth() - 2 - WHEEL_RADIUS * 2));
			setY(Mth.clamp(getY(), 1, screenHeight() - 2 - WHEEL_RADIUS * 2));
			if (this.visible)
				this.wheel.render(guiGraphics, getX() + WHEEL_RADIUS, getY() + WHEEL_RADIUS, 0, partialTicks);

			// draw rectangle lines as an indicator for the stamina wheel
			guiGraphics.fill(getX() - 1, getY() - 1, getX() + width + 1, getY(), wheelColor(0));
			guiGraphics.fill(getX() - 1, getY() + height, getX() + width + 1, getY() + height + 1, wheelColor(0));
			guiGraphics.fill(getX() - 1, getY() - 1, getX(), getY() + height + 1, wheelColor(0));
			guiGraphics.fill(getX() + width, getY() - 1, getX() + width + 1, getY() + height + 1, wheelColor(0));

			String s = (getX()) + ", " + (getY()) +
					" (" + PERCENTAGE.format(getStaminaWheelX()) + " :: " + PERCENTAGE.format(getStaminaWheelY()) + ")";
			int sw = font.width(s);

			int textX = Math.min(getX(), screenWidth() - sw - 3);
			int textY = getY() >= screenHeight() / 2 ? getY() - 1 - font.lineHeight : getY() + this.height + 1;
			guiGraphics.drawString(font, s, textX, textY, 0xFF00DF53);
		}

		@Override public boolean mouseClicked(double mouseX, double mouseY, int button) {
			if (this.active && this.visible && this.dragging && button == 1 && isMouseOver(mouseX, mouseY)) {
				this.dragging = false;
				setX(this.dragStartX);
				setY(this.dragStartY);
				return true;
			} else return super.mouseClicked(mouseX, mouseY, button);
		}

		@Override public void onClick(double mouseX, double mouseY, int button) {
			this.dragStartX = getX();
			this.dragStartY = getY();
			this.dragDeltaX = this.dragDeltaY = 0;
			this.dragging = true;
		}

		@Override protected void onDrag(double mouseX, double mouseY, double dragX, double dragY) {
			this.dragDeltaX += dragX;
			this.dragDeltaY += dragY;
			setX((int)Math.round(this.dragStartX + this.dragDeltaX + .5));
			setY((int)Math.round(this.dragStartY + this.dragDeltaY + .5));
		}

		@Override public void onRelease(double mouseX, double mouseY) {
			if (this.dragging) {
				setX((int)Math.round(this.dragStartX + this.dragDeltaX + .5));
				setY((int)Math.round(this.dragStartY + this.dragDeltaY + .5));
				this.dragging = false;
			}
		}

		@Override public void playDownSound(@NotNull SoundManager handler) {}
		@Override protected void updateWidgetNarration(@NotNull NarrationElementOutput o) {}
	}
}
