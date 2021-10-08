package tictim.paraglider.capabilities;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import tictim.paraglider.wind.Wind;

public final class Caps{
	private Caps(){}

	public static final Capability<PlayerMovement> playerMovement = CapabilityManager.get(new CapabilityToken<>(){});
	public static final Capability<Paraglider> paraglider = CapabilityManager.get(new CapabilityToken<>(){});
	public static final Capability<Wind> wind = CapabilityManager.get(new CapabilityToken<>(){});
	public static final Capability<Stamina> stamina = CapabilityManager.get(new CapabilityToken<>(){});
}
