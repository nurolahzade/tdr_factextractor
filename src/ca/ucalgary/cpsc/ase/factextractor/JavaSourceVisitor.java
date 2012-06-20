package ca.ucalgary.cpsc.ase.factextractor;

import java.io.File;

import org.eclipse.jdt.core.dom.CompilationUnit;

import ca.mcgill.cs.swevo.ppa.PPAOptions;
import ca.mcgill.cs.swevo.ppa.ui.PPAUtil;
import ca.ucalgary.cpsc.ase.FactManager.entity.SourceFile;
import ca.ucalgary.cpsc.ase.FactManager.service.SourceFileService;

public class JavaSourceVisitor extends FileSystemVisitor {

	@Override
	public void visit(File file) {
		SourceFileService sourceService = new SourceFileService();
		SourceFile source = sourceService.create(SourceModel.currentProject(), file.getAbsolutePath());
		SourceModel.stepIntoSourceFile(source);
		
		CompilationUnit cu = PPAUtil.getCU(file, new PPAOptions());
		TestVisitor visitor = new TestVisitor();
		cu.accept(visitor);
				
		super.visit(file);
	}	
	
}
