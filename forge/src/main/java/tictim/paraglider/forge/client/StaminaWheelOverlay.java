package tictim.paraglider.forge.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
import tictim.paraglider.ParagliderMod;
import tictim.paraglider.api.stamina.Stamina;
import tictim.paraglider.client.ParagliderClientSettings;
import tictim.paraglider.client.render.InGameStaminaWheelRenderer;
import tictim.paraglider.client.render.StaminaWheelRenderer;
import tictim.paraglider.client.screen.DisableStaminaRender;

import static tictim.paraglider.client.render.StaminaWheelConstants.WHEEL_RADIUS;

public class StaminaWheelOverlay implements IGuiOverlay{
	private static final StaminaWheelRenderer STAMINA_WHEEL_RENDERER = new InGameStaminaWheelRenderer();

	@Override
	public void render(ForgeGui gui, GuiGraphics guiGraphics, float partialTick, int screenWidth, int screenHeight){
		Minecraft mc = Minecraft.getInstance();
		if(mc.player==null||
				mc.screen instanceof DisableStaminaRender||
				!Stamina.get(mc.player).renderStaminaWheel()||
				!ParagliderMod.instance().getPlayerStateMap().hasStaminaConsumption()) return;

		ParagliderClientSettings settings = ParagliderClientSettings.get();
		int x = Mth.clamp((int)Math.round(settings.staminaWheelX()*screenWidth), 1+WHEEL_RADIUS, screenWidth-2-WHEEL_RADIUS);
		int y = Mth.clamp((int)Math.round(settings.staminaWheelY()*screenHeight), 1+WHEEL_RADIUS, screenHeight-2-WHEEL_RADIUS);

		STAMINA_WHEEL_RENDERER.renderStamina(guiGraphics, x, y, 25);
	}
}
