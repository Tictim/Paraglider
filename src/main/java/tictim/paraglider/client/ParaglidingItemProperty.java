package tictim.paraglider.client;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.ItemPropertyFunction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tictim.paraglider.ParagliderUtils;
import tictim.paraglider.contents.ParagliderTags;

@SuppressWarnings("deprecation")
public final class ParaglidingItemProperty implements ItemPropertyFunction {
	public static final ParaglidingItemProperty INSTANCE = new ParaglidingItemProperty();

	@Override public float call(@NotNull ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity entity, int seed) {
		return entity instanceof Player &&
				stack.is(ParagliderTags.PARAGLIDERS) &&
				ParagliderUtils.getCaps(stack).isParagliding(stack) ? 1 : 0;
	}
}
