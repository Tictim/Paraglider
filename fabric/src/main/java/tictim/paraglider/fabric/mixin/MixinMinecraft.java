package tictim.paraglider.fabric.mixin;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tictim.paraglider.ParagliderMod;
import tictim.paraglider.ParagliderUtils;
import tictim.paraglider.client.screen.ParagliderSettingScreen;
import tictim.paraglider.wind.Wind;

@Mixin(Minecraft.class)
public abstract class MixinMinecraft{
	@Shadow public ClientLevel level;

	@Shadow public abstract void setScreen(Screen screen);

	@Inject(at = @At("HEAD"), method = "handleKeybinds()V")
	public void onHandleKeybinds(CallbackInfo info){
		if(Screen.hasControlDown()){
			// I won't bow down to you and your evil scheme to prevent me from using CTRL+P you satanic company that goes by the name of Mojang
			// replace paraglider settings keymapping instance with registered keymapping instance with identical input key
			// to bypass keybinding collision (which most likely would happen because P is already occupied by
			// social interactions key added by vanilla Minecraft. wtf does social interactions key do anyway????)
			InputConstants.Key key = ParagliderUtils.getKey(ParagliderMod.instance().getParagliderSettingsKey());
			KeyMapping keyMapping = KeyMapping.MAP.get(key);
			if(keyMapping==null) return;
			if(keyMapping.consumeClick()){
				// shut up intellij idea
				//noinspection StatementWithEmptyBody
				while(keyMapping.consumeClick()) ; // clear all clicks, idk whatever
				setScreen(new ParagliderSettingScreen());
			}
		}
	}

	@Inject(at = @At("HEAD"), method = "setLevel(Lnet/minecraft/client/multiplayer/ClientLevel;)V")
	public void onSetLevel(ClientLevel level, CallbackInfo info){
		Wind.registerLevel(level);
		if(this.level!=null) Wind.unregisterLevel(this.level);
	}

	@Inject(at = @At("HEAD"), method = "clearClientLevel(Lnet/minecraft/client/gui/screens/Screen;)V")
	public void onClearLevel(Screen screen, CallbackInfo info){
		if(this.level!=null) Wind.unregisterLevel(this.level);
	}
}
