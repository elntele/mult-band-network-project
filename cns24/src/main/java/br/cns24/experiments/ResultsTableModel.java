package br.cns24.experiments;

import javax.swing.table.AbstractTableModel;

public class ResultsTableModel extends AbstractTableModel {
	private static final long serialVersionUID = 1L;

	private String[] columnNames;

	private Object[][] data;

	public ResultsTableModel(String[] columnNames, Object[][] data) {
		this.columnNames = columnNames;
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
		if (getValueAt(0, c) == null) {
			return String.class;
		}
		return getValueAt(0, c).getClass();
	}

	public boolean isCellEditable(int row, int col) {
		return false;
	}

	public void setValueAt(Object value, int row, int col) {
		data[row][col] = value;
		fireTableCellUpdated(row, col);
	}
}
