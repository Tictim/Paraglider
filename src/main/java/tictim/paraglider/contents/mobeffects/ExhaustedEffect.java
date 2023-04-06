package tictim.paraglider.contents.mobeffects;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.extensions.common.IClientMobEffectExtensions;

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public class ExhaustedEffect extends MobEffect{
	public ExhaustedEffect(){
		super(MobEffectCategory.HARMFUL, 0x5A6C81); // Slowness color
		addAttributeModifier(Attributes.MOVEMENT_SPEED, "65ed2ca4-ceb3-4521-8552-73006dcba58d", -0.30, AttributeModifier.Operation.MULTIPLY_TOTAL);
	}

	@Override public List<ItemStack> getCurativeItems(){
		return Collections.emptyList();
	}

	@Override public void initializeClient(Consumer<IClientMobEffectExtensions> consumer){
		consumer.accept(MobEffectExtension.INSTANCE);
	}

	public enum MobEffectExtension implements IClientMobEffectExtensions{
		INSTANCE;

		@Override public boolean isVisibleInInventory(MobEffectInstance instance){
			return false;
		}
		@Override public boolean isVisibleInGui(MobEffectInstance instance){
			return false;
		}
	}
}
