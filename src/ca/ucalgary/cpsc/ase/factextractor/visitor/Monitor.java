package ca.ucalgary.cpsc.ase.factextractor.visitor;

import java.util.concurrent.Future;

public class Monitor {

	private long start;
	private Future<?> future;
	
	public Monitor(Future<?> future) {
		this.start = System.currentTimeMillis();
		this.future = future;
	}
	
	public long getRunningTime() {
		return System.currentTimeMillis() - start;
	}

	public void timeout() {
		future.cancel(true);
	}
	
	public boolean isRunning() {
		return !future.isDone();
	}
	
}
