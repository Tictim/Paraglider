package tictim.paraglider.client.overlay;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
import tictim.paraglider.ModCfg;
import tictim.paraglider.client.DisableStaminaRender;
import tictim.paraglider.client.InGameStaminaWheelRenderer;
import tictim.paraglider.client.StaminaWheelRenderer;

import static tictim.paraglider.client.StaminaWheelConstants.WHEEL_RADIUS;

public class StaminaWheelOverlay implements IGuiOverlay{
	private static final StaminaWheelRenderer STAMINA_WHEEL_RENDERER = new InGameStaminaWheelRenderer();

	@Override
	public void render(ForgeGui gui, GuiGraphics guiGraphics, float partialTick, int screenWidth, int screenHeight) {
		if(Minecraft.getInstance().screen instanceof DisableStaminaRender||
				!(ModCfg.paraglidingConsumesStamina()||ModCfg.runningConsumesStamina())) return;

		int x = Mth.clamp((int)Math.round(ModCfg.staminaWheelX()*screenWidth), 1+WHEEL_RADIUS, screenWidth-2-WHEEL_RADIUS);
		int y = Mth.clamp((int)Math.round(ModCfg.staminaWheelY()*screenHeight), 1+WHEEL_RADIUS, screenHeight-2-WHEEL_RADIUS);

		STAMINA_WHEEL_RENDERER.renderStamina(guiGraphics, x, y, 25);
	}
}
