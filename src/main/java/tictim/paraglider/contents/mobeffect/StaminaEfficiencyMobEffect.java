package tictim.paraglider.contents.mobeffect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import tictim.paraglider.api.ParagliderAPI;
import tictim.paraglider.contents.Contents;

public class StaminaEfficiencyMobEffect extends MobEffect {
	public static final double EFFICIENCY_PER_LEVEL = 0.5;

	public StaminaEfficiencyMobEffect() {
		super(MobEffectCategory.BENEFICIAL, 0xff00df53);
		addAttributeModifier(Contents.get().staminaEfficiency(),
				ParagliderAPI.id("stamina_efficiency"),
				EFFICIENCY_PER_LEVEL,
				AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
	}
}
