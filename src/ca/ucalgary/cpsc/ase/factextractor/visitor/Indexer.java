package ca.ucalgary.cpsc.ase.factextractor.visitor;

import java.io.File;

import org.eclipse.jdt.core.dom.CompilationUnit;

import ca.mcgill.cs.swevo.ppa.PPAOptions;
import ca.mcgill.cs.swevo.ppa.ui.PPAUtil;
import ca.ucalgary.cpsc.ase.FactManager.entity.RepositoryFile;
import ca.ucalgary.cpsc.ase.FactManager.entity.SourceFile;
import ca.ucalgary.cpsc.ase.FactManager.service.SourceFileService;
import ca.ucalgary.cpsc.ase.factextractor.writer.IndexWriter;

public class Indexer {
	
	protected String root;
	protected RepositoryFile file;	

	public Indexer(String root, RepositoryFile file) {
		this.root = root;
		this.file = file;
	}
	
	public void run() throws PPAException, VisitorException {
		SourceFileService sourceService = new SourceFileService();
		SourceModel model = new SourceModel();

		SourceFile source = sourceService.create(model.currentProject(), file.getPath());
		model.stepIntoSourceFile(source);
		CompilationUnit cu = null;
		try {
			cu = PPAUtil.getCU(new File(root + file.getPath()), new PPAOptions());			
		} catch (Throwable t) {
			throw new PPAException(t);
		}
		if (cu == null) {
			throw new PPAException("PPA did not produce a CompilationUnit.");
		}
		try {
			TestVisitor visitor = new TestVisitor(new IndexWriter(model));
			cu.accept(visitor);
		} catch (Throwable t) {
			throw new VisitorException(t);
		}
	}
	
}
