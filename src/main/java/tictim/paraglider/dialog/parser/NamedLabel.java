package tictim.paraglider.dialog.parser;

import java.util.Objects;

public final class NamedLabel implements Label{
	private final String name;
	private boolean hasIndex;
	private int index;

	public NamedLabel(String name){
		this.name = name;
	}

	public void setIndex(int index){
		this.hasIndex = true;
		this.index = index;
	}

	@Override public int getIndex(){
		return hasIndex ? index : 0;
	}
	public boolean hasIndex(){
		return hasIndex;
	}

	@Override public boolean equals(Object o){
		if(this==o) return true;
		if(o==null||getClass()!=o.getClass()) return false;
		NamedLabel that = (NamedLabel)o;
		return name.equals(that.name);
	}
	@Override public int hashCode(){
		return Objects.hash(name);
	}

	@Override public String toString(){
		return "Label "+name;
	}
}
