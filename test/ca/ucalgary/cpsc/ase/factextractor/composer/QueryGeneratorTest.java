package ca.ucalgary.cpsc.ase.factextractor.composer;

import java.io.File;
import java.util.Map;

import org.apache.log4j.Logger;

import ca.ucalgary.cpsc.ase.FactManager.entity.TestMethod;
import ca.ucalgary.cpsc.ase.FactManager.service.TestMethodService;
import ca.ucalgary.cpsc.ase.QueryManager.Query;
import ca.ucalgary.cpsc.ase.QueryManager.VotingHeuristicManager;
import ca.ucalgary.cpsc.ase.factextractor.composer.QueryGenerator;

public class QueryGeneratorTest {
	
	private static Logger logger = Logger.getLogger(QueryGeneratorTest.class);

	public static void main(String[] args) throws Exception {
		if (args.length != 1) {
			System.out.println("Usage: QueryGeneratorTest <path>");
			System.exit(0);
		}
		QueryGeneratorTest test = new QueryGeneratorTest();
		test.testQueryTestFile(args[0]);
	}
	
	public void testQueryTestFile(String path) throws Exception {
		File file = new File(path);
						
		QueryGenerator generator = new QueryGenerator();
		Query query = generator.generate(file);

		VotingHeuristicManager manager = new VotingHeuristicManager();
		Map<Integer, Double> results = manager.match(query);
		
		print(results);
	}

	private void print(Map<Integer, Double> results) {
		TestMethodService service = new TestMethodService();
		for (Integer id : results.keySet()) {
			TestMethod tm = service.find(id);
			System.out.println(id + ", " + tm.getClazz().getFqn() + "." + tm.getName() + "(), " + results.get(id));
		}
	}
	
}
