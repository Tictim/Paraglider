package tictim.paraglider.client;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tictim.paraglider.ParagliderUtils;

public class ParagliderClientItemExtension implements IClientItemExtensions {
	public static final ParagliderClientItemExtension INSTANCE = new ParagliderClientItemExtension();

	@Override public HumanoidModel.@Nullable ArmPose getArmPose(
			@NotNull LivingEntity entityLiving, @NotNull InteractionHand hand, @NotNull ItemStack itemStack) {
		return ParagliderUtils.getCaps(itemStack).isParagliding(itemStack) ? ParaglidingArmPose.ENUM.getValue() : null;
	}
}
