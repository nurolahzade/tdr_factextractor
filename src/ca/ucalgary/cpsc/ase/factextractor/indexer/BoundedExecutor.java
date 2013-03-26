package ca.ucalgary.cpsc.ase.factextractor.indexer;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import ca.mcgill.cs.swevo.ppa.util.PPACoreSingleton;
import ca.ucalgary.cpsc.ase.common.ServiceProxy;
import ca.ucalgary.cpsc.ase.common.entity.RepositoryFile;
import ca.ucalgary.cpsc.ase.common.entity.SourceFile;
import ca.ucalgary.cpsc.ase.common.service.ServiceWrapperRemote;
import ca.ucalgary.cpsc.ase.common.service.RepositoryFileServiceRemote;
import ca.ucalgary.cpsc.ase.common.service.SourceFileServiceRemote;

public class BoundedExecutor {
    private final ExecutorService pool;
    private String root;
    private final Semaphore semaphore;
    private ThreadPoolMonitor poolMonitor;
    private SourceFileServiceRemote sourceService;
    private RepositoryFileServiceRemote repositoryService;
    
	private static Logger logger = Logger.getLogger(BoundedExecutor.class);
    
    public BoundedExecutor(String path, int bound) throws Exception {
		this.root = path;
    	this.pool = Executors.newFixedThreadPool(bound);
        this.semaphore = new Semaphore(bound);
        this.sourceService = ServiceProxy.getSourceFileService();
        this.repositoryService = ServiceProxy.getRepositoryFileService();
        initPPAEngine(bound);
        initThreadPoolMonitor();
    }
    
    private void initPPAEngine(int size) {
    	PPACoreSingleton.getInstance(size, PPACoreSingleton.DEFAULT_PROJECT_NAME);
    }
    
    private void initThreadPoolMonitor() {
        poolMonitor = new ThreadPoolMonitor(3 * 60, TimeUnit.SECONDS);
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
                    	SourceFile source = sourceService.create(null, file.getPath());
                    	new Indexer(source, root + file.getPath()).run();
                    	repositoryService.visit(file);
                    	logger.debug("Indexed: " + file.getPath());
                    } catch (Throwable t) {
                    	repositoryService.skip(file);
                    	logger.warn("Exception when indexing: " + file.getPath(), t);
                    } finally {                		
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
