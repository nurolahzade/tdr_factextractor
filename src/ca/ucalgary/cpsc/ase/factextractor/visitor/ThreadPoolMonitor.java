package ca.ucalgary.cpsc.ase.factextractor.visitor;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

public class ThreadPoolMonitor extends Thread {

	private boolean loop;
	private long timeout;
	private Map<Integer, Monitor> tasks; 
	
	public ThreadPoolMonitor(long timeout, TimeUnit unit) {
		super();
		loop = true;
		this.timeout = unit.toMillis(timeout);
		this.tasks = Collections.synchronizedMap(new HashMap<Integer, Monitor>());
	}
	
	public void preregister(Integer id) {
		tasks.put(id, null);
		System.out.println("ThreadPoolMonitor.preregister: " + id);
	}
	
	public void register(Integer id, Monitor monitor) {
		tasks.put(id, monitor);
		System.out.println("ThreadPoolMonitor.register: " + id);
	}
	
	public void unregister(Integer id) {
		tasks.remove(id);
		System.out.println("ThreadPoolMonitor.unregister: " + id);
	}

	@Override
	public void run() {
		while (loop) {
			for (Entry<Integer, Monitor> entry : tasks.entrySet()) {
				Monitor monitor = entry.getValue();
				if (monitor != null && monitor.isRunning() && isTimedOut(monitor)) {
					monitor.timeout();
					System.out.println("ThreadPoolMonitor.timeout: " + entry.getKey());
					unregister(entry.getKey());
				}
			}
			try {
				sleep(100);
			} catch (InterruptedException e) {
				// ignore
			}
		}
	}
	
	private boolean isTimedOut(Monitor monitor) {
		return monitor.getRunningTime() > timeout;
	}

	public boolean isRegistered(Integer id) {
		return tasks.keySet().contains(id);
	}
	
	public void shutdown() {
		tasks.clear();
		loop = false;
	}
	
}
