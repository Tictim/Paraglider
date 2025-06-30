package tictim.paraglider.api.item;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.ItemCapability;
import org.jetbrains.annotations.NotNull;
import tictim.paraglider.api.ParagliderAPI;
import tictim.paraglider.impl.DefaultParagliderItemCapability;

/**
 * <p>
 * Optional capability type for paraglider items. Note that this does not inherently give items functionality; the main
 * functionality is handled by item tag {@code paraglider:paragliders}.
 * </p>
 * <p>
 * You can attach a custom capability on your paraglider item to alter some of its behavior. If there's no capabilities,
 * Paraglider will use default implementation - see {@link DefaultParagliderItemCapability}.
 * </p>
 */
public interface Paraglider {
	ItemCapability<Paraglider, Void> CAPABILITY = ItemCapability.createVoid(ParagliderAPI.id("paraglider"), Paraglider.class);

	/**
	 * @return Default implementation
	 * @see DefaultParagliderItemCapability
	 */
	static @NotNull Paraglider defaultImpl() {
		return ParagliderAPI.defaultParagliderItemCapability();
	}

	/**
	 * Checks if the stack can perform paragliding. Note that this value is called
	 *
	 * @param stack Item stack
	 * @return Whether the stack can perform paragliding
	 */
	boolean canDoParagliding(@NotNull ItemStack stack);

	/**
	 * Checks if the paraglider is deployed, i.e. "paragliding". This method is used on client side, and the value set
	 * from {@link #setParagliding(ItemStack, boolean)} needs to be synced to client.
	 *
	 * @param stack Item stack
	 * @return Whether the paraglider is currently deployed
	 */
	boolean isParagliding(@NotNull ItemStack stack);

	/**
	 * Set the flag indicating whether the paraglider is currently deployed, i.e. "paragliding". This method is called
	 * on server side, and the value set from this method needs to be synced to client for
	 * {@link #isParagliding(ItemStack)}.
	 *
	 * @param stack       Item stack
	 * @param paragliding Whether the paraglider is currently deployed
	 */
	void setParagliding(@NotNull ItemStack stack, boolean paragliding);

	/**
	 * Damages the paraglider item. If it's not damageable, this method does nothing. The default logic for base mod
	 * paragliders do not destroy the item when broken.
	 *
	 * @param player Player
	 * @param stack  Item stack
	 */
	void damageParaglider(@NotNull Player player, @NotNull ItemStack stack);
}
