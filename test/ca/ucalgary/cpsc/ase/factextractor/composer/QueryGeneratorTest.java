package ca.ucalgary.cpsc.ase.factextractor.composer;

import java.io.File;
import java.util.Map;

import org.apache.log4j.Logger;

import ca.ucalgary.cpsc.ase.QueryManager.Query;
import ca.ucalgary.cpsc.ase.QueryManager.VotingHeuristicManager;
import ca.ucalgary.cpsc.ase.factextractor.composer.QueryGenerator;

public class QueryGeneratorTest {
	
	private static Logger logger = Logger.getLogger(QueryGeneratorTest.class);

	public static void main(String[] args) throws Exception {
		QueryGeneratorTest test = new QueryGeneratorTest();
		test.testQueryTestFile();
	}
	
	public void testQueryTestFile() throws Exception {
		File file = new File("/Users/mnurolahzade/Documents/workspace3.6.2/FactExtractor/test-res/2.java");
				
		
		QueryGenerator generator = new QueryGenerator();
		Query query = generator.generate(file);

		VotingHeuristicManager manager = new VotingHeuristicManager();
		Map<Integer, Double> results = manager.match(query);
		
		print(results);
	}

	private void print(Map<Integer, Double> results) {
		for (Integer id : results.keySet()) {
			System.out.println(id + " " + results.get(id));
		}
	}
	
}
