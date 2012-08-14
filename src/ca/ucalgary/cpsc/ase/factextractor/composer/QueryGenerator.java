package ca.ucalgary.cpsc.ase.factextractor.composer;

import java.io.File;

import org.eclipse.jdt.core.dom.CompilationUnit;

import ca.mcgill.cs.swevo.ppa.PPAOptions;
import ca.mcgill.cs.swevo.ppa.util.PPACoreSingleton;
import ca.mcgill.cs.swevo.ppa.util.PPACoreUtil;
import ca.ucalgary.cpsc.ase.QueryManager.Query;
import ca.ucalgary.cpsc.ase.factextractor.visitor.QueryModel;
import ca.ucalgary.cpsc.ase.factextractor.visitor.TestVisitor;
import ca.ucalgary.cpsc.ase.factextractor.writer.QueryWriter;

public class QueryGenerator {
	
	public QueryGenerator() {
		PPACoreSingleton.getInstance();
	}
	
	public Query generate(String code) {
		CompilationUnit cu = PPACoreUtil.getCU(code, new PPAOptions(), "Query Test");
		return generate(cu);
	}

	protected Query generate(CompilationUnit cu) {
		QueryWriter writer = new QueryWriter(new QueryModel());
		TestVisitor visitor = new TestVisitor(writer);
		cu.accept(visitor);
		return writer.getQuery();
	}
	
	public Query generate(File file) {
		CompilationUnit cu = PPACoreUtil.getCU(file, new PPAOptions(), "Query Test", true);
		return generate(cu);		
	}

}
