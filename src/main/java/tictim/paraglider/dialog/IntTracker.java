package tictim.paraglider.dialog;

import net.minecraft.entity.player.PlayerEntity;

import java.util.Objects;
import java.util.function.ToIntFunction;
import java.util.regex.Pattern;

public final class IntTracker{
	public static final String NAME_REGEX = "[a-zA-Z0-9_. -]+";
	public static final Pattern NAME_REGEX_PATTERN = Pattern.compile(NAME_REGEX);

	private final String name;
	private final int index;
	private final ToIntFunction<PlayerEntity> getter;

	public IntTracker(String name, int index, ToIntFunction<PlayerEntity> getter){
		if(!NAME_REGEX_PATTERN.matcher(name).matches()) throw new IllegalArgumentException("Name of IntTracker should match regex pattern "+NAME_REGEX);
		this.name = name;
		this.index = index;
		this.getter = Objects.requireNonNull(getter);
	}

	public String getName(){
		return name;
	}
	public int getIndex(){
		return index;
	}

	public int getCount(PlayerEntity player){
		return getter.applyAsInt(player);
	}

	@Override public String toString(){
		return this.getClass().getSimpleName()+" "+name+" "+index;
	}
}
