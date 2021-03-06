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
		super("ThreadPool Monitor");
		loop = true;
		this.timeout = unit.toMillis(timeout);
		this.tasks = Collections.synchronizedMap(new HashMap<Integer, Monitor>());
	}
	
	public void preregister(Integer id) {
		register(id, null);
	}
	
	public void register(Integer id, Monitor monitor) {
		synchronized (tasks) {
			tasks.put(id, monitor);			
		}
	}
	
	public void unregister(Integer id) {
		synchronized (tasks) {
			tasks.remove(id);			
		}
	}

	@Override
	public void run() {
		while (loop) {
			Set<Entry<Integer, Monitor>> entries = tasks.entrySet();
			synchronized (tasks) {
				Iterator<Entry<Integer, Monitor>> iterator = entries.iterator();
				while (iterator.hasNext()) {
					Entry<Integer, Monitor> entry = iterator.next();
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
		synchronized (tasks) {
			return tasks.keySet().contains(id);			
		}
	}
	
	public void shutdown() {
		tasks.clear();
		loop = false;
	}
	
}
