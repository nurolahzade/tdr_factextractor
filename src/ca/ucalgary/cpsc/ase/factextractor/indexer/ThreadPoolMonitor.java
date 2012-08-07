package ca.ucalgary.cpsc.ase.factextractor.indexer;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
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
	}
	
	public void register(Integer id, Monitor monitor) {
		tasks.put(id, monitor);
	}
	
	public void unregister(Integer id) {
		tasks.remove(id);
	}

	@Override
	public void run() {
		while (loop) {
			Set<Entry<Integer, Monitor>> keys = tasks.entrySet();
			synchronized (tasks) {
				Iterator iterator = keys.iterator();
				while (iterator.hasNext()) {
					Entry<Integer, Monitor> entry = (Entry<Integer, Monitor>) iterator.next();
					Monitor monitor = entry.getValue();
					if (monitor != null && monitor.isRunning() && isTimedOut(monitor)) {
						monitor.timeout();
						iterator.remove();
					}
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
