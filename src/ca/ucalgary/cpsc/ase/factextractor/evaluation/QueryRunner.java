package ca.ucalgary.cpsc.ase.factextractor.evaluation;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Map;

import org.apache.log4j.Logger;

import ca.ucalgary.cpsc.ase.FactManager.entity.Clazz;
import ca.ucalgary.cpsc.ase.FactManager.service.ClazzService;
import ca.ucalgary.cpsc.ase.QueryManager.Heuristic;
import ca.ucalgary.cpsc.ase.QueryManager.Query;
import ca.ucalgary.cpsc.ase.QueryManager.VotingHeuristicManager;
import ca.ucalgary.cpsc.ase.QueryManager.VotingResult;
import ca.ucalgary.cpsc.ase.factextractor.composer.QueryGenerator;

public class QueryRunner {
	
	private static Logger logger = Logger.getLogger(QueryRunner.class);

	public static void main(String[] args) {
		if (args.length != 1) {
			System.out.println("Usage: QueryGeneratorTest <path>");
			System.exit(0);
		}
		QueryRunner instance = new QueryRunner();
		instance.runQueryTestFile(new File(args[0]));
	}
	
	public void runQueryTestFile(File input) {
		QueryGenerator generator = new QueryGenerator();
		Query query = generator.generate(input);
		
		File output = new File(input.getParent(), input.getName().replaceFirst("\\.java$", ".txt"));
		PrintWriter writer;
		try {
			writer = new PrintWriter(output);
		} catch (FileNotFoundException e) {
			logger.error(e);
			return;
		} 
		
		writer.println(query);
		
		try {
			VotingHeuristicManager manager = new VotingHeuristicManager();
			Map<Integer, VotingResult> results = manager.match(query);
			print(writer, results);
		} catch (Exception e) {
			e.printStackTrace(writer);
			logger.error(e);
		}

		writer.close();
	}

	private void print(PrintWriter writer, Map<Integer, VotingResult> results) {
		ClazzService service = new ClazzService();
		for (Integer id : results.keySet()) {
			VotingResult result = results.get(id);
			Clazz c = service.find(id);
			writer.println("id=" + id + " rank=" + result.getRank() + 
					" score=" + String.format("%.3f", result.getScore()) + 
					" fqn=" + c.getFqn() + generateHeuristics(result));
		}
	}
	
	private String generateHeuristics(VotingResult result) {
		StringBuilder builder = new StringBuilder();
		builder.append(" heuristics={");
		int total = result.getHeuristics().size();
		int counter = 0;
		for (Heuristic heuristic : result.getHeuristics()) {
			builder.append(heuristic.getName());
			builder.append(":");
			builder.append(String.format("%.3f", result.getScore(heuristic)));
			if (++counter < total) {
				builder.append(", ");				
			}
		}
		builder.append("}");
		return builder.toString();
	}
	
}
