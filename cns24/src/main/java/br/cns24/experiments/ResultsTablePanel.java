package br.cns24.experiments;

import java.awt.Dimension;
import java.awt.GridLayout;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import br.cns24.TMetric;

public class ResultsTablePanel extends JPanel {
	private static final long serialVersionUID = 1L;

	public ResultsTablePanel(int width, ResultsTableModel model, JTable table) {
		super(new GridLayout(1, 0));

		table.setModel(model);
		table.setPreferredScrollableViewportSize(new Dimension(width, 70));
		table.setFillsViewportHeight(true);

		table.getColumnModel().getColumn(0).setPreferredWidth(30);
		table.getColumnModel().getColumn(1).setPreferredWidth(90);

		// Create the scroll pane and add the table to it.
		JScrollPane scrollPane = new JScrollPane(table);

		// Add the scroll pane to this panel.
		add(scrollPane);
	}
}
