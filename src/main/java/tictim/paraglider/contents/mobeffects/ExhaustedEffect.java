package tictim.paraglider.contents.mobeffects;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.EffectRenderer;

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

	@Override public void initializeClient(Consumer<EffectRenderer> consumer){
		consumer.accept(ExhaustedEffectRenderer.INSTANCE);
	}

	public static final class ExhaustedEffectRenderer extends EffectRenderer{
		public static final ExhaustedEffectRenderer INSTANCE = new ExhaustedEffectRenderer();

		private ExhaustedEffectRenderer(){}

		@Override public boolean shouldRender(MobEffectInstance effect){
			return false;
		}
		@Override public boolean shouldRenderInvText(MobEffectInstance effect){
			return false;
		}
		@Override public boolean shouldRenderHUD(MobEffectInstance effect){
			return false;
		}

		@Override public void renderInventoryEffect(MobEffectInstance effectInstance, EffectRenderingInventoryScreen<?> gui, PoseStack poseStack, int x, int y, float z){}
		@Override public void renderHUDEffect(MobEffectInstance effectInstance, GuiComponent gui, PoseStack poseStack, int x, int y, float z, float alpha){}
	}
}
