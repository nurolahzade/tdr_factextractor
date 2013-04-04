package ca.ucalgary.cpsc.ase.factextractor.evaluation;

import java.io.File;
import java.io.FileWriter;
import java.util.Iterator;
import java.util.Map;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import ca.ucalgary.cpsc.ase.common.ServiceProxy;
import ca.ucalgary.cpsc.ase.common.heuristic.HeuristicManager;
import ca.ucalgary.cpsc.ase.common.heuristic.VotingResult;
import ca.ucalgary.cpsc.ase.common.query.Query;
import ca.ucalgary.cpsc.ase.factextractor.composer.QueryGenerator;

public class ExperimentRunner {

	private static Logger logger = Logger.getLogger(ExperimentRunner.class);

	private static final String EVALUATION_OUTPUT = "output";
	private static final String TASK = "task";
	private static final String TASK_NAME = "name";
	private static final String TASK_TARGET = "target";
	private static final String RUN = "run";
	private static final String VARIATION = "variation";
	private static final String VARIATION_TYPE = "type";
	private static final String VARIATION_RANK = "rank";
	private static final String VARIATION_SCORE = "score";
	private static final String HEURISTIC = "heuristic";
	private static final String HEURISTIC_NAME = "name";
	private static final String HEURISTIC_SCORE = "score";
	private static final String NOT_AVAILABLE = "N/A";
	
	private Document inXml;
	private Document outXml;
	private HeuristicManager manager;
	
	public ExperimentRunner(File config) throws Exception {
		// parse input XML
        SAXReader reader = new SAXReader();
        inXml = reader.read(config);
        
        // read root node
        Element evaluation = inXml.getRootElement();        
        
        // create output XML
		outXml = DocumentHelper.createDocument();
		Element run = outXml.addElement(RUN);
		
		QueryGenerator generator = new QueryGenerator();
		manager = ServiceProxy.getHeuristicManager();
		
		// process input task nodes
		Iterator i = evaluation.elementIterator();
		while (i.hasNext()) {
		    Element task = (Element) i.next();
		    String name = task.attributeValue(TASK_NAME);
		    String target = task.attributeValue(TASK_TARGET);
		    
		    // create output task node
		    Element t = run.addElement(TASK)
		 		.addAttribute(TASK_NAME, name)
		 		.addAttribute(TASK_TARGET, target);
		    
		    // process input task variation nodes
		    Iterator iterator = task.elementIterator();
		    while (iterator.hasNext()) {
		    	Element variation = (Element) iterator.next();
		    	String type = variation.attributeValue(VARIATION_TYPE);
		    	String file = variation.getTextTrim();
		    	
		    	// parse variation file
		    	File input = new File(config.getParentFile(), file);
		    	Query query;
		    	try {
		    		query = generator.generate(input);				
		    	} catch (Exception e) {
		    		logger.error("Error processing " + input.getPath(), e);
		    		return;
		    	}
				
				// fetch
				Map<Integer, VotingResult> results = manager.match(query);
		    	
				// create output variation node
		    	Element v = t.addElement(VARIATION).addAttribute(VARIATION_TYPE, type);
				boolean fetched = false;
				for (Integer id : results.keySet()) {
					if (target.equals(results.get(id).getFqn())) {
						VotingResult vr = results.get(id);
						matchedTarget(v, vr);
						fetched = true;
						break;
					}
				}
				if (!fetched) {
					unmatchedTarget(v);
				}
		    }
		}
		
		// write output XML
        String path = evaluation.attributeValue(EVALUATION_OUTPUT);
        File output = new File(config.getParentFile(), path);
		FileWriter writer = new FileWriter(output);
		outXml.write(writer);
		writer.close();
	}

	private void unmatchedTarget(Element variation) {
		variation.addAttribute(VARIATION_RANK, NOT_AVAILABLE);
		variation.addAttribute(VARIATION_SCORE, NOT_AVAILABLE);
	}

	private void matchedTarget(Element variation, VotingResult vr) {
		variation.addAttribute(VARIATION_RANK, vr.getRank().toString());
		variation.addAttribute(VARIATION_SCORE, String.format("%.3f", vr.getScore()));
		for (String h : vr.getHeuristics()) {
			Element heuristic = variation.addElement(HEURISTIC);
			heuristic.addAttribute(HEURISTIC_NAME, h);
			heuristic.addAttribute(HEURISTIC_SCORE, String.format("%.3f", vr.getScore(h)));
		}
	}

	public static void main(String[] args) throws Exception {
		if (args.length != 1) {
			System.out.println("Usage: ExperimentRunner <config>");
			System.exit(0);
		}
		
		new ExperimentRunner(new File(args[0]));
	}
	
	
	
}
