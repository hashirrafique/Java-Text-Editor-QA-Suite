package dal;

import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.oujda_nlp_team.AlKhalil2Analyzer;

public class RootExtraction {


    public static Map<String, String> extractRoots(String text) {
    	final Logger logger = LogManager.getLogger(RootExtraction.class);
        Map<String, String> wordRootMap = new HashMap<>();

        String[] words = text.split("\\s+");

        try {
            AlKhalil2Analyzer analyzer = AlKhalil2Analyzer.getInstance();

            if (analyzer != null) {
                for (String word : words) {
                    String root = analyzer.processToken(word).getAllRootString(); // Get the root

                    if (root != null && !root.isEmpty()) {
                        wordRootMap.put(word, PreProcessText.preprocessText(root)); // Add word-root pair
                    } else {
                        wordRootMap.put(word, "Not found"); // Add "Not found" if no root is found
                    }
                }
            } else {
                System.err.println("Failed to initialize AlKhalil2Analyzer.");
                logger.error("Failed to initialize AlKhalil2Analyzer.");
            }
        } catch (Exception e) {
            System.err.println("Error while extracting roots: " + e.getMessage());
            logger.error("Error while extracting roots: " + e.getMessage());
        }

        return wordRootMap;
    }
}