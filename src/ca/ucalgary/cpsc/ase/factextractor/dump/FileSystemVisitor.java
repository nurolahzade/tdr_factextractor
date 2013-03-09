package ca.ucalgary.cpsc.ase.factextractor.dump;

import java.io.File;

public class FileSystemVisitor {
	
	protected File base;
	
	public FileSystemVisitor(File path) {
		base = path;
	}
	
	public void start() {
		if (base.isDirectory()) {
			walk(base);			
		}
		else {
			visit(base);
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
		return true;
	}
}
