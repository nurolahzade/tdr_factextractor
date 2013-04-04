package ca.ucalgary.cpsc.ase.factextractor.dump;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import javax.naming.NamingException;

import ca.ucalgary.cpsc.ase.common.ServiceProxy;
import ca.ucalgary.cpsc.ase.common.service.RepositoryFileServiceRemote;

public class RepositoryUtil {

	private RepositoryFileServiceRemote repositoryService;
	
	public RepositoryUtil() throws NamingException {
		repositoryService = ServiceProxy.getRepositoryFileService();
	}
	
	public static void main(String[] args) throws Exception {
		if (args.length != 1) {
			System.out.println("Usage: RepositoryUtil <path>");
			System.exit(0);			
		}
		RepositoryUtil util = new RepositoryUtil();
		util.process(args[0]);
	}

	private void process(String path) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(path));
		String line;
		while ((line = br.readLine()) != null) {
			if (!line.trim().isEmpty())
				repositoryService.create(line);
		}
		br.close();		
	}
}
