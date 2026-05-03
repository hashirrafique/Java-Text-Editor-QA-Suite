package dal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.oujda_nlp_team.AlKhalil2Analyzer;
import net.oujda_nlp_team.entity.Result;

public class POSTagger {
    public static Map<String, List<String>> extractPOS(String text) {
    	final Logger logger = LogManager.getLogger(POSTagger.class);

        Map<String, List<String>> wordPosMap = new HashMap<>();

        String[] words = text.split("\\s+");

        try {
            AlKhalil2Analyzer analyzer = AlKhalil2Analyzer.getInstance();

            if (analyzer != null) {
                for (String word : words) {
                    List<String> posTags = new ArrayList<>();
                    List<Result> results = analyzer.processToken(word).getAllResults();

                    if (results != null && !results.isEmpty()) {
                        String[] splitWords = results.get(0).getPartOfSpeech().split("\\|");

                        for (String tag : splitWords) {
                            posTags.add(tag);
                        }
                    } else {
                        posTags.add("None"); 
                    }

                    wordPosMap.put(word, posTags);
                }
            } else {
                System.err.println("Failed to initialize AlKhalil2Analyzer.");
                logger.error("Failed to initialize AlKhalil2Analyzer.");
                
            }
        } catch (Exception e) {
            System.err.println("Error while extracting POS tags: " + e.getMessage());
            logger.error("Error while extracting POS tags: " + e.getMessage());
        }

        return wordPosMap;
    }
}