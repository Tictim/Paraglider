package tictim.paraglider.client;

import com.mojang.serialization.MapCodec;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.properties.conditional.ConditionalItemModelProperty;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tictim.paraglider.ParagliderUtils;
import tictim.paraglider.contents.ParagliderTags;

public final class ParaglidingItemProperty implements ConditionalItemModelProperty {
	public static final ParaglidingItemProperty INSTANCE = new ParaglidingItemProperty();
	public static final MapCodec<ParaglidingItemProperty> CODEC = MapCodec.unit(INSTANCE);

	@Override public @NotNull MapCodec<? extends ConditionalItemModelProperty> type() {
		return CODEC;
	}

	@Override
	public boolean get(@NotNull ItemStack stack, @Nullable ClientLevel level,
	                   @Nullable LivingEntity entity, int seed,
	                   @NotNull ItemDisplayContext displayContext) {
		return entity instanceof Player &&
				stack.is(ParagliderTags.PARAGLIDERS) &&
				ParagliderUtils.getCaps(stack).isParagliding(stack);
	}
}
