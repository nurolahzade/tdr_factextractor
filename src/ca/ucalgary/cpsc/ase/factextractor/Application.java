package ca.ucalgary.cpsc.ase.factextractor;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
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

import ca.ucalgary.cpsc.ase.FactManager.entity.Project;
import ca.ucalgary.cpsc.ase.FactManager.entity.RepositoryFile;
import ca.ucalgary.cpsc.ase.FactManager.entity.SourceFile;
import ca.ucalgary.cpsc.ase.FactManager.service.ProjectService;
import ca.ucalgary.cpsc.ase.FactManager.service.RepositoryFileService;
import ca.ucalgary.cpsc.ase.FactManager.service.SourceFileService;
import ca.ucalgary.cpsc.ase.factextractor.composer.QueryGeneratorTest;
import ca.ucalgary.cpsc.ase.factextractor.visitor.BoundedExecutor;
import ca.ucalgary.cpsc.ase.factextractor.visitor.Indexer;
import ca.ucalgary.cpsc.ase.factextractor.visitor.SourceModel;
import ca.ucalgary.cpsc.ase.factextractor.visitor.TestVisitor;
import ca.ucalgary.cpsc.ase.factextractor.writer.DatabaseWriter;

public class Application implements IApplication {
	
	private static Logger logger = Logger.getLogger(Application.class);

	@Override
	public Object start(IApplicationContext context) throws Exception {
		String[] arguments = (String[]) context.getArguments().get("application.args");
		if (arguments.length != 1) {
			System.out.print("Usage: FactExtractor <path>");
			return IApplication.EXIT_OK;
		}
		iterateFileSystem(arguments[0]);		
//		iterateWorkspace()
//		QueryGeneratorTest test = new QueryGeneratorTest();
//		test.testQueryTestFile();
		
		return IApplication.EXIT_OK;
	}
	
	private void iterateWorkspace() {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IWorkspaceRoot root = workspace.getRoot();
		// Get all projects in the workspace
		IProject[] projects = root.getProjects();
		
		SourceModel model = new SourceModel();
		
		// Loop over all projects
		for (IProject project : projects) {
			try {
				if (project.isNatureEnabled("org.eclipse.jdt.core.javanature")) {
					String projectName = project.getDescription().getName();
					String projectVersion = null;
					ProjectService projectService = new ProjectService();
					Project prj = projectService.create(projectName, projectVersion);
					model.stepIntoProject(prj);
					logger.debug("Project: " + projectName);
					
					IPackageFragment[] packages = JavaCore.create(project)
							.getPackageFragments();
					// parse(JavaCore.create(project));
					for (IPackageFragment mypackage : packages) {
						if (mypackage.getKind() == IPackageFragmentRoot.K_SOURCE) {
							for (ICompilationUnit unit : mypackage
									.getCompilationUnits()) {
								SourceFileService sourceService = new SourceFileService();
								String path = unit.getPath().toString();
								SourceFile source = sourceService.create(model.currentProject(), path);
								model.stepIntoSourceFile(source);
								logger.debug("File: " + path);
								
								// Now create the AST for the ICompilationUnits
								CompilationUnit parse = parse(unit);
								TestVisitor visitor = new TestVisitor(new DatabaseWriter(model));
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
	
	private void iterateFileSystem(String path) {
		ExecutorService pool = Executors.newFixedThreadPool(5);
		BoundedExecutor executor = new BoundedExecutor(pool, 100);
		
		RepositoryFileService repositoryService = new RepositoryFileService();
		List<RepositoryFile> unvisited;
		
		do {
			unvisited = repositoryService.findUnvisited();
			for (RepositoryFile file : unvisited) {
				try {
					executor.submit(new Indexer(path, file));
				} catch (RejectedExecutionException e) {
					logger.warn(e.getMessage());
				} catch (InterruptedException e) {
					logger.warn(e.getMessage());
				}
			}
			
		} while (unvisited.size() > 0);
		
		pool.shutdown();
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
