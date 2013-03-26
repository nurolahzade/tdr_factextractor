package ca.ucalgary.cpsc.ase.factextractor.indexer;

import java.io.File;

import org.eclipse.jdt.core.dom.CompilationUnit;

import ca.mcgill.cs.swevo.ppa.PPAOptions;
import ca.mcgill.cs.swevo.ppa.util.PPACoreUtil;
import ca.ucalgary.cpsc.ase.common.entity.SourceFile;
import ca.ucalgary.cpsc.ase.common.service.ServiceWrapperRemote;
import ca.ucalgary.cpsc.ase.factextractor.visitor.SourceModel;
import ca.ucalgary.cpsc.ase.factextractor.visitor.TestVisitor;
import ca.ucalgary.cpsc.ase.factextractor.visitor.VisitorException;
import ca.ucalgary.cpsc.ase.factextractor.writer.IndexWriter;

public class Indexer {
	
	protected String path;
	protected SourceFile source;

	public Indexer(SourceFile source, String path) {
		this.path = path;
		this.source = source;
	}
	
	public void run() throws PPAException, VisitorException {
		SourceModel model = new SourceModel();
		model.stepIntoSourceFile(source);
		CompilationUnit cu = null;
		try {
			cu = PPACoreUtil.getCU(new File(path), new PPAOptions(), Thread.currentThread().getName(), true);			
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
