package tictim.paraglider.dialog;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.ToIntFunction;

/**
 * Instance representing specific dialog session and server-side actions used during the dialog.
 * The actual dialog is described in {@code Scenario} instance, which parsed from json and injected in clientside.
 */
public final class Dialog{
	private final String name;

	private final Map<String, DialogAction> dialogActions;
	private final List<IntTracker> trackedInts;

	@Nullable private final BiConsumer<DialogContainer, PlayerEntity> containerClosedListener;

	@Nullable private Scenario scenario;
	private final RegistryObject<ContainerType<DialogContainer>> registryObject;

	private Dialog(DeferredRegister<ContainerType<?>> deferredRegister,
	               String name,
	               Map<String, DialogAction> dialogActions,
	               List<IntTracker> trackedInts,
	               @Nullable BiConsumer<DialogContainer, PlayerEntity> containerClosedListener){
		this.name = Objects.requireNonNull(name);
		this.dialogActions = Collections.unmodifiableMap(dialogActions);
		this.trackedInts = Collections.unmodifiableList(trackedInts);
		this.containerClosedListener = containerClosedListener;

		this.registryObject = deferredRegister.register(name, () -> new ContainerType<>(this::createContainer));
	}

	public String getName(){
		return name;
	}

	@Nullable public Scenario getScenario(){
		return scenario;
	}

	public void setScenario(@Nullable Scenario scenario){
		this.scenario = scenario;
	}

	@Nullable public DialogAction getDialogAction(String id){
		return dialogActions.get(id);
	}

	public List<IntTracker> getTrackedInts(){
		return trackedInts;
	}

	@Nullable public IntTracker getIntTracker(String name){
		for(IntTracker trackedInt : trackedInts)
			if(trackedInt.getName().equals(name)) return trackedInt;
		return null;
	}

	public void onContainerClosed(DialogContainer container, PlayerEntity player){
		if(containerClosedListener!=null) containerClosedListener.accept(container, player);
	}

	public ContainerType<DialogContainer> getContainerType(){
		return registryObject.get();
	}

	public DialogContainer createContainer(int id, PlayerInventory playerInventory){
		return new DialogContainer(registryObject.get(), id, playerInventory, this);
	}

	public INamedContainerProvider getContainerProvider(@Nullable Vector3f lookAt){
		return new INamedContainerProvider(){
			@Override public ITextComponent getDisplayName(){
				return StringTextComponent.EMPTY;
			}
			@Override public Container createMenu(int windowId, PlayerInventory playerInventory, PlayerEntity playerEntity){
				DialogContainer container = createContainer(windowId, playerInventory);
				if(lookAt!=null) container.setLookAt(lookAt);
				return container;
			}
		};
	}

	public static Builder builder(String name){
		return new Builder(name);
	}

	@Override public boolean equals(Object o){
		if(this==o) return true;
		if(o==null||getClass()!=o.getClass()) return false;
		Dialog dialog = (Dialog)o;
		return name.equals(dialog.name);
	}
	@Override public int hashCode(){
		return Objects.hash(name);
	}

	public static final class Builder{
		private final String name;

		private final Map<String, DialogAction> dialogActions = new HashMap<>();
		private final List<IntTracker> trackedInts = new ArrayList<>();

		@Nullable private BiConsumer<DialogContainer, PlayerEntity> containerClosedListener;

		public Builder(String name){
			this.name = name;
		}

		public Builder addDialogAction(String id, DialogAction.Function action){
			if(dialogActions.containsKey(id)) throw new IllegalStateException("Duplicated DialogAction registration for id "+id);
			dialogActions.put(id, new DialogAction(id, action));
			return this;
		}

		public Builder trackInt(String name, ToIntFunction<PlayerEntity> getter){
			trackedInts.add(new IntTracker(name, trackedInts.size(), getter));
			return this;
		}

		public Builder onContainerClosed(@Nullable BiConsumer<DialogContainer, PlayerEntity> containerClosedListener){
			this.containerClosedListener = containerClosedListener;
			return this;
		}

		public Dialog build(DeferredRegister<ContainerType<?>> deferredRegister){
			return new Dialog(deferredRegister, name, dialogActions, trackedInts, containerClosedListener);
		}
	}
}
