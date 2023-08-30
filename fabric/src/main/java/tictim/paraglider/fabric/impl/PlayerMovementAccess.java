package tictim.paraglider.fabric.impl;

import org.jetbrains.annotations.NotNull;
import tictim.paraglider.impl.movement.PlayerMovement;

public interface PlayerMovementAccess{
	@NotNull PlayerMovement paragliderPlayerMovement();
}
