package dal;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.oujda_nlp_team.AlKhalil2Analyzer;
import net.oujda_nlp_team.entity.Result;

public class WordSegmentation {

	public static Map<String, String> extractSegments(String text) {

		Map<String, String> wordSegmentMap = new LinkedHashMap<>();
		final Logger logger = LogManager.getLogger(WordSegmentation.class);

		String[] words = text.split("\\s+");

		try {

			AlKhalil2Analyzer analyzer = AlKhalil2Analyzer.getInstance();

			if (analyzer != null) {
				for (String word : words) {

					List<Result> results = analyzer.processToken(word).getAllResults();

					if (results != null && !results.isEmpty()) {

						Result firstResult = results.get(0);
						String stem = firstResult.getStem();

						String prefix = getPrefix(word);
						String suffix = getSuffix(word);

						StringBuilder segmentBuilder = new StringBuilder();
						if (!prefix.isEmpty()) {
							segmentBuilder.append(prefix).append("-");
						}
						segmentBuilder.append(stem);
						if (!suffix.isEmpty()) {
							segmentBuilder.append("-").append(suffix);
						}

						wordSegmentMap.put(word, segmentBuilder.toString());
					} else {

						wordSegmentMap.put(word, "None");
					}
				}
			} else {
				System.err.println("Failed to initialize AlKhalil2Analyzer.");
				logger.error("Failed to initialize AlKhalil2Analyzer.");
			}
		} catch (Exception e) {
			System.err.println("Error while extracting word segments: " + e.getMessage());
			logger.error("Error while extracting word segments: " + e.getMessage());
		}

		return wordSegmentMap;
	}

	private static String getPrefix(String word) {
		String[] commonPrefixes = { "ال", "ب", "ت", "ك", "م", "و", "ف", "س" };
		for (String prefix : commonPrefixes) {
			if (word.startsWith(prefix)) {
				return prefix;
			}
		}
		return "";
	}

	private static String getSuffix(String word) {
		String[] commonSuffixes = { "ة", "ون", "ين", "ات", "ي", "ه" };

		for (String suffix : commonSuffixes) {
			if (word.endsWith(suffix)) {
				return suffix;
			}
		}
		return "";
	}

}
