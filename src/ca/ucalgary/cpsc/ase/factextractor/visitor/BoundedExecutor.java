package ca.ucalgary.cpsc.ase.factextractor.visitor;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import ca.ucalgary.cpsc.ase.FactManager.entity.RepositoryFile;
import ca.ucalgary.cpsc.ase.FactManager.service.RepositoryFileService;

public class BoundedExecutor {
    private final ExecutorService pool;
    private String root;
    private final Semaphore semaphore;
    private ThreadPoolMonitor poolMonitor;
    
	private static Logger logger = Logger.getLogger(BoundedExecutor.class);
    
    public BoundedExecutor(String path, int bound) {
		this.root = path;
    	this.pool = Executors.newFixedThreadPool(bound);
        this.semaphore = new Semaphore(bound);
        this.poolMonitor = new ThreadPoolMonitor(3 * 60, TimeUnit.SECONDS);
        poolMonitor.start();
    }

    public void submit(final RepositoryFile file)
            throws InterruptedException, RejectedExecutionException {
    	if (poolMonitor.isRegistered(file.getId())) {
    		return;
    	}
    	
    	poolMonitor.preregister(file.getId());
        
        semaphore.acquire();
        try {
        	final Future<?> future = pool.submit(new Runnable() {
                public void run() {
                    try {
                    	new Indexer(root, file).run();
                    	logger.debug("Indexed: " + file.getPath());
                    } catch (Throwable t) {
                    	// TODO create a SKIPPED state for RepositoryFile.visited attribute
                    	logger.warn("Exception when indexing: " + file.getPath(), t);
                    } finally {                		
                    	RepositoryFileService repositoryService = new RepositoryFileService();
                    	repositoryService.visit(file);
                    	poolMonitor.unregister(file.getId());
                        semaphore.release();                                	
                    }
                }
            });
        	Monitor monitor = new Monitor(future);
        	poolMonitor.register(file.getId(), monitor);
        } catch(RejectedExecutionException e) {
        	poolMonitor.unregister(file.getId());
            semaphore.release();                                	
        }
    }
    
    public boolean isRunning(Integer id) {
    	return poolMonitor.isRegistered(id);
    }
    
    public void shutdown() {
    	pool.shutdown();
    	poolMonitor.shutdown();
    }
}
