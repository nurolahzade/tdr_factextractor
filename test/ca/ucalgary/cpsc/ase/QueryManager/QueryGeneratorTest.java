package ca.ucalgary.cpsc.ase.QueryManager;

import java.io.File;
import java.util.Map;

import ca.ucalgary.cpsc.ase.factextractor.composer.QueryGenerator;

public class QueryGeneratorTest {
	
	public static void main(String[] args) throws Exception {
		QueryGeneratorTest test = new QueryGeneratorTest();
		test.testQueryTestFile();
	}
	
	public void testQueryTestFile() throws Exception {
		File file = new File("/test-res/Test1.java");

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
