package ca.ucalgary.cpsc.ase.factextractor;

import java.io.File;

public class FileSystemVisitor {

	public void walk(String path) {

        File root = new File(path);
        File[] files = root.listFiles();

        for (File f : files) {
            if (f.isDirectory()) {
            	if (visitDirectory(f)) {
                    walk(f.getAbsolutePath());            		
            	}
            }
            else {
                if (f.getName().endsWith(".java")) {
                	visit(f);
                }
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
