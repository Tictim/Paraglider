package tictim.paraglider.client.screen;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.Util;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import tictim.paraglider.ModCfg;
import tictim.paraglider.event.ParagliderClientEventHandler;

import javax.annotation.Nullable;
import java.util.concurrent.atomic.AtomicReference;

public class ParagliderSettingScreen extends Screen{
	@Nullable private AtomicReference<Boolean> saveResult;
	private double displaySaveMessageTicks;

	public ParagliderSettingScreen(){
		super(Component.empty());
	}

	public void saveSettings(){
		AtomicReference<Boolean> saveResult = new AtomicReference<>();
		Util.ioPool().execute(() -> saveResult.set(ModCfg.saveParagliderSettings()));
		this.saveResult = saveResult;
		this.displaySaveMessageTicks = 600;
	}

	@Override protected void init(){
		addRenderableWidget(new Button(width/2-64, height/2-8, 128, 20, Component.translatable("paragliderSettings.staminaWheelSettings"), b -> {
			//noinspection ConstantConditions
			this.minecraft.setScreen(new StaminaWheelSettingScreen(this));
		}));
	}

	@Override public void render(PoseStack matrixStack, int mouseX, int mouseY, float partialTicks){
		renderBackground(matrixStack);
		super.render(matrixStack, mouseX, mouseY, partialTicks);
		if(saveResult!=null){
			Boolean r = saveResult.get();
			font.drawShadow(matrixStack, Component.translatable(
					r==null ? "paragliderSettings.saving" :
							r ? "paragliderSettings.saving.success" :
									"paragliderSettings.saving.failure"), 0, height-font.lineHeight, 0xFFFFFF);
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
		InputConstants.Key mouseKey = InputConstants.getKey(keyCode, scanCode);
		if(this.minecraft.options.keyInventory.isActiveAndMatches(mouseKey)||
				ParagliderClientEventHandler.paragliderSettingsKey().getKey().equals(mouseKey)){
			this.onClose();
			return true;
		}else return false;
	}
}
