package br.cns24.experiments;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;

public class AboutDialog extends JDialog implements ActionListener {
	private static final long serialVersionUID = 1L;

	private Image image; 
	
	private int top;
	
	private int left;
	
	private static final int WIDTH = 610;
	
	private static final int HEIGHT = 490;
	
	public AboutDialog(JFrame parent, String title, String message, Image image) {
		super(parent, title, true);
		setLayout(null);
		left = 5;
		top = 30;
		
		this.image = image;
		setPreferredSize(new Dimension(WIDTH, HEIGHT));

		JButton button = new JButton("Fechar");
		button.addActionListener(this);
		button.setBounds((WIDTH - 80)/2, HEIGHT - 75, 80, 22);
		
		add(button);
		
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		center(parent);
		pack();
		setVisible(false);
	}
	
	@Override
	public void paint(Graphics g) {
		super.paint(g);
		g.drawImage(image, left, top, this);
	}

	public void center(JFrame parent) {
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		int x = (screenSize.width - WIDTH) / 2;
		int y = (screenSize.height - HEIGHT) / 2;
		setLocation(x, y);
	}

	public void actionPerformed(ActionEvent e) {
		setVisible(false);
		dispose();
	}

}