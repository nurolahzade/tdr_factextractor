package ca.ucalgary.cpsc.ase.factextractor.dump;

public class DumpFilter {

	public static void main(String[] args) {
		if (args.length != 1) {
			System.out.println("Usage: DumpIndexer <path>");
			System.exit(0);
		}
		
		new JavaSourceVisitor(args[0]);
	}	

}
