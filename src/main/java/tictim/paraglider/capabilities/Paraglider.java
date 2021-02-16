package tictim.paraglider.capabilities;

import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nullable;

public class Paraglider implements ICapabilityProvider{
	@CapabilityInject(Paraglider.class)
	public static Capability<Paraglider> CAP = null;

	public boolean isParagliding;

	private final LazyOptional<Paraglider> self = LazyOptional.of(() -> this);

	@Override public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction side){
		return cap==CAP ? self.cast() : LazyOptional.empty();
	}

	static boolean isParaglider(ItemStack stack){
		return CAP!=null&&!stack.isEmpty()&&stack.getCapability(CAP).isPresent();
	}
}
