package br.cns24.experiments.som;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;

import javax.swing.JComponent;

public class ShapeColorSprite extends JComponent {
	private static final long serialVersionUID = 1L;
	
	private Color color;
	
	private static final int WIDTH = 20;
	public ShapeColorSprite(Color color){
		this.color = color;
		setPreferredSize(new Dimension(WIDTH, WIDTH));
	}

	@Override
	public void update(Graphics g) {
		paint(g);
	}

	@Override
	public void paint(Graphics g) {
		g.setColor(color);
		g.fillRect(0, 0, WIDTH, WIDTH);
	}
}
