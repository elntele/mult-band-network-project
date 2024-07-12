package br.cns24.experiments;

import javax.swing.table.AbstractTableModel;

import br.cns24.TMetric;

public class ModelsConfigurationTableModel extends AbstractTableModel {
	private static final long serialVersionUID = 1L;

	private String[] columnNames = { "Variar", "Nome do modelo", "Parâmetro",
			"Valor inicial", "Valor final", "Passo" };

	private Object[][] data = new Object[TMetric.values().length][6];

	public ModelsConfigurationTableModel(Object[][] data) {
		this.data = data;
	}

	public int getColumnCount() {
		return columnNames.length;
	}

	public int getRowCount() {
		return data.length;
	}

	public String getColumnName(int col) {
		return columnNames[col];
	}

	public Object getValueAt(int row, int col) {
		return data[row][col];
	}

	public Class getColumnClass(int c) {
		return getValueAt(0, c).getClass();
	}

	public boolean isCellEditable(int row, int col) {
		if (col == 1 || col == 2) {
			return false;
		}
		return true;
	}

	public void setValueAt(Object value, int row, int col) {
		data[row][col] = value;
		fireTableCellUpdated(row, col);
	}
}
