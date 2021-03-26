package tictim.paraglider.dialog.parser;

public final class SimpleLabel implements Label{
	private int index;

	public SimpleLabel(int index){
		this.index = index;
	}

	@Override public int getIndex(){
		return index;
	}
	public void setIndex(int index){
		this.index = index;
	}

	@Override public String toString(){
		return "Label "+index;
	}
}
