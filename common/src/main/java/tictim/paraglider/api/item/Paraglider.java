package tictim.paraglider.api.item;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * Interface for paraglider items. Items implementing this interface will be considered as Paragliders.
 */
public interface Paraglider{
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
	 * @see tictim.paraglider.ParagliderUtils#damageItemWithoutBreaking(Player, ItemStack)
	 */
	void damageParaglider(@NotNull Player player, @NotNull ItemStack stack);
}
