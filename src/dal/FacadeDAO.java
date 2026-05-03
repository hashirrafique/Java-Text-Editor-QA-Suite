package dal;

import java.util.List;
import java.util.Map;

import dto.Documents;

public class FacadeDAO implements IFacadeDAO {

	IEditorDBDAO mariaDB;

	public FacadeDAO(IEditorDBDAO mariaDB) {
		this.mariaDB = mariaDB;
	}

	@Override
	public boolean createFileInDB(String nameOfFile, String content) {
		return mariaDB.createFileInDB(nameOfFile, content);
	}

	@Override
	public boolean updateFileInDB(int id, String fileName, int pageNumber, String content) {
		return mariaDB.updateFileInDB(id, fileName, pageNumber, content);
	}

	@Override
	public boolean deleteFileInDB(int id) {
		return mariaDB.deleteFileInDB(id);
	}

	@Override
	public List<Documents> getFilesFromDB() {
		return mariaDB.getFilesFromDB();
	}

	@Override
	public String transliterateInDB(int pageId, String arabicText) {
		return mariaDB.transliterateInDB(pageId, arabicText);
	}


	@Override
	public Map<String, String> lemmatizeWords(String text) {
		return mariaDB.lemmatizeWords(text);
	}

	@Override
	public  Map<String, List<String>> extractPOS(String text) {
		return mariaDB.extractPOS(text);
	}

	@Override
	public Map<String, String> extractRoots(String text) {
		return mariaDB.extractRoots(text);
	}

	@Override
	public double performTFIDF(List<String> unSelectedDocsContent, String selectedDocContent) {
		return mariaDB.performTFIDF(unSelectedDocsContent, selectedDocContent);
	}

	@Override
	public Map<String, Double> performPMI(String content) {
		return mariaDB.performPMI(content);
	}

	@Override
	public Map<String, Double> performPKL(String content) {
		return mariaDB.performPKL(content);
	}

	@Override
	public Map<String, String> stemWords(String text) {
		return mariaDB.stemWords(text);
	}

	@Override
	public Map<String, String>segmentWords(String text) {
		return mariaDB.segmentWords(text);
	}



}
