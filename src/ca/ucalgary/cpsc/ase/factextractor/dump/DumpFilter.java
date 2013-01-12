package ca.ucalgary.cpsc.ase.factextractor.dump;

import java.io.File;

public class DumpFilter {

	public static void main(String[] args) {
		if (args.length != 1) {
			System.out.println("Usage: DumpIndexer <path>");
			System.exit(0);
		}
		
		new RepositoryVisitor(new File(args[0]));
	}	

}
