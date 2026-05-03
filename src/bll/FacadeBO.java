package bll;

import java.io.File;
import java.util.List;
import java.util.Map;

import dto.Documents;

public class FacadeBO implements IFacadeBO {

	IEditorBO bo;

	public FacadeBO(IEditorBO bo) {
		this.bo = bo;
	}

	@Override
	public boolean createFile(String nameOfFile, String content) {
		return bo.createFile(nameOfFile, content);
	}

	@Override
	public boolean updateFile(int id, String fileName, int pageNumber, String content) {
		return bo.updateFile(id, fileName, pageNumber, content);
	}

	@Override
	public boolean deleteFile(int id) {
		return bo.deleteFile(id);
	}

	@Override
	public boolean importTextFiles(File file, String fileName) {
		return bo.importTextFiles(file, fileName);
	}

	@Override
	public Documents getFile(int id) {
		return bo.getFile(id);
	}

	@Override
	public List<Documents> getAllFiles() {
		return bo.getAllFiles();
	}

	@Override
	public String getFileExtension(String fileName) {
		return bo.getFileExtension(fileName);
	}

	@Override
	public String transliterate(int pageId, String arabicText) {
		return bo.transliterate(pageId, arabicText);
	}

	@Override
	public List<String> searchKeyword(String keyword) {
		return bo.searchKeyword(keyword);
	}

	@Override
	public Map<String, String> lemmatizeWords(String text) {
		return bo.lemmatizeWords(text);
	}

	@Override
	public Map<String, List<String>> extractPOS(String text) {
		return bo.extractPOS(text);
	}

	@Override
	public Map<String, String> extractRoots(String text) {
		return bo.extractRoots(text);
	}

	@Override
	public double performTFIDF(List<String> unSelectedDocsContent, String selectedDocContent) {
		return bo.performTFIDF(unSelectedDocsContent, selectedDocContent);
	}

	@Override
	public Map<String, Double> performPMI(String content) {
		return bo.performPMI(content);
	}

	@Override
	public Map<String, Double> performPKL(String content) {
		return bo.performPKL(content);
	}

	@Override
	public Map<String, String> stemWords(String text) {
		return bo.stemWords(text);
	}

	@Override
	public Map<String, String> segmentWords(String text) {
		return bo.segmentWords(text);
	}

}
