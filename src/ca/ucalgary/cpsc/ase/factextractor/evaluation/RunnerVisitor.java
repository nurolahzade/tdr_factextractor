package ca.ucalgary.cpsc.ase.factextractor.evaluation;

import java.io.File;

import ca.ucalgary.cpsc.ase.factextractor.composer.QueryGeneratorTest;
import ca.ucalgary.cpsc.ase.factextractor.dump.FileSystemVisitor;

public class RunnerVisitor extends FileSystemVisitor {

	public RunnerVisitor(File path) {
		super(path);
	}

	@Override
	protected void visit(File file) {
		if (file.getName().endsWith(".java")) {
			new QueryGeneratorTest().runQueryTestFile(file);
		}
		super.visit(file);
	}

}
