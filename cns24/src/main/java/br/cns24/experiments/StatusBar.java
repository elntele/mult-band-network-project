package br.cns24.experiments;

import java.awt.Dimension;

import javax.swing.JLabel;

public class StatusBar extends JLabel {
	private static final long serialVersionUID = 1L;

	public StatusBar() {
		super();
		super.setPreferredSize(new Dimension(100, 16));
		setMessage("Selecione uma das op��es no menu!");
	}

	public void setMessage(String message) {
		setText(" " + message);
	}
}