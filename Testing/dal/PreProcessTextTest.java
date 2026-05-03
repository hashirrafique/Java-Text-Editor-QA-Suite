package dal;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test suite for PreProcessText.
 *
 * Covers:
 *  - Harakat (diacritics) removal
 *  - Non-Arabic character stripping
 *  - Full preprocessText pipeline
 */
@DisplayName("PreProcessText Tests")
class PreProcessTextTest {

    // ─────────────────────────────────────────────────────
    //  removeHarakat
    // ─────────────────────────────────────────────────────

    @Test
    @DisplayName("removeHarakat: diacritics are stripped from fully vowelled Arabic text")
    void testRemoveHarakat_VowelledText_DiacriticsRemoved() {
        String input    = "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ";
        String result   = PreProcessText.removeHarakat(input);

        // None of the diacritics characters should remain
        assertFalse(result.contains("ِ"),  "Kasra must be removed");
        assertFalse(result.contains("ْ"),  "Sukun must be removed");
        assertFalse(result.contains("َ"),  "Fatha must be removed");
        assertFalse(result.contains("ّ"),  "Shadda must be removed");
        assertFalse(result.contains("ً"),  "Tanwin Fath must be removed");
        assertTrue(result.contains("بسم"), "Base Arabic letters must be preserved");
    }

    @Test
    @DisplayName("removeHarakat: plain Arabic text without diacritics is unchanged")
    void testRemoveHarakat_PlainText_Unchanged() {
        String input  = "الكتاب المدرسي";
        String result = PreProcessText.removeHarakat(input);
        assertEquals(input, result, "Text without diacritics must pass through unchanged");
    }

    @Test
    @DisplayName("removeHarakat: empty string returns empty string")
    void testRemoveHarakat_EmptyString_ReturnsEmpty() {
        assertEquals("", PreProcessText.removeHarakat(""));
    }

    @Test
    @DisplayName("removeHarakat: non-Arabic characters are preserved")
    void testRemoveHarakat_NonArabicChars_Preserved() {
        String input  = "Hello 123 مرحبا";
        String result = PreProcessText.removeHarakat(input);
        assertTrue(result.contains("Hello"), "Latin characters must be preserved");
        assertTrue(result.contains("123"),   "Digits must be preserved");
    }

    // ─────────────────────────────────────────────────────
    //  removeNonArabicCharacters
    // ─────────────────────────────────────────────────────

    @Test
    @DisplayName("removeNonArabicCharacters: strips Latin, digits, and punctuation")
    void testRemoveNonArabic_MixedInput_OnlyArabicAndSpacesRemain() {
        String input  = "Hello مرحبا 123! @# world العالم";
        String result = PreProcessText.removeNonArabicCharacters(input);

        assertFalse(result.contains("Hello"), "Latin must be removed");
        assertFalse(result.contains("123"),   "Digits must be removed");
        assertFalse(result.contains("!"),     "Punctuation must be removed");
        assertTrue(result.contains("مرحبا"), "Arabic words must be preserved");
        assertTrue(result.contains("العالم"), "Arabic words must be preserved");
    }

    @Test
    @DisplayName("removeNonArabicCharacters: pure Arabic input is unchanged")
    void testRemoveNonArabic_PureArabic_Unchanged() {
        String input  = "بسم الله الرحمن الرحيم";
        String result = PreProcessText.removeNonArabicCharacters(input);
        assertEquals(input, result, "Pure Arabic text must not be altered");
    }

    @Test
    @DisplayName("removeNonArabicCharacters: all-Latin input becomes empty or whitespace")
    void testRemoveNonArabic_AllLatin_ReturnsBlank() {
        String result = PreProcessText.removeNonArabicCharacters("Hello World 12345!");
        assertTrue(result.trim().isEmpty(),
                "All-Latin input should become empty after stripping non-Arabic chars");
    }

    @Test
    @DisplayName("removeNonArabicCharacters: empty input returns empty string")
    void testRemoveNonArabic_EmptyInput_ReturnsEmpty() {
        assertEquals("", PreProcessText.removeNonArabicCharacters(""));
    }

    // ─────────────────────────────────────────────────────
    //  preprocessText (full pipeline)
    // ─────────────────────────────────────────────────────

    @Test
    @DisplayName("preprocessText: removes diacritics, strips non-Arabic, lowercases")
    void testPreprocessText_FullPipeline_CleanArabicText() {
        String input  = "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ Hello 123!";
        String result = PreProcessText.preprocessText(input);

        assertFalse(result.contains("ِ"),    "Diacritics must be removed");
        assertFalse(result.contains("Hello"), "Latin must be removed");
        assertFalse(result.contains("123"),   "Digits must be removed");
        assertTrue(result.contains("بسم"),   "Base Arabic letters must remain");
    }

    @Test
    @DisplayName("preprocessText: empty string returns empty string")
    void testPreprocessText_EmptyInput_ReturnsEmpty() {
        assertEquals("", PreProcessText.preprocessText("").trim());
    }

    @Test
    @DisplayName("preprocessText: special-characters-only input becomes empty")
    void testPreprocessText_SpecialCharsOnly_BecomesEmpty() {
        String result = PreProcessText.preprocessText("!@#$%^&*()");
        assertTrue(result.trim().isEmpty(),
                "Special-chars-only input must produce empty result after full preprocessing");
    }

    @ParameterizedTest(name = "preprocessText(''{0}'') must not contain ''{1}''")
    @CsvSource({
        "بِسْمِ اللَّهِ, ِ",
        "مَرحباً بالعالم, ً",
        "كِتَابٌ مُفِيدٌ, ٌ"
    })
    @DisplayName("preprocessText: parameterized — diacritics removed for various inputs")
    void testPreprocessText_Parameterized_DiacriticsRemoved(String input, String forbidden) {
        String result = PreProcessText.preprocessText(input);
        assertFalse(result.contains(forbidden),
                "Character '" + forbidden + "' must not appear in preprocessed output of '" + input + "'");
    }
}
