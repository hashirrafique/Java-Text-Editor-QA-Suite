package dal;

import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.oujda_nlp_team.AlKhalil2Analyzer;

public class Lemmatization {

	public static Map<String, String> lemmatizeWords(String text) {
		final Logger logger = LogManager.getLogger(Lemmatization.class);
		Map<String, String> wordLemmaMap = new HashMap<>();

		String[] words = text.split("\\s+");

		try {
			AlKhalil2Analyzer analyzer = AlKhalil2Analyzer.getInstance();

			if (analyzer != null) {
				for (String word : words) {
					String lemma = analyzer.processToken(word).getAllLemmasString();

					if (lemma != null && !lemma.isEmpty()) {
						wordLemmaMap.put(word, PreProcessText.preprocessText(lemma));
					} else {
						wordLemmaMap.put(word, "Not found");
					}
				}
			} else {
				System.err.println("Failed to initialize AlKhalil2Analyzer.");
				logger.error("Failed to initialize AlKhalil2Analyzer.");
			}
		} catch (Exception e) {
			System.err.println("Error while lemmatizing words: " + e.getMessage());
			logger.error("Error while lemmatizing words: " + e.getMessage());
		}

		return wordLemmaMap;
	}
}