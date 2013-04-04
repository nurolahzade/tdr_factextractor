package ca.ucalgary.cpsc.ase.factextractor;

import java.io.File;
import java.util.List;
import java.util.concurrent.RejectedExecutionException;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;

import ca.ucalgary.cpsc.ase.common.ServiceProxy;
import ca.ucalgary.cpsc.ase.common.entity.Project;
import ca.ucalgary.cpsc.ase.common.entity.RepositoryFile;
import ca.ucalgary.cpsc.ase.common.entity.SourceFile;
import ca.ucalgary.cpsc.ase.common.service.RepositoryFileServiceRemote;
import ca.ucalgary.cpsc.ase.common.service.ServiceWrapperRemote;
import ca.ucalgary.cpsc.ase.factextractor.evaluation.ExperimentRunner;
import ca.ucalgary.cpsc.ase.factextractor.evaluation.RunnerVisitor;
import ca.ucalgary.cpsc.ase.factextractor.indexer.BoundedExecutor;
import ca.ucalgary.cpsc.ase.factextractor.visitor.SourceModel;
import ca.ucalgary.cpsc.ase.factextractor.visitor.TestVisitor;
import ca.ucalgary.cpsc.ase.factextractor.writer.IndexWriter;

public class Application implements IApplication {
	
	private static Logger logger = Logger.getLogger(Application.class);

	@Override
	public Object start(IApplicationContext context) throws Exception {
		String[] arguments = (String[]) context.getArguments().get("application.args");
		if (arguments.length != 1) {
			System.out.print("Usage: FactExtractor <path>");
			return IApplication.EXIT_OK;
		}
//		iterateFileSystem(arguments[0]);		
//		iterateWorkspace()
		evaluate(new File("C:\\Users\\mehrdad\\workspace\\Approximations\\src\\eval-config.xml"));
//		evaluate(new File("C:\\Users\\mehrdad\\workspace\\Approximations\\src"));
		
		return IApplication.EXIT_OK;
	}
	
	private void iterateWorkspace() throws Exception {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IWorkspaceRoot root = workspace.getRoot();
		// Get all projects in the workspace
		IProject[] projects = root.getProjects();
		
		ServiceWrapperRemote serviceWrapper = ServiceProxy.getServiceWrapper();
		SourceModel model = new SourceModel();
		
		// Loop over all projects
		for (IProject project : projects) {
			try {
				if (project.isNatureEnabled("org.eclipse.jdt.core.javanature")) {
					String projectName = project.getDescription().getName();
					Project prj = serviceWrapper.createProject(projectName);
					model.stepIntoProject(prj);
					logger.debug("Project: " + projectName);
					
					IPackageFragment[] packages = JavaCore.create(project)
							.getPackageFragments();
					// parse(JavaCore.create(project));
					for (IPackageFragment mypackage : packages) {
						if (mypackage.getKind() == IPackageFragmentRoot.K_SOURCE) {
							for (ICompilationUnit unit : mypackage
									.getCompilationUnits()) {
								String path = unit.getPath().toString();
								SourceFile source = serviceWrapper.createSourceFile(model.currentProject(), path);
								model.stepIntoSourceFile(source);
								logger.debug("File: " + path);
								
								// Now create the AST for the ICompilationUnits
								CompilationUnit parse = parse(unit);
								TestVisitor visitor = new TestVisitor(new IndexWriter(model));
								parse.accept(visitor);
							}
						}

					}
				}
			} catch (CoreException e) {
				logger.warn(e.getMessage());
			}
		}		
	}
	
	private void iterateFileSystem(String path) throws Exception {
		BoundedExecutor executor = new BoundedExecutor(path, 2);
				
		RepositoryFileServiceRemote repositoryService = ServiceProxy.getRepositoryFileService();
		List<RepositoryFile> unvisited;
		
		do {
			unvisited = repositoryService.findUnvisited(100);
			for (RepositoryFile file : unvisited) {
				try {
					if (!executor.isRunning(file.getId())) {
						executor.submit(file);						
					}
				} catch (RejectedExecutionException e) {
					logger.warn(e.getMessage());
				} catch (InterruptedException e) {
					logger.warn(e.getMessage());
				}
			}
			
		} while (unvisited.size() > 0);
		
		executor.shutdown();
	}
	
	private void evaluate(File file) throws Exception {
//		new RunnerVisitor(file).start();
		new ExperimentRunner(file);
	}
	
	private static CompilationUnit parse(ICompilationUnit unit) {
		ASTParser parser = ASTParser.newParser(AST.JLS3);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setSource(unit);
		parser.setResolveBindings(true);
		return (CompilationUnit) parser.createAST(null); // parse
	}
	

	@Override
	public void stop() {
		// TODO Auto-generated method stub		
	}

}
