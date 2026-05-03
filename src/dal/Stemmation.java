package dal;

import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.oujda_nlp_team.AlKhalil2Analyzer;

public class Stemmation {

    public static Map<String, String> stemWords(String text) {
    	final Logger logger = LogManager.getLogger(Stemmation.class);

        Map<String, String> wordStemMap = new HashMap<>();

        String[] words = text.split("\\s+");

        try {
            AlKhalil2Analyzer analyzer = AlKhalil2Analyzer.getInstance();

            if (analyzer != null) {
                for (String word : words) {
                    String stem = analyzer.processToken(word).getAllStemString(); // Get the stem

                    if (stem != null && !stem.isEmpty()) {
                        wordStemMap.put(word, PreProcessText.preprocessText(stem)); // Add word-stem pair
                    } else {
                        wordStemMap.put(word, "Not found"); // Add "Not found" if no stem is found
                    }
                }
            } else {
                System.err.println("Failed to initialize AlKhalil2Analyzer.");
                logger.error("Failed to initialize AlKhalil2Analyzer.");
            }
        } catch (Exception e) {
            System.err.println("Error while stemming words: " + e.getMessage());
            logger.error("Error while stemming words: " + e.getMessage());
        }

        return wordStemMap;
    }
}