package tictim.paraglider.client.dialog;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class Transition{
	private final List<Runnable> listeners = new ArrayList<>();
	private boolean finished;

	public void then(Runnable listener){
		if(finished) listener.run();
		else this.listeners.add(Objects.requireNonNull(listener));
	}

	public void finish(){
		if(finished) throw new IllegalStateException("Already finished");
		finished = true;
		for(Runnable listener : listeners){
			listener.run();
		}
	}

	public boolean isFinished(){
		return finished;
	}
}
