package tictim.paraglider.client;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.ClampedItemPropertyFunction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tictim.paraglider.api.item.Paraglider;

public final class ParagliderItemProperty implements ClampedItemPropertyFunction{
	private ParagliderItemProperty(){}

	public static final ResourceLocation KEY_PARAGLIDING = new ResourceLocation("paragliding");

	private static final ParagliderItemProperty instance = new ParagliderItemProperty();

	@NotNull public static ClampedItemPropertyFunction get(){
		return instance;
	}

	@Override public float unclampedCall(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity entity, int tintIndex){
		return entity instanceof Player&&stack.getItem() instanceof Paraglider p&&p.isParagliding(stack) ? 1 : 0;
	}
}
