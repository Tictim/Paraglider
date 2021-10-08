package tictim.paraglider.datagen;

import net.minecraft.advancements.Advancement;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.HashCache;
import net.minecraft.data.advancements.AdvancementProvider;
import net.minecraftforge.common.data.ExistingFileHelper;

import java.util.Collections;
import java.util.function.Consumer;

/**
 * Why in the fucking earth that list of advancements should be collected using predefined inaccessible private final immutable list? Excuse me?
 */
public abstract class ModAdvancementProvider extends AdvancementProvider{
	public ModAdvancementProvider(DataGenerator generator, ExistingFileHelper existingFileHelper){
		super(generator, existingFileHelper);
	}

	@Override public void run(HashCache cache){
		this.tabs = Collections.singletonList(consumer -> registerAdvancements(consumer));
		super.run(cache);
	}

	protected abstract void registerAdvancements(Consumer<Advancement> consumer);
}
