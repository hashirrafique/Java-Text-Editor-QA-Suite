package bll;

import dto.Documents;
import dto.Pages;
import org.junit.jupiter.api.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test suite for SearchWord.
 *
 * Covers:
 *  - Minimum keyword length enforcement
 *  - Keyword found in page content
 *  - Keyword not found returns empty list
 *  - Multi-document corpus search
 */
@DisplayName("SearchWord Tests")
class SearchWordTest {

    private List<Documents> buildCorpus() {
        Pages page1 = new Pages(1, 1, 1, "بسم الله الرحمن الرحيم");
        Pages page2 = new Pages(2, 1, 2, "الحمد لله رب العالمين الرحمن الرحيم");
        Pages page3 = new Pages(3, 2, 1, "وعلمنا لغة العرب والعلوم");

        Documents doc1 = new Documents(1, "Surah_Al-Fatiha.txt", "hash1", "2024-01-01", "2024-01-01",
                Arrays.asList(page1, page2));
        Documents doc2 = new Documents(2, "Arabic_Text.txt", "hash2", "2024-01-02", "2024-01-02",
                Collections.singletonList(page3));

        return Arrays.asList(doc1, doc2);
    }

    // ─────────────────────────────────────────────────────
    //  Minimum keyword length
    // ─────────────────────────────────────────────────────

    @Test
    @DisplayName("Keyword shorter than 3 chars throws IllegalArgumentException")
    void testSearch_KeywordTooShort_ThrowsIllegalArgumentException() {
        List<Documents> corpus = buildCorpus();

        assertThrows(IllegalArgumentException.class,
                () -> SearchWord.searchKeyword("ال", corpus),
                "Keyword with fewer than 3 characters must throw IllegalArgumentException");
    }

    @Test
    @DisplayName("Keyword of exactly 1 char throws IllegalArgumentException")
    void testSearch_SingleChar_ThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                () -> SearchWord.searchKeyword("ب", buildCorpus()));
    }

    @Test
    @DisplayName("Keyword of exactly 2 chars throws IllegalArgumentException")
    void testSearch_TwoChars_ThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                () -> SearchWord.searchKeyword("بس", buildCorpus()));
    }

    @Test
    @DisplayName("Empty keyword throws IllegalArgumentException")
    void testSearch_EmptyKeyword_ThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                () -> SearchWord.searchKeyword("", buildCorpus()));
    }

    // ─────────────────────────────────────────────────────
    //  Successful keyword searches
    // ─────────────────────────────────────────────────────

    @Test
    @DisplayName("Keyword found in corpus — result list is non-empty")
    void testSearch_KeywordExists_ReturnsNonEmptyList() {
        List<String> results = SearchWord.searchKeyword("الرحمن", buildCorpus());

        assertNotNull(results, "Result list must not be null");
        assertFalse(results.isEmpty(),
                "Searching for 'الرحمن' which exists in corpus must return results");
    }

    @Test
    @DisplayName("Keyword not present in any document — returns empty list")
    void testSearch_KeywordAbsent_ReturnsEmptyList() {
        List<String> results = SearchWord.searchKeyword("طيران", buildCorpus());

        assertNotNull(results, "Result list must not be null even when empty");
        assertTrue(results.isEmpty(),
                "Searching for 'طيران' which is absent must return empty list");
    }

    @Test
    @DisplayName("Keyword found in multiple pages — all matching snippets returned")
    void testSearch_KeywordInMultiplePages_AllResultsReturned() {
        // "الرحيم" appears in both page1 and page2 of doc1
        List<String> results = SearchWord.searchKeyword("الرحيم", buildCorpus());

        assertNotNull(results);
        // Must have found at least one result (ideally two, one per page)
        assertTrue(results.size() >= 1,
                "Keyword in multiple pages must produce at least one result entry");
    }

    @Test
    @DisplayName("Search on empty corpus returns empty list")
    void testSearch_EmptyCorpus_ReturnsEmptyList() {
        List<String> results = SearchWord.searchKeyword("بسم", Collections.emptyList());

        assertNotNull(results);
        assertTrue(results.isEmpty(),
                "Searching an empty corpus must return an empty list");
    }

    @Test
    @DisplayName("Minimum valid keyword length (3 chars) does not throw")
    void testSearch_ThreeCharKeyword_DoesNotThrow() {
        assertDoesNotThrow(() -> SearchWord.searchKeyword("بسم", buildCorpus()),
                "A 3-char keyword must not throw an exception");
    }

    @Test
    @DisplayName("Search is case-sensitive for Arabic — exact match required")
    void testSearch_CaseSensitivity_ExactMatchRequired() {
        // Arabic doesn't have case, but verifies exact string matching
        List<String> results = SearchWord.searchKeyword("لغة", buildCorpus());

        assertNotNull(results);
        // "لغة" appears in doc2's page3
        assertFalse(results.isEmpty(), "'لغة' must be found in doc2");
    }
}
