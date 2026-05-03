package pl;

import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.UIManager;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import bll.IEditorBO;

public class FileImporter {
	private static final Logger logger = LogManager.getLogger(FileImporter.class);
    private IEditorBO businessObj;

    public FileImporter(IEditorBO businessObj) {
        this.businessObj = businessObj;
        
        try {
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void importFiles(ActionEvent e) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setMultiSelectionEnabled(true);
        int result = fileChooser.showOpenDialog(null);

        if (result == JFileChooser.APPROVE_OPTION) {
            File[] selectedFiles = fileChooser.getSelectedFiles();
            if (selectedFiles.length > 0) {
                for (File selectedFile : selectedFiles) {
                    String fileName = selectedFile.getName();
                    boolean isImport = businessObj.importTextFiles(selectedFile, fileName);
                    JOptionPane.showMessageDialog(null,
                            isImport ? fileName + " uploaded successfully!" : fileName + " failed to upload!");
                    logger.info(isImport ? fileName + " uploaded successfully!" : fileName + " failed to upload!");
                }
            }
        }
    }
}
