package ca.ucalgary.cpsc.ase.factextractor.dump;

import java.io.File;

public class FileSystemVisitor {

	public void walk(File root) {

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
	
	public void visit(File file) {
		// do nothing
	}
	
	public boolean visitDirectory(File file) {
		// do nothing
		return true;
	}
}