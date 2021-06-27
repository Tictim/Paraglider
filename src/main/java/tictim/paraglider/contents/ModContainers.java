package tictim.paraglider.contents;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import tictim.paraglider.recipe.bargain.BargainResult;
import tictim.paraglider.recipe.bargain.BargainResult.FailedReason;
import tictim.paraglider.recipe.bargain.StatueBargain;
import tictim.paraglider.recipe.bargain.StatueBargainContainer;
import tictim.paraglider.utils.StatueDialog;

import javax.annotation.Nullable;
import java.util.function.BiPredicate;

public final class ModContainers{ // TODO complete the dialog
	private ModContainers(){}

	private static final BiPredicate<StatueBargain, BargainResult> RECIPE_GIVES_HEART = (b, r) -> b.givesHeartContainer();
	private static final BiPredicate<StatueBargain, BargainResult> RECIPE_GIVES_STAMINA = (b, r) -> b.givesStaminaVessel();
	private static final BiPredicate<StatueBargain, BargainResult> RECIPE_CONSUMES_HEART = (b, r) -> b.consumesHeartContainer();
	private static final BiPredicate<StatueBargain, BargainResult> RECIPE_CONSUMES_STAMINA = (b, r) -> b.consumesStaminaVessel();

	private static final BiPredicate<StatueBargain, BargainResult> NOT_ENOUGH_ITEMS = (b, r) -> r.has(FailedReason.NOT_ENOUGH_ITEMS);
	private static final BiPredicate<StatueBargain, BargainResult> NOT_ENOUGH_HEART = (b, r) -> r.has(FailedReason.NOT_ENOUGH_HEART);
	private static final BiPredicate<StatueBargain, BargainResult> NOT_ENOUGH_STAMINA = (b, r) -> r.has(FailedReason.NOT_ENOUGH_STAMINA);
	private static final BiPredicate<StatueBargain, BargainResult> NOT_ENOUGH_ESSENCE = (b, r) -> r.has(FailedReason.NOT_ENOUGH_ESSENCE);
	private static final BiPredicate<StatueBargain, BargainResult> HEART_FULL = (b, r) -> r.has(FailedReason.HEART_FULL);
	private static final BiPredicate<StatueBargain, BargainResult> STAMINA_FULL = (b, r) -> r.has(FailedReason.STAMINA_FULL);
	private static final BiPredicate<StatueBargain, BargainResult> ESSENCE_FULL = (b, r) -> r.has(FailedReason.ESSENCE_FULL);

	private static final BiPredicate<StatueBargain, BargainResult> HEART_OR_STAMINA_FULL = (b, r) -> r.has(FailedReason.HEART_FULL)|r.has(FailedReason.STAMINA_FULL);

	private static final StatueDialog GODDESS_STATUE_DIALOG = new StatueDialog()
			.atInitial("bargain.dialog.goddess_statue.initial.0")
			.atInitial("bargain.dialog.goddess_statue.initial.1")
			.atSuccess("bargain.dialog.goddess_statue.success.0")
			.atSuccess("bargain.dialog.goddess_statue.success.heart.0", RECIPE_GIVES_HEART)
			.atSuccess("bargain.dialog.goddess_statue.success.heart.1", RECIPE_GIVES_HEART)
			.atSuccess("bargain.dialog.goddess_statue.success.stamina.0", RECIPE_GIVES_STAMINA)
			.atSuccess("bargain.dialog.goddess_statue.success.stamina.1", RECIPE_GIVES_STAMINA)
			.atFailure("bargain.dialog.goddess_statue.failure.not_enough_items.0", NOT_ENOUGH_ITEMS)
			.atFailure("bargain.dialog.goddess_statue.failure.not_enough_items.1", NOT_ENOUGH_ITEMS)
			.atFailure("bargain.dialog.goddess_statue.failure.not_enough_items.2", NOT_ENOUGH_ITEMS)
			.atFailure("bargain.dialog.goddess_statue.failure.full.0", HEART_OR_STAMINA_FULL)
			.atFailure("bargain.dialog.goddess_statue.failure.heart_full.0", HEART_FULL)
			.atFailure("bargain.dialog.goddess_statue.failure.heart_full.1", HEART_FULL)
			.atFailure("bargain.dialog.goddess_statue.failure.stamina_full.0", STAMINA_FULL)
			.atFailure("bargain.dialog.goddess_statue.failure.stamina_full.1", STAMINA_FULL)
			.atFailureFallback("bargain.dialog.goddess_statue.failure.fallback.0");
	private static final StatueDialog HORNED_STATUE_DIALOG = new StatueDialog()
			.atInitial("bargain.dialog.horned_statue.initial.0")
			.atInitial("bargain.dialog.horned_statue.initial.1")
			.atSuccess("bargain.dialog.horned_statue.success.0")
			.atSuccess("bargain.dialog.horned_statue.success.1")
			.atSuccess("bargain.dialog.horned_statue.success.consumes_heart.0", RECIPE_CONSUMES_HEART)
			.atSuccess("bargain.dialog.horned_statue.success.consumes_heart.1", RECIPE_CONSUMES_HEART)
			.atSuccess("bargain.dialog.horned_statue.success.consumes_stamina.0", RECIPE_CONSUMES_STAMINA)
			.atSuccess("bargain.dialog.horned_statue.success.consumes_stamina.1", RECIPE_CONSUMES_STAMINA)
			.atFailure("bargain.dialog.horned_statue.failure.not_enough_items.0", NOT_ENOUGH_ITEMS)
			.atFailure("bargain.dialog.horned_statue.failure.not_enough_heart.0", NOT_ENOUGH_HEART)
			.atFailure("bargain.dialog.horned_statue.failure.not_enough_heart.1", NOT_ENOUGH_HEART)
			.atFailure("bargain.dialog.horned_statue.failure.not_enough_stamina.0", NOT_ENOUGH_STAMINA)
			.atFailure("bargain.dialog.horned_statue.failure.not_enough_stamina.1", NOT_ENOUGH_STAMINA)
			.atFailure("bargain.dialog.horned_statue.failure.not_enough_essence.0", NOT_ENOUGH_ESSENCE)
			.atFailure("bargain.dialog.horned_statue.failure.not_enough_essence.1", NOT_ENOUGH_ESSENCE)
			.atFailure("bargain.dialog.horned_statue.failure.heart_full.0", HEART_FULL)
			.atFailure("bargain.dialog.horned_statue.failure.stamina_full.0", STAMINA_FULL)
			.atFailure("bargain.dialog.horned_statue.failure.essence_full.0", ESSENCE_FULL)
			.atFailureFallback("bargain.dialog.horned_statue.failure.fallback.0");

	public static StatueBargainContainer goddessStatue(int windowId, PlayerInventory playerInventory){
		return new StatueBargainContainer(Contents.GODDESS_STATUE_CONTAINER.get(),
				windowId,
				playerInventory,
				GODDESS_STATUE_DIALOG,
				ModAdvancements.PRAY_TO_THE_GODDESS);
	}
	public static StatueBargainContainer hornedStatue(int windowId, PlayerInventory playerInventory){
		return new StatueBargainContainer(Contents.HORNED_STATUE_CONTAINER.get(),
				windowId,
				playerInventory,
				HORNED_STATUE_DIALOG,
				ModAdvancements.STATUES_BARGAIN);
	}

	public static void openContainer(PlayerEntity player, ContainerFactory<? extends StatueBargainContainer> containerFactory, double lookAtX, double lookAtY, double lookAtZ){
		openContainer(player, containerFactory, new Vector3d(lookAtX, lookAtY, lookAtZ));
	}

	public static void openContainer(PlayerEntity player, ContainerFactory<? extends StatueBargainContainer> containerFactory, @Nullable Vector3d lookAt){
		player.openContainer(new INamedContainerProvider(){
			@Override public ITextComponent getDisplayName(){
				return StringTextComponent.EMPTY;
			}
			@Nullable @Override public Container createMenu(int windowId, PlayerInventory playerInventory, PlayerEntity playerEntity){
				StatueBargainContainer container = containerFactory.create(windowId, playerInventory);
				if(container.getBargains().isEmpty()) return null;
				if(lookAt!=null) container.setLookAt(lookAt);
				return container;
			}
		});
	}

	@FunctionalInterface
	public interface ContainerFactory<C extends Container>{
		C create(int windowId, PlayerInventory playerInventory);
	}
}
