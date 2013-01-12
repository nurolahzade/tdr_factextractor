package ca.ucalgary.cpsc.ase.factextractor.dump;

import java.io.File;

public class FileSystemVisitor {
	
	protected File base;
	
	public FileSystemVisitor(File path) {
		base = path;
		if (path.isDirectory()) {
			walk(path);			
		}
		else {
			visit(path);
		}
	}

	protected void walk(File root) {

        File[] files = root.listFiles();

        for (File f : files) {
            if (f.isDirectory()) {
            	if (visitDirectory(f)) {
                    walk(f);            		
            	}
            }
            else {
                visit(f);
            }
        }
    }
	
	protected void visit(File file) {
		// do nothing
	}
	
	protected boolean visitDirectory(File file) {
		// do nothing
		return true;
	}
}
