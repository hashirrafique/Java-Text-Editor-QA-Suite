package dal;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test suite for HashCalculator.
 *
 * Issue #6 — Hash Integrity (MD5)
 *
 * Verifies:
 *  - Output matches expected MD5 value for known inputs
 *  - Results are consistent (idempotent)
 *  - No crash occurs for any input
 */
@DisplayName("HashCalculator Tests — Issue #6")
class HashCalculatorTest {

    // ─────────────────────────────────────────────────────
    //  Issue #6 — Known-value verification
    // ─────────────────────────────────────────────────────

    @Test
    @DisplayName("[Issue #6] MD5 of empty string matches RFC-1321 reference value")
    void testHash_EmptyString_MatchesRFCReference() throws Exception {
        // MD5("") = D41D8CD98F00B204E9800998ECF8427E  (RFC-1321)
        String expected = "D41D8CD98F00B204E9800998ECF8427E";
        String actual   = HashCalculator.calculateHash("");

        assertEquals(expected, actual,
                "MD5 of empty string must match the RFC-1321 reference value");
    }

    @Test
    @DisplayName("[Issue #6] MD5 of 'hello' matches known reference value")
    void testHash_HelloString_MatchesKnownValue() throws Exception {
        // MD5("hello") = 5D41402ABC4B2A76B9719D911017C592
        String expected = "5D41402ABC4B2A76B9719D911017C592";
        String actual   = HashCalculator.calculateHash("hello");

        assertEquals(expected, actual,
                "MD5 of 'hello' must equal known reference '5D41402ABC4B2A76B9719D911017C592'");
    }

    @Test
    @DisplayName("[Issue #6] MD5 of 'The quick brown fox' matches known reference value")
    void testHash_QuickBrownFox_MatchesKnownValue() throws Exception {
        // MD5("The quick brown fox jumps over the lazy dog")
        //   = 9E107D9D372BB6826BD81D3542A419D6
        String input    = "The quick brown fox jumps over the lazy dog";
        String expected = "9E107D9D372BB6826BD81D3542A419D6";
        String actual   = HashCalculator.calculateHash(input);

        assertEquals(expected, actual,
                "MD5 of the quick-brown-fox sentence must match its well-known reference value");
    }

    @Test
    @DisplayName("[Issue #6] MD5 of Arabic text does not crash and returns 32-char hex string")
    void testHash_ArabicText_ReturnValidHex() throws Exception {
        String arabicInput = "بسم الله الرحمن الرحيم";

        assertDoesNotThrow(() -> {
            String hash = HashCalculator.calculateHash(arabicInput);
            assertNotNull(hash, "Hash of Arabic text must not be null");
            assertEquals(32, hash.length(),
                    "MD5 hash must always be exactly 32 hex characters");
            assertTrue(hash.matches("[0-9A-F]+"),
                    "MD5 hash must contain only uppercase hex digits");
        });
    }

    // ─────────────────────────────────────────────────────
    //  Issue #6 — Consistency (idempotence)
    // ─────────────────────────────────────────────────────

    @Test
    @DisplayName("[Issue #6] Consistency: same input always produces same MD5 output")
    void testHash_SameInput_AlwaysProducesSameOutput() throws Exception {
        String input  = "Java Text Editor QA Suite — consistency check";
        String hash1  = HashCalculator.calculateHash(input);
        String hash2  = HashCalculator.calculateHash(input);
        String hash3  = HashCalculator.calculateHash(input);

        assertEquals(hash1, hash2, "First and second hash calls must be identical");
        assertEquals(hash2, hash3, "Second and third hash calls must be identical");
    }

    @Test
    @DisplayName("[Issue #6] Consistency: different inputs produce different hashes")
    void testHash_DifferentInputs_ProduceDifferentHashes() throws Exception {
        String hash1 = HashCalculator.calculateHash("document_version_1");
        String hash2 = HashCalculator.calculateHash("document_version_2");

        assertNotEquals(hash1, hash2,
                "Different inputs must not collide in a standard MD5 computation");
    }

    // ─────────────────────────────────────────────────────
    //  Issue #6 — Output format validation
    // ─────────────────────────────────────────────────────

    @Test
    @DisplayName("[Issue #6] Output is always exactly 32 uppercase hex characters")
    void testHash_OutputFormat_32UppercaseHexChars() throws Exception {
        String[] inputs = {"a", "ab", "abc", "abcd", "12345", "!@#$%", "مرحبا"};

        for (String input : inputs) {
            String hash = HashCalculator.calculateHash(input);
            assertEquals(32, hash.length(),
                    "Hash of '" + input + "' must be 32 chars, was: " + hash.length());
            assertTrue(hash.matches("[0-9A-F]+"),
                    "Hash of '" + input + "' must be uppercase hex, was: " + hash);
        }
    }

    @ParameterizedTest(name = "MD5(''{0}'') == ''{1}''")
    @CsvSource({
        "'',         D41D8CD98F00B204E9800998ECF8427E",
        "hello,      5D41402ABC4B2A76B9719D911017C592",
        "world,      7D793037A0760186574B0282F2F435E7",
        "123456,     E10ADC3949BA59ABBE56E057F20F883E"
    })
    @DisplayName("[Issue #6] Parameterized: MD5 correctness for multiple known inputs")
    void testHash_KnownMappings(String input, String expectedHash) throws Exception {
        String trimmedInput    = input.trim();
        String trimmedExpected = expectedHash.trim();

        assertEquals(trimmedExpected, HashCalculator.calculateHash(trimmedInput),
                "MD5('" + trimmedInput + "') must equal '" + trimmedExpected + "'");
    }

    // ─────────────────────────────────────────────────────
    //  Issue #6 — Edge cases / No crash
    // ─────────────────────────────────────────────────────

    @Test
    @DisplayName("[Issue #6] Large input string does not crash and returns valid hash")
    void testHash_LargeInput_NoException() {
        String largeInput = "أ".repeat(100_000); // 100k Arabic characters

        assertDoesNotThrow(() -> {
            String hash = HashCalculator.calculateHash(largeInput);
            assertNotNull(hash);
            assertEquals(32, hash.length());
        });
    }

    @Test
    @DisplayName("[Issue #6] Special characters input does not crash")
    void testHash_SpecialCharacters_NoException() {
        assertDoesNotThrow(() -> {
            String hash = HashCalculator.calculateHash("!@#$%^&*()_+-=[]{}|;':\",./<>?");
            assertNotNull(hash);
            assertEquals(32, hash.length());
        });
    }

    @Test
    @DisplayName("[Issue #6] Null input throws exception predictably")
    void testHash_NullInput_ThrowsException() {
        // calculateHash must throw a predictable exception for null — not silently NPE
        assertThrows(Exception.class,
                () -> HashCalculator.calculateHash(null),
                "Null input must throw an exception");
    }
}
