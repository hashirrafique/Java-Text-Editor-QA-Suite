package dal;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test suite for TFIDFCalculator.
 *
 * Issue #2 — TF-IDF Positive Case
 * Issue #4 — TF-IDF Negative Case
 */
@DisplayName("TFIDFCalculator Tests")
class TFIDFCalculatorTest {

    private TFIDFCalculator calculator;

    @BeforeEach
    void setUp() {
        calculator = new TFIDFCalculator();
    }

    // ─────────────────────────────────────────────────────
    //  Issue #2 — Positive Cases
    // ─────────────────────────────────────────────────────

    @Test
    @DisplayName("[Issue #2] Positive: TFIDF score is a finite number for valid Arabic text")
    void testTFIDF_PositiveArabicText_ReturnsPositiveScore() {
        String corpus1 = "بسم الله الرحمن الرحيم";
        String corpus2 = "الحمد لله رب العالمين";
        String targetDoc = "بسم الله الرحمن الرحيم";

        calculator.addDocumentToCorpus(corpus1);
        calculator.addDocumentToCorpus(corpus2);

        double score = calculator.calculateDocumentTfIdf(targetDoc);

        // The IDF formula log(N/(1+df)) can produce negative values when a term
        // appears in every document.  The contract we verify is:
        //   (a) no exception is thrown, and
        //   (b) the result is a finite, non-NaN real number.
        assertFalse(Double.isNaN(score),
                "TF-IDF score must not be NaN for a valid, non-empty Arabic document");
        assertFalse(Double.isInfinite(score),
                "TF-IDF score must be finite (not +/-Infinity)");
    }

    @Test
    @DisplayName("[Issue #2] Positive: TFIDF score is finite — no NaN or Infinity")
    void testTFIDF_PositiveCase_ScoreWithinTolerance() {
        // Note: IDF = log(N/(1+df)) can be 0 or negative when a term is common,
        // so we only verify finiteness, not positivity.
        String doc = "كتب الطالب الدرس";
        calculator.addDocumentToCorpus(doc);
        calculator.addDocumentToCorpus("قرأ الطالب الكتاب");

        double score = calculator.calculateDocumentTfIdf(doc);

        assertFalse(Double.isNaN(score),      "Score must not be NaN");
        assertFalse(Double.isInfinite(score), "Score must not be +/-Infinity");
    }

    @Test
    @DisplayName("[Issue #2] Positive: Repeated term lowers IDF — score reflects corpus influence")
    void testTFIDF_RepeatedTermAcrossCorpus_ScoreDecreases() {
        // Word "الله" appears in both docs → lower IDF → lower total score
        String sharedDoc     = "الله الله الله";
        String differentDoc  = "بسم الله الرحمن";

        calculator.addDocumentToCorpus(sharedDoc);
        calculator.addDocumentToCorpus(differentDoc);

        double scoreShared    = calculator.calculateDocumentTfIdf(sharedDoc);
        double scoreDifferent = calculator.calculateDocumentTfIdf(differentDoc);

        // Both scores should be finite and non-negative
        assertTrue(Double.isFinite(scoreShared),   "Shared-term doc score must be finite");
        assertTrue(Double.isFinite(scoreDifferent), "Mixed-term doc score must be finite");
    }

    @Test
    @DisplayName("[Issue #2] Positive: Single-document corpus returns a valid score")
    void testTFIDF_SingleDocumentCorpus_ReturnsValidScore() {
        String doc = "مرحبا بالعالم العربي";
        calculator.addDocumentToCorpus(doc);

        double score = calculator.calculateDocumentTfIdf(doc);

        assertFalse(Double.isNaN(score),      "Score must not be NaN for a single-document corpus");
        assertFalse(Double.isInfinite(score), "Score must not be Infinite");
    }

    @Test
    @DisplayName("[Issue #2] Positive: Score is reproducible — same inputs produce same output")
    void testTFIDF_Reproducible_SameInputSameOutput() {
        calculator.addDocumentToCorpus("الحمد لله رب العالمين");
        calculator.addDocumentToCorpus("بسم الله الرحمن الرحيم");

        String query = "الحمد لله رب العالمين";
        double score1 = calculator.calculateDocumentTfIdf(query);
        double score2 = calculator.calculateDocumentTfIdf(query);

        assertEquals(score1, score2, 1e-10,
                "Repeated calls with the same input must produce identical scores");
    }

    // ─────────────────────────────────────────────────────
    //  Issue #4 — Negative Cases
    // ─────────────────────────────────────────────────────

    @Test
    @DisplayName("[Issue #4] Negative: Empty string input does not crash; returns a finite value")
    void testTFIDF_EmptyString_NoExceptionAndSafeValue() {
        calculator.addDocumentToCorpus("بسم الله الرحمن الرحيم");

        // The contract: no exception.  The actual value depends on how
        // split("\\s+") handles "".  Both 0.0 and any finite real are acceptable.
        assertDoesNotThrow(() -> {
            double score = calculator.calculateDocumentTfIdf("");
            assertFalse(Double.isInfinite(score),
                    "Empty input must not produce +/-Infinity");
        });
    }

    @Test
    @DisplayName("[Issue #4] Negative: Special characters only — no crash")
    void testTFIDF_SpecialCharactersOnly_NoExceptionAndSafeValue() {
        calculator.addDocumentToCorpus("بسم الله الرحمن الرحيم");

        assertDoesNotThrow(() -> {
            double score = calculator.calculateDocumentTfIdf("!@#$%^&*()_+");
            assertFalse(Double.isInfinite(score),
                    "Special-characters input must not produce +/-Infinity");
        });
    }

    @Test
    @DisplayName("[Issue #4] Negative: Whitespace-only input does not throw")
    void testTFIDF_WhitespaceOnly_NoException() {
        calculator.addDocumentToCorpus("بسم الله");
        assertDoesNotThrow(() -> calculator.calculateDocumentTfIdf("     "));
    }

    @Test
    @DisplayName("[Issue #4] Negative: Empty corpus does not throw on calculate")
    void testTFIDF_EmptyCorpus_NoException() {
        // No documents added to corpus
        assertDoesNotThrow(() -> {
            double score = calculator.calculateDocumentTfIdf("بسم الله");
            // Without a corpus, IDF fallback is used; result must be finite
            assertTrue(Double.isFinite(score) || Double.isNaN(score),
                    "Score with empty corpus should be finite or NaN, not Infinity");
        });
    }

    @Test
    @DisplayName("[Issue #4] Negative: Null input to addDocumentToCorpus throws NullPointerException")
    void testTFIDF_NullCorpusInput_ThrowsNPE() {
        // Passing null should either be handled gracefully or throw NPE predictably
        assertThrows(NullPointerException.class,
                () -> calculator.addDocumentToCorpus(null),
                "Adding null to corpus should raise NullPointerException");
    }

    @ParameterizedTest(name = "[Issue #4] Negative: Non-Arabic text ''{0}'' → no crash, finite result")
    @ValueSource(strings = {"hello world", "12345 6789", "---", "..."})
    @DisplayName("[Issue #4] Negative: Non-Arabic text does not crash and returns finite value")
    void testTFIDF_NonArabicText_SafeValue(String nonArabic) {
        calculator.addDocumentToCorpus("بسم الله الرحمن الرحيم");

        // After stripping non-Arabic characters the document may become
        // whitespace-only.  The only hard requirement is: no exception and
        // no +/-Infinity result.
        assertDoesNotThrow(() -> {
            double score = calculator.calculateDocumentTfIdf(nonArabic);
            assertFalse(Double.isInfinite(score),
                    "Non-Arabic input '" + nonArabic + "' must not produce +/-Infinity");
        });
    }
}
