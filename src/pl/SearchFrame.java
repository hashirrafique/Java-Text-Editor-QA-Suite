package pl;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.util.List;

class ButtonColumn extends JButton {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ButtonColumn(String text) {
        super(text);
    }
}

class SearchResultsTableModel extends DefaultTableModel {
	private static final long serialVersionUID = 1L;

	public SearchResultsTableModel() {
        super(new Object[]{"File Name", "Prefix", "Keyword", "Actions"}, 0);
    }

    @Override
    public boolean isCellEditable(int row, int column) {
        return column == 3;
    }
}

public class SearchFrame extends JFrame {
	private static final long serialVersionUID = 1L;
	private JTable resultsTable;
    private SearchResultsTableModel tableModel;

    public SearchFrame(List<String> searchResults) {
        setTitle("Search Results");
        setExtendedState(JFrame.MAXIMIZED_BOTH);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLocationRelativeTo(null);
		setLayout(new BorderLayout());

        tableModel = new SearchResultsTableModel();
        resultsTable = new JTable(tableModel) {
			private static final long serialVersionUID = 1L;

			@Override
            public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
                Component c = super.prepareRenderer(renderer, row, column);
                if (column == 3) {
                    JButton button = new ButtonColumn("Replace");
                    button.setPreferredSize(new Dimension(80, 30));
                    button.addActionListener(e -> {
                        System.out.println("Replace button clicked for row " + row);
                    });
                    return button;
                }
                return c;
            }
        };
        JScrollPane scrollPane = new JScrollPane(resultsTable);

        for (String result : searchResults) {
            String[] parts = result.split(" - ");
            if (parts.length == 2) {
                String fileName = parts[0];
                String prefixAndKeyword = parts[1];
                String[] prefixAndKeywordParts = prefixAndKeyword.split(" ");
                String prefix = prefixAndKeywordParts[0];
                String keyword = prefixAndKeywordParts[1];
                tableModel.addRow(new Object[]{fileName, prefix, keyword, "Replace"});
            }
        }

        JButton backButton = new JButton("Back to Menu");
        backButton.addActionListener(e -> {
            dispose();
        });

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(backButton);
        add(scrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        setVisible(true);
    }
}