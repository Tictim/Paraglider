package tictim.paraglider.impl.vessel;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tictim.paraglider.api.Copy;
import tictim.paraglider.api.Serde;
import tictim.paraglider.api.vessel.VesselContainer;
import tictim.paraglider.config.Cfg;

/**
 * Standard implementation of {@link VesselContainer}.
 */
public class SimpleVesselContainer implements VesselContainer, Serde, Copy{
	protected final Player player;

	protected int heartContainer;
	protected int staminaVessel;
	protected int essence;

	public SimpleVesselContainer(@Nullable Player player){
		this.player = player;
	}

	@Override public final int heartContainer(){
		return heartContainer;
	}
	@Override public final int staminaVessel(){
		return staminaVessel;
	}
	@Override public final int essence(){
		return essence;
	}

	@Override @NotNull public SetResult setHeartContainer(int amount, boolean simulate, boolean playEffect){
		if(amount<0) return SetResult.TOO_LOW;
		if(amount>Cfg.get().maxHeartContainers()) return SetResult.TOO_HIGH;
		int change = amount-this.heartContainer;
		if(change==0) return SetResult.NO_CHANGE;
		if(!simulate){
			this.heartContainer = amount;
			onChange(ActionType.HEART_CONTAINER, change);
			if(playEffect) playEffect(ActionType.HEART_CONTAINER, change);
		}
		return SetResult.OK;
	}
	@Override @NotNull public SetResult setStaminaVessel(int amount, boolean simulate, boolean playEffect){
		if(amount<0) return SetResult.TOO_LOW;
		if(amount>Cfg.get().maxStaminaVessels()) return SetResult.TOO_HIGH;
		int change = amount-this.staminaVessel;
		if(change==0) return SetResult.NO_CHANGE;
		if(!simulate){
			this.staminaVessel = amount;
			onChange(ActionType.STAMINA_VESSEL, change);
			if(playEffect) playEffect(ActionType.STAMINA_VESSEL, change);
		}
		return SetResult.OK;
	}
	@Override @NotNull public SetResult setEssence(int amount, boolean simulate, boolean playEffect){
		if(amount<0) return SetResult.TOO_LOW;
		int change = amount-this.essence;
		if(change==0) return SetResult.NO_CHANGE;
		if(!simulate){
			this.essence = amount;
			onChange(ActionType.ESSENCE, change);
			if(playEffect) playEffect(ActionType.ESSENCE, change);
		}
		return SetResult.OK;
	}

	@Override public int giveHeartContainers(int amount, boolean simulate, boolean playEffect){
		amount = Math.min(amount, Cfg.get().maxHeartContainers()-this.heartContainer);
		if(amount<=0) return 0;
		if(!simulate){
			this.heartContainer += amount;
			onChange(ActionType.HEART_CONTAINER, amount);
			if(playEffect) playEffect(ActionType.HEART_CONTAINER, amount);
		}
		return amount;
	}
	@Override public int giveStaminaVessels(int amount, boolean simulate, boolean playEffect){
		amount = Math.min(amount, Cfg.get().maxStaminaVessels()-this.staminaVessel);
		if(amount<=0) return 0;
		if(!simulate){
			this.staminaVessel += amount;
			onChange(ActionType.STAMINA_VESSEL, amount);
			if(playEffect) playEffect(ActionType.STAMINA_VESSEL, amount);
		}
		return amount;
	}
	@Override public int giveEssences(int amount, boolean simulate, boolean playEffect){
		amount = Math.min(amount, Integer.MAX_VALUE-this.essence);
		if(amount<=0) return 0;
		if(!simulate){
			this.essence += amount;
			onChange(ActionType.ESSENCE, amount);
			if(playEffect) playEffect(ActionType.ESSENCE, amount);
		}
		return amount;
	}

	@Override public int takeHeartContainers(int amount, boolean simulate, boolean playEffect){
		amount = Math.min(amount, this.heartContainer);
		if(amount<=0) return 0;
		if(!simulate){
			this.heartContainer -= amount;
			onChange(ActionType.HEART_CONTAINER, -amount);
			if(playEffect) playEffect(ActionType.HEART_CONTAINER, -amount);
		}
		return amount;
	}
	@Override public int takeStaminaVessels(int amount, boolean simulate, boolean playEffect){
		amount = Math.min(amount, this.staminaVessel);
		if(amount<=0) return 0;
		if(!simulate){
			this.staminaVessel -= amount;
			onChange(ActionType.STAMINA_VESSEL, -amount);
			if(playEffect) playEffect(ActionType.STAMINA_VESSEL, -amount);
		}
		return amount;
	}
	@Override public int takeEssences(int amount, boolean simulate, boolean playEffect){
		amount = Math.min(amount, this.essence);
		if(amount<=0) return 0;
		if(!simulate){
			this.essence -= amount;
			onChange(ActionType.ESSENCE, -amount);
			if(playEffect) playEffect(ActionType.ESSENCE, -amount);
		}
		return amount;
	}

	@Override public void copyFrom(@NotNull Object from){
		if(!(from instanceof VesselContainer vessels)) return;
		setHeartContainer(vessels.heartContainer(), false, false);
		setStaminaVessel(vessels.staminaVessel(), false, false);
		setEssence(vessels.essence(), false, false);
	}

	protected void onChange(@NotNull ActionType actionType, int change){}

	protected void playEffect(@NotNull ActionType actionType, int change){
		if(change>0) switch(actionType){
			case HEART_CONTAINER -> spawnParticle(ParticleTypes.HEART, 5+5*change);
			case STAMINA_VESSEL -> spawnParticle(ParticleTypes.HAPPY_VILLAGER, 7+7*change);
		}
	}

	protected void spawnParticle(@NotNull ParticleOptions particle, int count){
		if(player!=null&&player.level() instanceof ServerLevel sl){
			sl.sendParticles(particle, player.getX(), player.getY(.5), player.getZ(), count, 1, 2, 1, 0);
		}
	}

	@Override @NotNull public CompoundTag write(){
		CompoundTag tag = new CompoundTag();
		tag.putInt("heartContainers", this.heartContainer);
		tag.putInt("staminaVessels", this.staminaVessel);
		tag.putInt("essences", this.essence);
		return tag;
	}

	@Override public void read(@NotNull CompoundTag tag){
		this.heartContainer = tag.getInt("heartContainers");
		this.staminaVessel = tag.getInt("staminaVessels");
		this.essence = tag.getInt("essences");
	}

	@Override public String toString(){
		return "SimpleVesselContainer{"+
				"heartContainer="+heartContainer+
				", staminaVessel="+staminaVessel+
				", essence="+essence+
				'}';
	}

	public enum ActionType{
		HEART_CONTAINER,
		STAMINA_VESSEL,
		ESSENCE
	}
}
