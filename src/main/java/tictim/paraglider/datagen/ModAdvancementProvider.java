package tictim.paraglider.datagen;

import net.minecraft.advancements.Advancement;
import net.minecraft.data.AdvancementProvider;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DirectoryCache;

import java.io.IOException;
import java.util.Collections;
import java.util.function.Consumer;

/**
 * Why in the fucking earth that list of advancements should be collected using predefined inaccessible private final immutable list? Excuse me?
 */
public abstract class ModAdvancementProvider extends AdvancementProvider{
	public ModAdvancementProvider(DataGenerator generatorIn){
		super(generatorIn);
	}

	@Override public void act(DirectoryCache cache) throws IOException{
		this.advancements = Collections.singletonList(consumer -> registerAdvancements(consumer));
		super.act(cache);
	}

	protected abstract void registerAdvancements(Consumer<Advancement> consumer);
}
