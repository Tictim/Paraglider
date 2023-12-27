package tictim.paraglider.fabric.event;

import com.google.common.base.Strings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import tictim.paraglider.ParagliderMod;
import tictim.paraglider.ParagliderUtils;
import tictim.paraglider.api.movement.Movement;
import tictim.paraglider.api.stamina.Stamina;
import tictim.paraglider.client.ParagliderClientSettings;
import tictim.paraglider.client.render.InGameStaminaWheelRenderer;
import tictim.paraglider.client.render.StaminaWheelRenderer;
import tictim.paraglider.client.screen.DisableStaminaRender;
import tictim.paraglider.config.DebugCfg;

import java.util.ArrayList;
import java.util.List;

import static tictim.paraglider.api.movement.ParagliderPlayerStates.Flags.FLAG_PARAGLIDING;
import static tictim.paraglider.client.render.StaminaWheelConstants.WHEEL_RADIUS;

public final class ParagliderClientEventHandler{
	private ParagliderClientEventHandler(){}

	private static final StaminaWheelRenderer staminaWheelRenderer = new InGameStaminaWheelRenderer();

	public static void renderHUD(GuiGraphics guiGraphics){
		renderDebugText(guiGraphics);
		renderStaminaWheel(guiGraphics);
	}

	private static void renderStaminaWheel(GuiGraphics guiGraphics){
		Minecraft mc = Minecraft.getInstance();
		if(mc.player==null||
				mc.screen instanceof DisableStaminaRender||
				!Stamina.get(mc.player).renderStaminaWheel()||
				!ParagliderMod.instance().getPlayerStateMap().hasStaminaConsumption()) return;

		ParagliderClientSettings settings = ParagliderClientSettings.get();
		int x = Mth.clamp((int)Math.round(settings.staminaWheelX()*guiGraphics.guiWidth()), 1+WHEEL_RADIUS, guiGraphics.guiWidth()-2-WHEEL_RADIUS);
		int y = Mth.clamp((int)Math.round(settings.staminaWheelY()*guiGraphics.guiHeight()), 1+WHEEL_RADIUS, guiGraphics.guiHeight()-2-WHEEL_RADIUS);

		staminaWheelRenderer.renderStamina(guiGraphics, x, y, 25);
	}

	private static void renderDebugText(GuiGraphics guiGraphics){
		if(!DebugCfg.get().debugPlayerMovement()) return;
		Minecraft mc = Minecraft.getInstance();
		if(mc.getDebugOverlay().showDebugScreen()) return; // handled by DebugScreenOverlay
		Player p = mc.player;
		if(p==null) return;

		List<String> list = new ArrayList<>();
		ParagliderUtils.addDebugText(p, list);

		renderLines(guiGraphics, mc.font, list);
	}

	// DebugScreenOverlay#renderLines
	private static void renderLines(GuiGraphics guiGraphics, Font font, List<String> list){
		for(int i = 0; i<list.size(); ++i){
			String string = list.get(i);
			if(Strings.isNullOrEmpty(string)) continue;
			int width = font.width(string);
			int x = guiGraphics.guiWidth()-2-width;
			int y = 2+font.lineHeight*i;
			guiGraphics.fill(x-1, y-1, x+width+1, y+font.lineHeight-1, 0x90505050);
		}
		for(int i = 0; i<list.size(); ++i){
			String string = list.get(i);
			if(Strings.isNullOrEmpty(string)) continue;
			int width = font.width(string);
			int x = guiGraphics.guiWidth()-2-width;
			int y = 2+font.lineHeight*i;
			guiGraphics.drawString(font, string, x, y, 0xE0E0E0, false);
		}
	}

	public static boolean beforeAttack(LocalPlayer player){
		return Movement.get(player).state().has(FLAG_PARAGLIDING);
	}

	public static boolean beforeBlockOutline(){
		Player player = Minecraft.getInstance().player;
		return player==null||!Movement.get(player).state().has(FLAG_PARAGLIDING);
	}
}
