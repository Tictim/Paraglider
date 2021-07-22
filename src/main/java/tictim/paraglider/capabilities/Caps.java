package tictim.paraglider.capabilities;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import tictim.paraglider.capabilities.wind.Wind;

public final class Caps{
	private Caps(){}

	@CapabilityInject(PlayerMovement.class)
	public static Capability<PlayerMovement> playerMovement = null;
	@CapabilityInject(Paraglider.class)
	public static Capability<Paraglider> paraglider = null;
	@CapabilityInject(Wind.class)
	public static Capability<Wind> wind = null;
	@CapabilityInject(Stamina.class)
	public static Capability<Stamina> stamina = null;
}
