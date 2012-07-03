package ca.ucalgary.cpsc.ase.factextractor.dump;

import java.io.File;

import ca.ucalgary.cpsc.ase.FactManager.service.RepositoryFileService;

public class JavaSourceVisitor extends FileSystemVisitor {

	@Override
	public void visit(File file) {
		if (file.getName().endsWith(".java")) {
			RepositoryFileService service = new RepositoryFileService();
			service.create(file.getAbsolutePath());
		}
				
		super.visit(file);
	}	
	
}
