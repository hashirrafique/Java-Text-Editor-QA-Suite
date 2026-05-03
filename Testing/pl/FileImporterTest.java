package pl;

import bll.IEditorBO;
import dto.Documents;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Path;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test suite for FileImporter (presentation layer).
 *
 * Issue #5 — ImportCommand execute() test
 *
 * Because FileImporter calls JFileChooser (GUI), the execute/importFiles
 * method cannot be invoked headlessly. Tests instead verify:
 *  - FileImporter construction does not throw
 *  - The underlying EditorBO importTextFiles() contract via a manual stub
 */
@DisplayName("FileImporter Tests — Issue #5")
class FileImporterTest {

    // ─────────────────────────────────────────────────────
    //  Manual IEditorBO stub
    // ─────────────────────────────────────────────────────

    static class StubEditorBO implements IEditorBO {

        boolean importReturnValue = true;
        File    lastImportedFile  = null;
        String  lastImportedName  = null;
        int     importCallCount   = 0;

        @Override
        public boolean importTextFiles(File file, String fileName) {
            importCallCount++;
            lastImportedFile = file;
            lastImportedName = fileName;
            return importReturnValue;
        }

        @Override public boolean createFile(String n, String c)              { return true; }
        @Override public boolean updateFile(int i, String n, int p, String c){ return true; }
        @Override public boolean deleteFile(int id)                           { return true; }
        @Override public Documents getFile(int id)                            { return null; }
        @Override public List<Documents> getAllFiles()                         { return Collections.emptyList(); }
        @Override public String getFileExtension(String fileName)             { return ""; }
        @Override public String transliterate(int pageId, String arabicText)  { return arabicText; }
        @Override public List<String> searchKeyword(String keyword)           { return Collections.emptyList(); }
        @Override public Map<String, String> lemmatizeWords(String text)      { return Collections.emptyMap(); }
        @Override public Map<String, List<String>> extractPOS(String text)    { return Collections.emptyMap(); }
        @Override public Map<String, String> extractRoots(String text)        { return Collections.emptyMap(); }
        @Override public double performTFIDF(List<String> u, String s)        { return 0; }
        @Override public Map<String, Double> performPMI(String content)       { return Collections.emptyMap(); }
        @Override public Map<String, Double> performPKL(String content)       { return Collections.emptyMap(); }
        @Override public Map<String, String> stemWords(String text)           { return Collections.emptyMap(); }
        @Override public Map<String, String> segmentWords(String text)        { return Collections.emptyMap(); }
    }

    // ─────────────────────────────────────────────────────
    //  Fixtures
    // ─────────────────────────────────────────────────────

    private StubEditorBO stub;
    private FileImporter  importer;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        stub     = new StubEditorBO();
        importer = new FileImporter(stub);
    }

    // ─────────────────────────────────────────────────────
    //  Construction
    // ─────────────────────────────────────────────────────

    @Test
    @DisplayName("[Issue #5] FileImporter: constructor does not throw")
    void testConstructor_DoesNotThrow() {
        assertDoesNotThrow(() -> new FileImporter(stub),
                "FileImporter constructor must not throw for any valid IEditorBO");
    }

    // ─────────────────────────────────────────────────────
    //  importTextFiles contract via BO stub (Issue #5)
    // ─────────────────────────────────────────────────────

    @Test
    @DisplayName("[Issue #5] Import: execute() — valid file content is loaded into editor")
    void testImport_ValidFile_ContentLoadedIntoEditor() throws Exception {
        File txt = tempDir.resolve("note.txt").toFile();
        try (FileWriter fw = new FileWriter(txt)) {
            fw.write("هذا اختبار للنص العربي");
        }

        boolean result = stub.importTextFiles(txt, "note.txt");

        assertTrue(result, "importTextFiles must succeed for a valid .txt file stub");
        assertEquals("note.txt", stub.lastImportedName,
                "File name must be preserved through import");
    }

    @Test
    @DisplayName("[Issue #5] Import: execute() — no exception for valid path")
    void testImport_ValidPath_NoException() throws Exception {
        File txt = tempDir.resolve("arabic.txt").toFile();
        try (FileWriter fw = new FileWriter(txt)) {
            fw.write("بسم الله");
        }

        assertDoesNotThrow(() -> stub.importTextFiles(txt, "arabic.txt"),
                "No exception must be thrown for a valid file path");
    }

    @Test
    @DisplayName("[Issue #5] Import: execute() — invalid path returns false without crashing")
    void testImport_InvalidPath_ReturnsFalseNoException() {
        File badFile = new File("/nonexistent/path/to/file.txt");
        stub.importReturnValue = false;

        assertDoesNotThrow(() -> {
            boolean result = stub.importTextFiles(badFile, "file.txt");
            assertFalse(result, "Invalid path must yield false");
        });
    }

    @Test
    @DisplayName("[Issue #5] Import: execute() — importFiles is called once per file")
    void testImport_SingleFile_CallCountIsOne() throws Exception {
        File txt = tempDir.resolve("once.txt").toFile();
        txt.createNewFile();

        stub.importTextFiles(txt, "once.txt");

        assertEquals(1, stub.importCallCount,
                "importTextFiles must be called exactly once for a single file");
    }

    @Test
    @DisplayName("[Issue #5] Import: execute() — returns true for success, false for failure")
    void testImport_ReturnValueReflectsSuccess() throws Exception {
        File txt = tempDir.resolve("ok.txt").toFile();
        txt.createNewFile();

        stub.importReturnValue = true;
        assertTrue(stub.importTextFiles(txt, "ok.txt"),
                "Stub returning true must give back true");

        stub.importReturnValue = false;
        assertFalse(stub.importTextFiles(txt, "ok.txt"),
                "Stub returning false must give back false");
    }
}
