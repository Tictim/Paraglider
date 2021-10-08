package tictim.paraglider.capabilities;

import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class Paraglider implements ICapabilityProvider{
	private final LazyOptional<Paraglider> self = LazyOptional.of(() -> this);

	@Nonnull @Override public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction side){
		return cap==Caps.paraglider ? self.cast() : LazyOptional.empty();
	}

	public static boolean isParaglider(ItemStack stack){
		return Caps.paraglider!=null&&!stack.isEmpty()&&(!stack.isDamageableItem()||stack.getDamageValue()<stack.getMaxDamage())&&stack.getCapability(Caps.paraglider).isPresent();
	}
}
