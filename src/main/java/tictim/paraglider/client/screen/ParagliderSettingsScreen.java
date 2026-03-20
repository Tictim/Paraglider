package tictim.paraglider.client.screen;

import com.mojang.blaze3d.platform.InputConstants;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Util;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tictim.paraglider.ParagliderClientMod;
import tictim.paraglider.ParagliderMod;
import tictim.paraglider.ParagliderUtils;
import tictim.paraglider.client.settings.ParagliderClientSettings;
import tictim.paraglider.client.settings.ParagliderClientSettingsIO;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static tictim.paraglider.ParagliderUtils.ms;

public class ParagliderSettingsScreen extends Screen {
	private final Component saveButtonText = Component.translatable("paraglider.settings.save");
	private final Component saveButtonTextUnsaved = Component.translatable("paraglider.settings.save.unsaved");

	private final List<AbstractWidget> widgets = new ArrayList<>();
	private @Nullable ParticleSliderWidget particleSliderWidget;
	private @Nullable CycleButton<Boolean> autoParaglidingButton;
	private @Nullable Button saveSettingsButton;

	private @Nullable ParagliderSettingsScreen.SaveLoadAction saveLoadAction;

	public ParagliderSettingsScreen() {
		super(Component.empty());
	}

	public void saveSettings() {
		var currentSettings = ParagliderClientSettings.get();
		var newSettings = currentSettings;

		if (this.particleSliderWidget != null && this.autoParaglidingButton != null &&
				(this.particleSliderWidget.dirty ||
						this.autoParaglidingButton.getValue() != currentSettings.autoParagliding())) {
			newSettings = new ParagliderClientSettings(
					currentSettings.staminaWheelPosition(),
					this.particleSliderWidget.value(),
					currentSettings.extraWheelAttachment(),
					this.autoParaglidingButton.getValue()
			);
			this.particleSliderWidget.dirty = false;
		}

		ParagliderClientMod.instance().setSettings(newSettings);

		SaveLoadAction saveLoadAction = new SaveLoadAction(false, null);
		ParagliderClientSettingsIO.save(newSettings, saveLoadAction::notifySaveResult);
		this.saveLoadAction = saveLoadAction;
	}

	public void loadSettings() {
		enableWidgets(false);
		SaveLoadAction saveLoadAction = new SaveLoadAction(true, b -> enableWidgets(true));
		ParagliderClientSettingsIO.load(saveLoadAction::notifySaveResult);
		this.saveLoadAction = saveLoadAction;
	}

	private void enableWidgets(boolean enabled) {
		for (AbstractWidget widget : this.widgets) widget.active = enabled;
	}

	@Override protected void init() {
		Button staminaWheelSettingsButton = Button
				.builder(Component.translatable("paraglider.settings.stamina_wheel_settings"),
						b -> Objects.requireNonNull(this.minecraft).setScreen(new StaminaWheelSettingScreen(this)))
				.size(128, 20)
				.build();

		this.widgets.clear();
		this.widgets.add(staminaWheelSettingsButton);
		this.widgets.add(this.particleSliderWidget = new ParticleSliderWidget(128, 20, this.particleSliderWidget));
		this.widgets.add(this.autoParaglidingButton = CycleButton.onOffBuilder(ParagliderClientSettings.get().autoParagliding())
				.create(0, 0, 128, 20, Component.translatable("paraglider.settings.auto_paragliding")));

		int totalHeight = (this.widgets.size() - 1) * 10;
		for (AbstractWidget w : this.widgets) {
			totalHeight += w.getHeight();
		}

		int y = (this.height - totalHeight) / 2;
		for (AbstractWidget w : this.widgets) {
			w.setX((this.width - w.getWidth()) / 2);
			w.setY(y);
			y += w.getHeight() + 10;
		}

		this.widgets.add(this.saveSettingsButton = Button.builder(this.saveButtonText, b -> saveSettings())
				.bounds(this.width - 128 - 10, this.height - 20 - 10 - 20 - 10 - 20 - 10, 128, 20)
				.build());
		this.widgets.add(Button.builder(Component.translatable("paraglider.settings.open_folder"),
						b -> Util.getPlatform().openUri(ParagliderClientSettingsIO.FILEPATH.getParent().toUri()))
				.bounds(this.width - 128 - 10, this.height - 20 - 10 - 20 - 10, 128, 20)
				.tooltip(Tooltip.create(Component.translatable("paraglider.settings.open_folder.tooltip")))
				.build());
		this.widgets.add(Button.builder(Component.translatable("paraglider.settings.reload"),
						b -> loadSettings())
				.bounds(this.width - 128 - 10, this.height - 20 - 10, 128, 20)
				.build());

		for (AbstractWidget widget : this.widgets) addRenderableWidget(widget);

		if (!ParagliderUtils.renderStaminaWheel(Objects.requireNonNull(this.minecraft).player)) {
			staminaWheelSettingsButton.active = false;

			String s = ParagliderClientMod.instance().removeStaminaWheel() ?
					ParagliderClientMod.instance().staminaWheelRemoverId() :
					ParagliderMod.instance().staminaFactoryOrigin();
			if (s == null) s = "(unknown)";

			staminaWheelSettingsButton.setTooltip(Tooltip.create(
					Component.translatable("paraglider.settings.stamina_wheel_settings.disabled", s)));
		}
	}

	@Override public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
		if (this.saveSettingsButton != null) {
			this.saveSettingsButton.setMessage(this.particleSliderWidget != null && this.particleSliderWidget.dirty ?
					saveButtonTextUnsaved : saveButtonText);
		}

		super.render(graphics, mouseX, mouseY, partialTicks);

		if (this.saveLoadAction != null) {
			this.saveLoadAction.update();
			int alpha = !saveLoadAction.isComplete() ? 0xFF : BargainScreen.getDialogAlpha(ms() - saveLoadAction.saveCompleteTimestamp);
			if (alpha == 0) this.saveLoadAction = null;
			else {
				graphics.drawString(font, saveLoadAction.text(), 5, height - font.lineHeight - 5, alpha << 24 | 0xFFFFFF, true);
			}
		}
	}

	@Override public boolean keyPressed(@NotNull KeyEvent event) {
		if (super.keyPressed(event)) return true;
		InputConstants.Key key = InputConstants.getKey(event);
		if (this.minecraft.options.keyInventory.isActiveAndMatches(key) ||
				ParagliderClientMod.instance().getParagliderSettingsKey().getKey().equals(key)) {
			this.onClose();
			return true;
		} else return false;
	}

	private static final class ParticleSliderWidget extends AbstractSliderButton {
		private boolean dirty;

		public ParticleSliderWidget(int width, int height, @Nullable ParticleSliderWidget prevInstance) {
			super(0, 0, width, height, Component.empty(), 0);
			setTooltip(Tooltip.create(Component.translatable("paraglider.settings.wind_particle_freq.tooltip")));

			if (prevInstance != null) {
				setMessage(prevInstance.getMessage());
				this.value = prevInstance.value;
				this.dirty = prevInstance.dirty;
			} else {
				setMessage(message(this.value = ParagliderClientSettings.get().windParticleFrequency()));
				this.dirty = false;
			}
		}

		public double value() {
			return this.value;
		}

		@Override protected void updateMessage() {
			this.setMessage(message(this.value));
		}
		@Override protected void applyValue() {
			this.dirty = true;
		}

		private static final DecimalFormat fmt = new DecimalFormat("0%");

		private static Component message(double value) {
			return Component.translatable("paraglider.settings.wind_particle_freq", fmt.format(value));
		}
	}

	private static final class SaveLoadAction {
		private final boolean isLoading;
		private @Nullable BooleanConsumer callback;

		private volatile State state = State.WAITING;
		private volatile long saveCompleteTimestamp;

		private SaveLoadAction(boolean isLoading, @Nullable BooleanConsumer callback) {
			this.isLoading = isLoading;
			this.callback = callback;
		}

		void update() {
			if (callback != null && isComplete()) {
				callback.accept(state == State.SUCCESS);
				callback = null;
			}
		}

		void notifySaveResult(boolean success) {
			if (this.state != State.WAITING) return;
			this.state = success ? State.SUCCESS : State.FAIL;
			this.saveCompleteTimestamp = ms();
		}

		boolean isComplete() {
			return state != State.WAITING;
		}

		@NotNull Component text() {
			return isLoading ? state.loadText : state.saveText;
		}

		enum State {
			WAITING("paraglider.settings.saving", "paraglider.settings.loading"),
			SUCCESS("paraglider.settings.saving.success", "paraglider.settings.loading.success"),
			FAIL("paraglider.settings.saving.failure", "paraglider.settings.loading.failure", ChatFormatting.RED);

			final Component saveText;
			final Component loadText;

			State(String saveKey, String loadKey) {
				this.saveText = Component.translatable(saveKey);
				this.loadText = Component.translatable(loadKey);
			}
			State(String saveKey, String loadKey, ChatFormatting fmt) {
				this.saveText = Component.translatable(saveKey).withStyle(fmt);
				this.loadText = Component.translatable(loadKey).withStyle(fmt);
			}
		}
	}
}
