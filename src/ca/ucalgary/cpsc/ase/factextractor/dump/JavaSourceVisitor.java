package ca.ucalgary.cpsc.ase.factextractor.dump;

import java.io.File;

import ca.ucalgary.cpsc.ase.FactManager.service.RepositoryFileService;

public class JavaSourceVisitor extends FileSystemVisitor {

	public JavaSourceVisitor(String path) {
		super(path);
	}

	@Override
	protected void visit(File file) {
		if (file.getName().endsWith(".java")) {
			RepositoryFileService service = new RepositoryFileService();
			service.create(relativize(file));
		}
				
		super.visit(file);
	}
	
	protected String relativize(File file) {
		return base.toURI().relativize(file.toURI()).getPath();
	}
	
}
