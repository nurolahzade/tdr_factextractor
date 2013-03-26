package ca.ucalgary.cpsc.ase.factextractor.evaluation;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Map;

import org.apache.log4j.Logger;

import ca.ucalgary.cpsc.ase.common.ServiceProxy;
import ca.ucalgary.cpsc.ase.common.heuristic.HeuristicManager;
import ca.ucalgary.cpsc.ase.common.heuristic.VotingResult;
import ca.ucalgary.cpsc.ase.common.query.Query;
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
			HeuristicManager manager = ServiceProxy.getHeuristicManager();
			Map<Integer, VotingResult> results = manager.match(query);
			print(writer, results);
		} catch (Exception e) {
			e.printStackTrace(writer);
			logger.error(e);
		}

		writer.close();
	}

	private void print(PrintWriter writer, Map<Integer, VotingResult> results) {
		for (Integer id : results.keySet()) {
			VotingResult result = results.get(id);
			writer.println("id=" + id + " rank=" + result.getRank() + 
					" score=" + String.format("%.3f", result.getScore()) + 
					" fqn=" + result.getFqn() + generateHeuristics(result));
		}
	}
	
	private String generateHeuristics(VotingResult result) {
		StringBuilder builder = new StringBuilder();
		builder.append(" heuristics={");
		int total = result.getHeuristics().size();
		int counter = 0;
		for (String heuristic : result.getHeuristics()) {
			builder.append(heuristic);
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
