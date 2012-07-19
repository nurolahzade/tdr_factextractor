package ca.ucalgary.cpsc.ase.factextractor.visitor;

import java.io.File;

import org.eclipse.jdt.core.dom.CompilationUnit;

import ca.mcgill.cs.swevo.ppa.PPAOptions;
import ca.mcgill.cs.swevo.ppa.ui.PPAUtil;
import ca.ucalgary.cpsc.ase.FactManager.entity.RepositoryFile;
import ca.ucalgary.cpsc.ase.FactManager.entity.SourceFile;
import ca.ucalgary.cpsc.ase.FactManager.service.RepositoryFileService;
import ca.ucalgary.cpsc.ase.FactManager.service.SourceFileService;
import ca.ucalgary.cpsc.ase.factextractor.writer.DatabaseWriter;

public class Indexer {
	
	protected String root;
	protected RepositoryFile file;	

	public Indexer(String root, RepositoryFile file) {
		this.root = root;
		this.file = file;
	}
	
	public void run() {
		SourceFileService sourceService = new SourceFileService();
		SourceModel model = new SourceModel();

		SourceFile source = sourceService.create(model.currentProject(), file.getPath());
		model.stepIntoSourceFile(source);
		
		CompilationUnit cu = PPAUtil.getCU(new File(root + file.getPath()), new PPAOptions());
		TestVisitor visitor = new TestVisitor(new DatabaseWriter(model));
		cu.accept(visitor);
	}
	
}
