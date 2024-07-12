package br.cns24.experiments.som;

import java.awt.Color;
import java.awt.Graphics2D;

import br.cns24.experiments.ComplexNetwork;

public class PointSprite {
	private int x;

	private int y;

	private ComplexNetwork network;

	private Color color;

	private boolean showDetails;

	public static final int WIDTH = 10;
	
	private int idSymbol;

	public PointSprite(int x, int y, ComplexNetwork network, Color color, int idSymbol) {
		super();
		this.x = x;
		this.y = y;
		this.idSymbol = idSymbol;
		this.network = network;
		this.color = color;
	}

	public void draw(Graphics2D g) {
		g.setColor(color);
		switch (idSymbol) {
		case 0:
			//cruz
			g.drawLine(x, y - WIDTH / 2, x, y + WIDTH / 2);
			g.drawLine(x - WIDTH / 2, y, x + WIDTH / 2, y);
			break;
		case 1:
			//x
			g.drawLine(x - WIDTH / 2, y - WIDTH / 2, x + WIDTH / 2, y + WIDTH / 2);
			g.drawLine(x - WIDTH / 2, y + WIDTH / 2, x + WIDTH / 2, y - WIDTH / 2);
			break;
		case 2:
			g.fillRect(x - WIDTH / 2, y - WIDTH / 2, WIDTH, WIDTH);
			break;
		case 3:
			g.fillPolygon(new int[]{x - WIDTH / 2, x, x + WIDTH / 2}, 
					new int[]{y + WIDTH / 2, y - WIDTH / 2, y + WIDTH / 2}, 3);
			break;
		case 4:
			g.fillPolygon(new int[]{x - WIDTH / 2, x + WIDTH / 2, x}, 
					new int[]{y - WIDTH / 2, y - WIDTH / 2, y + WIDTH / 2}, 3);
			break;
		case 5:
			g.drawLine(x - WIDTH / 2, y - WIDTH / 2, x + WIDTH / 2, y + WIDTH / 2);
			g.drawLine(x - WIDTH / 2, y + WIDTH / 2, x + WIDTH / 2, y - WIDTH / 2);
			g.drawLine(x, y - WIDTH / 2, x, y + WIDTH / 2);
			g.drawLine(x - WIDTH / 2, y, x + WIDTH / 2, y);
			break;
		case 6:
			g.drawLine(x - WIDTH / 2, y - WIDTH / 2, x + WIDTH / 2, y + WIDTH / 2);
			g.drawLine(x - WIDTH / 2, y + WIDTH / 2, x + WIDTH / 2, y - WIDTH / 2);
			g.drawLine(x - WIDTH / 2, y, x + WIDTH / 2, y);
			break;
		case 7:
			g.fillOval(x - WIDTH / 2, y - WIDTH / 2, WIDTH, WIDTH);
			break;
		case 8:
			g.drawOval(x - WIDTH / 2, y - WIDTH / 2, WIDTH, WIDTH);
			break;
		default:
			g.fillOval(x - WIDTH / 2, y - WIDTH / 2, WIDTH, WIDTH);
			break;
		}
		
	}

	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getY() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
	}

	public ComplexNetwork getNetwork() {
		return network;
	}

	public void setNetwork(ComplexNetwork network) {
		this.network = network;
	}

	public boolean isShowDetails() {
		return showDetails;
	}

	public void setShowDetails(boolean showDetails) {
		this.showDetails = showDetails;
	}

	public Color getColor() {
		return color;
	}

	public void setColor(Color color) {
		this.color = color;
	}

	public int getIdSymbol() {
		return idSymbol;
	}

	public void setIdSymbol(int idSymbol) {
		this.idSymbol = idSymbol;
	}

}
