package ca.ucalgary.cpsc.ase.factextractor.dump;

import java.io.File;

import org.eclipse.jdt.core.dom.CompilationUnit;

import ca.mcgill.cs.swevo.ppa.PPAOptions;
import ca.mcgill.cs.swevo.ppa.ui.PPAUtil;
import ca.ucalgary.cpsc.ase.FactManager.entity.SourceFile;
import ca.ucalgary.cpsc.ase.FactManager.service.RepositoryFileService;
import ca.ucalgary.cpsc.ase.FactManager.service.SourceFileService;

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
