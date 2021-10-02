package tictim.paraglider.client.screen;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.util.InputMappings;
import net.minecraft.util.Util;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import tictim.paraglider.ModCfg;
import tictim.paraglider.event.ParagliderClientEventHandler;

import javax.annotation.Nullable;
import java.util.concurrent.atomic.AtomicReference;

public class ParagliderSettingScreen extends Screen{
	@Nullable private AtomicReference<Boolean> saveResult;
	private double displaySaveMessageTicks;

	public ParagliderSettingScreen(){
		super(StringTextComponent.EMPTY);
	}

	public void saveSettings(){
		AtomicReference<Boolean> saveResult = new AtomicReference<>();
		Util.getRenderingService().execute(() -> saveResult.set(ModCfg.saveParagliderSettings()));
		this.saveResult = saveResult;
		this.displaySaveMessageTicks = 600;
	}

	@Override protected void init(){
		addButton(new Button(width/2-64, height/2-8, 128, 20, new TranslationTextComponent("paragliderSettings.staminaWheelSettings"), b -> {
			//noinspection ConstantConditions
			this.minecraft.displayGuiScreen(new StaminaWheelSettingScreen(this));
		}));
	}

	@Override public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks){
		renderBackground(matrixStack);
		super.render(matrixStack, mouseX, mouseY, partialTicks);
		if(saveResult!=null){
			Boolean r = saveResult.get();
			font.func_243246_a(matrixStack, new TranslationTextComponent(
					r==null ? "paragliderSettings.saving" :
							r ? "paragliderSettings.saving.success" :
									"paragliderSettings.saving.failure"), 0, height-font.FONT_HEIGHT, 0xFFFFFF);
			if(r!=null){
				displaySaveMessageTicks -= partialTicks;
				if(displaySaveMessageTicks<=0){
					displaySaveMessageTicks = 0;
					saveResult = null;
				}
			}
		}
	}

	@SuppressWarnings("ConstantConditions")
	@Override public boolean keyPressed(int keyCode, int scanCode, int modifiers){
		if(super.keyPressed(keyCode, scanCode, modifiers)) return true;
		InputMappings.Input mouseKey = InputMappings.getInputByCode(keyCode, scanCode);
		if(this.minecraft.gameSettings.keyBindInventory.isActiveAndMatches(mouseKey)||
				ParagliderClientEventHandler.paragliderSettingsKey().getKey().equals(mouseKey)){
			this.closeScreen();
			return true;
		}else return false;
	}
}
