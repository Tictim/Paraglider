package tictim.paraglider.api;

import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.NotNull;
import tictim.paraglider.api.stamina.Stamina;

/**
 * Interface providing methods for de/serialization using {@link CompoundTag}. Custom implementations of
 * {@link Stamina} may implement this interface for saving and loading persistent data.
 */
public interface Serde{
	/**
	 * Read from the compound tag. Note that this method gets called even if there wasn't any tags previously written.
	 * The tag might contain other unknown properties if another implementation of this class existed before.
	 *
	 * @param tag Compound tag
	 */
	void read(@NotNull CompoundTag tag);
	/**
	 * Write to a new compound tag.
	 *
	 * @return Compound tag containing serialized data
	 */
	@NotNull CompoundTag write();
}
