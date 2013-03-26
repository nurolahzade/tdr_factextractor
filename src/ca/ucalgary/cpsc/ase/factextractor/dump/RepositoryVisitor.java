package ca.ucalgary.cpsc.ase.factextractor.dump;

import java.io.File;

import ca.ucalgary.cpsc.ase.common.ServiceProxy;
import ca.ucalgary.cpsc.ase.common.service.RepositoryFileServiceRemote;

public class RepositoryVisitor extends FileSystemVisitor {

	private RepositoryFileServiceRemote service;
	
	public RepositoryVisitor(File path) throws Exception {
		super(path);
		service = ServiceProxy.getRepositoryFileService();
	}

	@Override
	protected void visit(File file) {
		if (file.getName().endsWith(".java")) {
			service.create(relativize(file));
		}
		super.visit(file);
	}
	
	protected String relativize(File file) {
		return base.toURI().relativize(file.toURI()).getPath();
	}
	
}
