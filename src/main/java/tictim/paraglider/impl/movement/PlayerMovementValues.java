package tictim.paraglider.impl.movement;

import net.minecraft.resources.Identifier;
import tictim.paraglider.api.ParagliderAPI;

public interface PlayerMovementValues {
	int PANIC_INITIAL_DELAY = 20;
	int PANIC_DELAY = 30;
	int PANIC_DURATION = 15;
	Identifier HEART_CONTAINER_ATTRIBUTE_ID = ParagliderAPI.id("heart_container");
	Identifier STAMINA_VESSEL_ATTRIBUTE_ID = ParagliderAPI.id("stamina_vessel");
	float AUTO_PARAGLIDING_FALL_DISTANCE = 1.45f;
	int PARAGLIDER_ITEM_COOLDOWN = 5;
}
