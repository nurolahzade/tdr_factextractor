package ca.ucalgary.cpsc.ase.factextractor.composer;

import java.io.File;

import org.eclipse.jdt.core.dom.CompilationUnit;

import ca.mcgill.cs.swevo.ppa.PPAOptions;
import ca.mcgill.cs.swevo.ppa.ui.PPAUtil;
import ca.ucalgary.cpsc.ase.QueryManager.Query;
import ca.ucalgary.cpsc.ase.factextractor.visitor.QueryModel;
import ca.ucalgary.cpsc.ase.factextractor.visitor.TestVisitor;
import ca.ucalgary.cpsc.ase.factextractor.writer.QueryWriter;

public class QueryGenerator {
	
	public Query generate(String code) {
		CompilationUnit cu = PPAUtil.getCU(code, new PPAOptions());
		return generate(cu);
	}

	protected Query generate(CompilationUnit cu) {
		QueryWriter writer = new QueryWriter(new QueryModel());
		TestVisitor visitor = new TestVisitor(writer);
		cu.accept(visitor);
		return writer.getQuery();
	}
	
	public Query generate(File file) {
		CompilationUnit cu = PPAUtil.getCU(file, new PPAOptions());
		return generate(cu);		
	}

}
