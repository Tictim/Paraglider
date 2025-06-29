package tictim.paraglider.impl.movement;

import net.minecraft.resources.ResourceLocation;
import tictim.paraglider.api.ParagliderAPI;

public interface PlayerMovementValues {
	int PANIC_INITIAL_DELAY = 10;
	int PANIC_DELAY = 30;
	int PANIC_DURATION = 15;
	ResourceLocation HEART_CONTAINER_ATTRIBUTE_ID = ParagliderAPI.id("heart_container");
	ResourceLocation STAMINA_VESSEL_ATTRIBUTE_ID = ParagliderAPI.id("stamina_vessel");
	float PARAGLIDING_FALL_DISTANCE = 1.45f;
}
