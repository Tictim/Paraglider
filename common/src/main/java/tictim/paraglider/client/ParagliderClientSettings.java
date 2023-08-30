package tictim.paraglider.client;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.Type;
import tictim.paraglider.ParagliderMod;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public final class ParagliderClientSettings{
	@NotNull public static ParagliderClientSettings get(){
		return ParagliderMod.instance().getClientSettings();
	}

	private static final double DEFAULT_STAMINA_WHEEL_X = (427-100)/854.0;
	private static final double DEFAULT_STAMINA_WHEEL_Y = (240-15)/480.0;
	private static final double DEFAULT_PARTICLE_FREQ = 1;

	@NotNull private final Path configPath;
	private double staminaWheelX = DEFAULT_STAMINA_WHEEL_X;
	private double staminaWheelY = DEFAULT_STAMINA_WHEEL_Y;

	private double windParticleFrequency = DEFAULT_PARTICLE_FREQ;

	public ParagliderClientSettings(@NotNull Path gameDirectory){
		this.configPath = gameDirectory.resolve("paragliderSettings.nbt");
	}

	@NotNull public Path configPath(){
		return configPath;
	}

	public double staminaWheelX(){
		return staminaWheelX;
	}
	public void setStaminaWheelX(double staminaWheelX){
		this.staminaWheelX = filterBadValue(staminaWheelX, DEFAULT_STAMINA_WHEEL_X);
	}
	public double staminaWheelY(){
		return staminaWheelY;
	}
	public void setStaminaWheelY(double staminaWheelY){
		this.staminaWheelY = filterBadValue(staminaWheelY, DEFAULT_STAMINA_WHEEL_Y);
	}
	public double windParticleFrequency(){
		return windParticleFrequency;
	}
	public void setWindParticleFrequency(double windParticleFrequency){
		this.windParticleFrequency = filterBadValue(windParticleFrequency, DEFAULT_PARTICLE_FREQ);
	}

	private static double filterBadValue(double d, double defaultValue){
		if(Double.isNaN(d)) return defaultValue;
		return Mth.clamp(d, 0, 1);
	}

	public void setStaminaWheel(double staminaWheelX, double staminaWheelY){
		setStaminaWheelX(staminaWheelX);
		setStaminaWheelY(staminaWheelY);
	}

	public boolean load(){
		try{
			if(Files.exists(this.configPath)){
				try(DataInputStream dis = new DataInputStream(Files.newInputStream(this.configPath))){
					CompoundTag tag = NbtIo.read(dis);
					CompoundTag staminaWheel = tag.getCompound("staminaWheel");
					setStaminaWheelX(staminaWheel.getDouble("x"));
					setStaminaWheelY(staminaWheel.getDouble("y"));
					setWindParticleFrequency(tag.contains("windParticleFreq", Type.DOUBLE) ? tag.getDouble("windParticleFreq") : DEFAULT_PARTICLE_FREQ);
				}
			}else{
				setStaminaWheelX(DEFAULT_STAMINA_WHEEL_X);
				setStaminaWheelY(DEFAULT_STAMINA_WHEEL_Y);
				setWindParticleFrequency(DEFAULT_PARTICLE_FREQ);
			}
			return true;
		}catch(RuntimeException|IOException ex){
			ParagliderMod.LOGGER.error("Error occurred while loading paraglider settings: ", ex);
			return false;
		}
	}

	public boolean save(){
		try{
			CompoundTag tag = new CompoundTag();
			CompoundTag staminaWheel = new CompoundTag();
			staminaWheel.putDouble("x", staminaWheelX());
			staminaWheel.putDouble("y", staminaWheelY());
			tag.put("staminaWheel", staminaWheel);
			tag.putDouble("windParticleFreq", windParticleFrequency());

			try(DataOutputStream dos = new DataOutputStream(
					Files.newOutputStream(this.configPath, StandardOpenOption.CREATE))){
				NbtIo.write(tag, dos);
			}
			return true;
		}catch(RuntimeException|IOException ex){
			ParagliderMod.LOGGER.error("Error occurred while saving paraglider settings: ", ex);
			return false;
		}
	}

	@Override public String toString(){
		return "ParagliderClientSettings{"+
				"configPath="+configPath+
				", staminaWheelX="+staminaWheelX+
				", staminaWheelY="+staminaWheelY+
				", windParticleFrequency="+windParticleFrequency+
				'}';
	}
}
