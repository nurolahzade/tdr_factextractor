package ca.ucalgary.cpsc.ase.factextractor;

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
import ca.ucalgary.cpsc.ase.FactManager.entity.SourceFile;
import ca.ucalgary.cpsc.ase.FactManager.service.ProjectService;
import ca.ucalgary.cpsc.ase.FactManager.service.SourceFileService;

public class Application implements IApplication {
	
	private static Logger logger = Logger.getLogger(Application.class);

	@Override
	public Object start(IApplicationContext context) throws Exception {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IWorkspaceRoot root = workspace.getRoot();
		// Get all projects in the workspace
		IProject[] projects = root.getProjects();
		// Loop over all projects
		for (IProject project : projects) {
			try {
				if (project.isNatureEnabled("org.eclipse.jdt.core.javanature")) {
					String projectName = project.getDescription().getName();
					String projectVersion = null;
					ProjectService projectService = new ProjectService();
					Project prj = projectService.create(projectName, projectVersion);
					SourceModel.getInstance().setProject(prj);
					logger.debug("Project: " + projectName);
					IPackageFragment[] packages = JavaCore.create(project)
							.getPackageFragments();
					// parse(JavaCore.create(project));
					for (IPackageFragment mypackage : packages) {
						if (mypackage.getKind() == IPackageFragmentRoot.K_SOURCE) {
							for (ICompilationUnit unit : mypackage
									.getCompilationUnits()) {
								// Now create the AST for the ICompilationUnits
								SourceFileService sourceService = new SourceFileService();
								String path = unit.getPath().toString();
								SourceFile source = sourceService.create(SourceModel.getInstance().getProject(), path);
								SourceModel.getInstance().setSourceFile(source);
								logger.debug("File: " + path);
								CompilationUnit parse = parse(unit);
								TestVisitor visitor = new TestVisitor();
								parse.accept(visitor);
								
							}
						}

					}
				}
			} catch (CoreException e) {
				logger.warn(e.getMessage());
			}
		}
		return IApplication.EXIT_OK;
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
