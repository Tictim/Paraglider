package tictim.paraglider.contents;

import com.mojang.serialization.MapCodec;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.display.DisplayContentsFactory;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;
import java.util.function.Supplier;
import java.util.stream.Stream;

public enum VesselSlotDisplay implements SlotDisplay {
	HEART_CONTAINER(() -> Contents.get().heartContainer()),
	STAMINA_VESSEL(() -> Contents.get().staminaVessel()),
	ESSENCE(() -> Contents.get().essence());

	public final Type<VesselSlotDisplay> type = new Type<>(
			MapCodec.unit(this), StreamCodec.unit(this)
	);
	private final Supplier<Item> item;

	VesselSlotDisplay(Supplier<Item> item) {
		this.item = item;
	}

	@Override public <T> @NotNull Stream<T> resolve(@NotNull ContextMap context, @NotNull DisplayContentsFactory<T> output) {
		if (output instanceof DisplayContentsFactory.ForStacks<T> forStacks) {
			return Stream.of(new ItemStack(this.item.get())).map(forStacks::forStack);
		}

		return Stream.empty();
	}

	@Override public @NotNull Type<? extends SlotDisplay> type() {
		return this.type;
	}

	public static void register(DeferredRegister<Type<?>> register) {
		for (VesselSlotDisplay slotDisplay : values()) {
			Type<VesselSlotDisplay> type = slotDisplay.type;
			register.register(slotDisplay.name().toLowerCase(Locale.ROOT), () -> type);
		}
	}
}
