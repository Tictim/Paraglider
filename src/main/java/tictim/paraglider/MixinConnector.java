package tictim.paraglider;

import org.spongepowered.asm.mixin.Mixins;
import org.spongepowered.asm.mixin.connect.IMixinConnector;

public class MixinConnector implements IMixinConnector{
	@Override public void connect(){
		Mixins.addConfiguration("assets/paraglider/mixin.json");
	}
}
