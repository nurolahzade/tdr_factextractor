package ca.ucalgary.cpsc.ase.factextractor.visitor;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.Semaphore;

import ca.ucalgary.cpsc.ase.FactManager.entity.RepositoryFile;

public class BoundedExecutor {
    private final ExecutorService pool;
    private String root;
    private final Semaphore semaphore;
    private Set<Integer> commands; 
    
    public BoundedExecutor(String path, int bound) {
		this.root = path;
    	this.pool = Executors.newFixedThreadPool(10);
        this.semaphore = new Semaphore(bound);
        this.commands = Collections.synchronizedSet(new HashSet<Integer>());
    }

    public void submit(final RepositoryFile file)
            throws InterruptedException, RejectedExecutionException {
    	if (commands.contains(file.getId())) {
    		return;
    	}
    	
    	commands.add(file.getId());
        
        semaphore.acquire();
        try {
            pool.execute(new Runnable() {
                public void run() {
                    try {
                    	new Indexer(root, file).run();                    		
                    } finally {
                    	commands.remove(file.getId());
                        semaphore.release();                        
                    }
                }
            });
        } catch (RejectedExecutionException e) {
        	commands.remove(file.getId());
            semaphore.release();
            throw e;
        }        
    }
    
    public boolean isRunning(Integer id) {
    	return commands.contains(id);
    }
    
    public void shutdown() {
    	pool.shutdown();
    }
}
