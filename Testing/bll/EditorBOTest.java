package bll;

import dal.IFacadeDAO;
import dto.Documents;
import dto.Pages;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Path;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test suite for EditorBO.
 *
 * Issue #1  — Auto-Save boundary conditions
 * Issue #5  — ImportCommand execute() / importTextFiles() behaviour
 *
 * Uses a manual stub of IFacadeDAO to avoid a live MariaDB connection.
 */
@DisplayName("EditorBO Tests")
class EditorBOTest {

    // ─────────────────────────────────────────────────────
    //  Manual stub — replaces Mockito to keep zero extra deps
    // ─────────────────────────────────────────────────────

    /**
     * Configurable stub that records calls and lets each test
     * choose what createFileInDB / updateFileInDB / deleteFileInDB return.
     */
    static class StubFacadeDAO implements IFacadeDAO {

        boolean createReturnValue  = true;
        boolean updateReturnValue  = true;
        boolean deleteReturnValue  = true;

        String  lastCreatedName    = null;
        String  lastCreatedContent = null;
        int     createCallCount    = 0;
        int     updateCallCount    = 0;
        int     deleteCallCount    = 0;

        List<Documents> stubDocuments = new ArrayList<>();

        @Override
        public boolean createFileInDB(String nameOfFile, String content) {
            createCallCount++;
            lastCreatedName    = nameOfFile;
            lastCreatedContent = content;
            return createReturnValue;
        }

        @Override
        public boolean updateFileInDB(int id, String fileName, int pageNumber, String content) {
            updateCallCount++;
            return updateReturnValue;
        }

        @Override
        public boolean deleteFileInDB(int id) {
            deleteCallCount++;
            return deleteReturnValue;
        }

        @Override public List<Documents> getFilesFromDB() { return stubDocuments; }
        @Override public String transliterateInDB(int pageId, String arabicText) { return arabicText; }
        @Override public Map<String, String> lemmatizeWords(String text) { return Collections.emptyMap(); }
        @Override public Map<String, List<String>> extractPOS(String text) { return Collections.emptyMap(); }
        @Override public Map<String, String> extractRoots(String text) { return Collections.emptyMap(); }
        @Override public double performTFIDF(List<String> unSelected, String selected) { return 0.5; }
        @Override public Map<String, Double> performPMI(String content) { return Collections.emptyMap(); }
        @Override public Map<String, Double> performPKL(String content) { return Collections.emptyMap(); }
        @Override public Map<String, String> stemWords(String text) { return Collections.emptyMap(); }
        @Override public Map<String, String> segmentWords(String text) { return Collections.emptyMap(); }
    }

    // ─────────────────────────────────────────────────────
    //  Test fixtures
    // ─────────────────────────────────────────────────────

    private StubFacadeDAO stub;
    private EditorBO       bo;

    @BeforeEach
    void setUp() {
        stub = new StubFacadeDAO();
        bo   = new EditorBO(stub);
    }

    // ─────────────────────────────────────────────────────
    //  Issue #1 — Auto-Save boundary tests
    // ─────────────────────────────────────────────────────

    @Test
    @DisplayName("[Issue #1] AutoSave: createFile delegates to DB and returns true on success")
    void testAutoSave_CreateFile_SuccessPath() {
        boolean result = bo.createFile("testDoc.txt", "محتوى المستند");

        assertTrue(result, "createFile must return true when DB succeeds");
        assertEquals(1, stub.createCallCount, "DB must be called exactly once");
        assertEquals("testDoc.txt",     stub.lastCreatedName);
        assertEquals("محتوى المستند", stub.lastCreatedContent);
    }

    @Test
    @DisplayName("[Issue #1] AutoSave: createFile returns false when DB fails")
    void testAutoSave_CreateFile_FailurePath() {
        stub.createReturnValue = false;

        boolean result = bo.createFile("failDoc.txt", "some content");

        assertFalse(result, "createFile must propagate the DB failure as false");
    }

    @Test
    @DisplayName("[Issue #1] AutoSave: updateFile (save) delegates to DB with correct params")
    void testAutoSave_UpdateFile_DelegatesToDB() {
        boolean result = bo.updateFile(42, "myFile.txt", 1, "updated content");

        assertTrue(result, "updateFile must return true when DB succeeds");
        assertEquals(1, stub.updateCallCount, "DB update must be called exactly once");
    }

    @Test
    @DisplayName("[Issue #1] AutoSave: updateFile returns false on DB failure")
    void testAutoSave_UpdateFile_FailurePath() {
        stub.updateReturnValue = false;

        assertFalse(bo.updateFile(1, "doc.txt", 1, "content"),
                "updateFile must propagate DB failure as false");
    }

    @Test
    @DisplayName("[Issue #1] AutoSave: empty file name is passed through without modification")
    void testAutoSave_EmptyFileName_PassedThrough() {
        // Edge case: auto-save with an empty name
        bo.createFile("", "some content");

        assertEquals("", stub.lastCreatedName,
                "Empty file name must be passed through to DB unchanged");
    }

    @Test
    @DisplayName("[Issue #1] AutoSave: empty content is passed through without modification")
    void testAutoSave_EmptyContent_PassedThrough() {
        bo.createFile("empty.txt", "");

        assertEquals("", stub.lastCreatedContent,
                "Empty content must be passed to DB unchanged — boundary condition");
    }

    @Test
    @DisplayName("[Issue #1] AutoSave: very large content is handled without exception")
    void testAutoSave_LargeContent_NoException() {
        String largeContent = "بسم الله الرحمن الرحيم ".repeat(5_000);

        assertDoesNotThrow(() -> bo.createFile("large.txt", largeContent),
                "createFile must not throw for very large content");
        assertEquals(1, stub.createCallCount);
    }

    // ─────────────────────────────────────────────────────
    //  Issue #5 — ImportCommand / importTextFiles tests
    // ─────────────────────────────────────────────────────

    @TempDir
    Path tempDir;

    @Test
    @DisplayName("[Issue #5] Import: valid .txt file imports successfully and content matches")
    void testImport_ValidTxtFile_ContentLoadedIntoEditor() throws Exception {
        String arabicContent = "بسم الله الرحمن الرحيم\nالحمد لله رب العالمين";
        File txtFile = tempDir.resolve("arabic.txt").toFile();
        try (FileWriter fw = new FileWriter(txtFile)) {
            fw.write(arabicContent);
        }

        boolean result = bo.importTextFiles(txtFile, "arabic.txt");

        assertTrue(result, "importTextFiles must return true for a valid .txt file");
        assertEquals(1, stub.createCallCount,
                "A successful import must delegate to createFileInDB exactly once");
        assertEquals("arabic.txt", stub.lastCreatedName,
                "Imported file name must be preserved");
    }

    @Test
    @DisplayName("[Issue #5] Import: file content is loaded into the editor (matches original)")
    void testImport_FileContentMatchesOriginal() throws Exception {
        String originalContent = "محتوى الملف الأصلي";
        File txtFile = tempDir.resolve("content.txt").toFile();
        try (FileWriter fw = new FileWriter(txtFile)) {
            fw.write(originalContent);
        }

        bo.importTextFiles(txtFile, "content.txt");

        assertTrue(stub.lastCreatedContent.contains(originalContent.trim()) ||
                   stub.lastCreatedContent.trim().equals(originalContent.trim()),
                "Content passed to DB must match the original file content");
    }

    @Test
    @DisplayName("[Issue #5] Import: valid .md5 extension file is accepted")
    void testImport_ValidMd5Extension_ImportSucceeds() throws Exception {
        File md5File = tempDir.resolve("checksum.md5").toFile();
        try (FileWriter fw = new FileWriter(md5File)) {
            fw.write("D41D8CD98F00B204E9800998ECF8427E");
        }

        boolean result = bo.importTextFiles(md5File, "checksum.md5");

        assertTrue(result, ".md5 extension must be an accepted import type");
    }

    @Test
    @DisplayName("[Issue #5] Import: unsupported extension (.pdf) returns false without crashing")
    void testImport_UnsupportedExtension_ReturnsFalse() throws Exception {
        File pdfFile = tempDir.resolve("document.pdf").toFile();
        try (FileWriter fw = new FileWriter(pdfFile)) {
            fw.write("fake pdf content");
        }

        boolean result = bo.importTextFiles(pdfFile, "document.pdf");

        assertFalse(result, ".pdf must be rejected — only .txt and .md5 are supported");
        assertEquals(0, stub.createCallCount,
                "DB must not be called for unsupported file types");
    }

    @Test
    @DisplayName("[Issue #5] Import: invalid/non-existent file path returns false without crashing")
    void testImport_InvalidPath_ReturnsFalseNoException() {
        File nonExistent = new File("/this/path/does/not/exist/file.txt");

        assertDoesNotThrow(() -> {
            boolean result = bo.importTextFiles(nonExistent, "file.txt");
            assertFalse(result, "Non-existent file must return false without throwing");
        });
    }

    @Test
    @DisplayName("[Issue #5] Import: empty file imports with empty content without crashing")
    void testImport_EmptyFile_ImportedWithEmptyContent() throws Exception {
        File emptyFile = tempDir.resolve("empty.txt").toFile();
        emptyFile.createNewFile(); // zero-byte file

        assertDoesNotThrow(() -> bo.importTextFiles(emptyFile, "empty.txt"),
                "Importing a zero-byte file must not throw an exception");
    }

    @Test
    @DisplayName("[Issue #5] Import: file name without extension returns false")
    void testImport_NoExtension_ReturnsFalse() throws Exception {
        File noExtFile = tempDir.resolve("noExtension").toFile();
        try (FileWriter fw = new FileWriter(noExtFile)) {
            fw.write("content");
        }

        boolean result = bo.importTextFiles(noExtFile, "noExtension");

        assertFalse(result, "File without recognised extension must be rejected");
    }

    // ─────────────────────────────────────────────────────
    //  deleteFile tests
    // ─────────────────────────────────────────────────────

    @Test
    @DisplayName("deleteFile: delegates to DB and returns true on success")
    void testDeleteFile_Success() {
        assertTrue(bo.deleteFile(5), "deleteFile must return true when DB succeeds");
        assertEquals(1, stub.deleteCallCount);
    }

    @Test
    @DisplayName("deleteFile: returns false when DB reports failure")
    void testDeleteFile_Failure() {
        stub.deleteReturnValue = false;
        assertFalse(bo.deleteFile(99), "deleteFile must propagate DB failure as false");
    }

    // ─────────────────────────────────────────────────────
    //  getFileExtension tests
    // ─────────────────────────────────────────────────────

    @Test
    @DisplayName("getFileExtension: returns correct extension for 'report.txt'")
    void testGetFileExtension_TxtFile() {
        assertEquals("txt", bo.getFileExtension("report.txt"));
    }

    @Test
    @DisplayName("getFileExtension: returns empty string for file with no extension")
    void testGetFileExtension_NoExtension() {
        assertEquals("", bo.getFileExtension("README"),
                "File with no dot must return empty string");
    }

    @Test
    @DisplayName("getFileExtension: handles dotfile names correctly")
    void testGetFileExtension_DotFile() {
        // '.gitignore' → extension is "gitignore"
        String ext = bo.getFileExtension(".gitignore");
        assertEquals("gitignore", ext);
    }

    // ─────────────────────────────────────────────────────
    //  getFile / getAllFiles tests
    // ─────────────────────────────────────────────────────

    @Test
    @DisplayName("getFile: returns correct document when ID matches")
    void testGetFile_MatchingId_ReturnsDocument() {
        Pages page = new Pages(1, 10, 1, "النص");
        Documents doc = new Documents(10, "found.txt", "h1", "2024-01-01", "2024-01-01",
                Collections.singletonList(page));
        stub.stubDocuments.add(doc);

        Documents result = bo.getFile(10);

        assertNotNull(result, "getFile must return the document with matching ID");
        assertEquals("found.txt", result.getName());
    }

    @Test
    @DisplayName("getFile: returns null when ID does not match any document")
    void testGetFile_NoMatch_ReturnsNull() {
        assertNull(bo.getFile(999),
                "getFile must return null when no document has the given ID");
    }
}
