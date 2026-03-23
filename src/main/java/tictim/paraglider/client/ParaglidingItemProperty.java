package tictim.paraglider.client;

import com.mojang.serialization.MapCodec;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.properties.conditional.ConditionalItemModelProperty;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NullMarked;
import tictim.paraglider.ParagliderUtils;
import tictim.paraglider.contents.ParagliderTags;

@NullMarked
public final class ParaglidingItemProperty implements ConditionalItemModelProperty {
	public static final ParaglidingItemProperty INSTANCE = new ParaglidingItemProperty();
	public static final MapCodec<ParaglidingItemProperty> CODEC = MapCodec.unit(INSTANCE);

	@Override public MapCodec<? extends ConditionalItemModelProperty> type() {
		return CODEC;
	}

	@Override
	public boolean get(ItemStack stack, @Nullable ClientLevel level,
	                   @Nullable LivingEntity entity, int seed,
	                   ItemDisplayContext displayContext) {
		return entity instanceof Player &&
				stack.is(ParagliderTags.PARAGLIDERS) &&
				ParagliderUtils.getCaps(stack).isParagliding(stack);
	}
}
