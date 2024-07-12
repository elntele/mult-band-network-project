package br.cns24.experiments;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.text.NumberFormat;

import javax.swing.JComponent;

import br.cns24.experiments.setup.ApplicationSetup;
import br.cns24.experiments.setup.MetricExperimentSetup;
import br.cns24.util.FormatUtils;

public class GraphCanvas extends JComponent {
	private static final long serialVersionUID = 1L;

	private ComplexNetwork network;

	private int[][] screenPositionMatrix;

	private int width;

	private int height;

	private double xScale;

	private double yScale;

	private int xIni;

	private int borders;

	private Image upeIcon;

	private Image ufpeIcon;

	private ApplicationSetup setup;

	private static final int DEFAULT_BORDERS = 30;

	private static final int INFO_WIDTH = 0;
	
	private static final int yAdj = -20;

	private static final NumberFormat nf = NumberFormat.getInstance();

	private static final Font fontResearchGroupTitle = new Font("Arial Narrow, Arial", Font.BOLD, 10);

	private static final Font fontTitle = new Font("Arial", Font.BOLD, 12);

	private static final Font font = new Font("Arial", Font.PLAIN, 12);

	private static final NumberFormat formmater1FractionDigit = NumberFormat.getInstance();

	static {
		nf.setMaximumFractionDigits(2);
		nf.setMinimumFractionDigits(2);
		formmater1FractionDigit.setMaximumFractionDigits(1);
		formmater1FractionDigit.setMinimumFractionDigits(1);
	}

	public GraphCanvas(ComplexNetwork network, int width, int height, Image upeIcon, Image ufpeIcon,
			ApplicationSetup setup) {
		super();
		this.setup = setup;
		this.upeIcon = upeIcon;
		this.ufpeIcon = ufpeIcon;
		this.network = network;
		this.width = width;
		this.height = height;
		this.borders = DEFAULT_BORDERS;
		this.xIni = borders;
		createScreenPositions(network, width, height);
		repaint();
	}

	private void createScreenPositions(ComplexNetwork network, int width, int height) {
		double maiorX = 0;
		double maiorY = 0;
		double menorX = Double.MAX_VALUE;
		double menorY = Double.MAX_VALUE;
		this.width = width;
		this.height = height;
		int n = network.getAdjacencyMatrix().length;
		
		for (int i = 0; i < n; i++) {
			if (network.getNodePositions()[i][0] > maiorX) {
				maiorX = network.getNodePositions()[i][0];
			}
			if (network.getNodePositions()[i][1] > maiorY) {
				maiorY = network.getNodePositions()[i][1];
			}
			if (network.getNodePositions()[i][0] < menorX) {
				menorX = network.getNodePositions()[i][0];
			}
			if (network.getNodePositions()[i][1] < menorY) {
				menorY = network.getNodePositions()[i][1];
			}
		}
		xScale = (width - INFO_WIDTH - 2 * borders) / (maiorX - menorX);
		yScale = (height - 2 * borders - 20) / (maiorY - menorY);
		screenPositionMatrix = new int[n][2];
		for (int i = 0; i < screenPositionMatrix.length; i++) {
			screenPositionMatrix[i][0] = (int) (Math.round((network.getNodePositions()[i][0] - menorX) * xScale));
			screenPositionMatrix[i][1] = (int) (Math.round((network.getNodePositions()[i][1] - menorY) * yScale));
		}
		repaint();
	}

	@Override
	public void update(Graphics g) {
		paint(g);
	}

	@Override
	public void paint(Graphics g) {
		draw(g);
	}

	public void draw(Graphics g) {
		g.setColor(Color.white);
		g.fillRect(0, 0, width, height);
		drawTopology(g);
		drawTitle(g);
		// drawWaterMark(g);
	}

	private void drawTitle(Graphics g) {
		g.setFont(fontTitle);
		String title = "REDE COMPLEXA MODELO " + network.getModel().toString().toUpperCase();
		g.drawString(title, (width - getFontMetrics(fontTitle).stringWidth(title)) / 2, borders);
	}

	private void drawTopology(Graphics g) {
		g.setFont(font);
		((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
//		g.setColor(Color.lightGray);
		g.setColor(Color.black);
		for (int i = 0; i < network.getAdjacencyMatrix().length; i++) {
			for (int j = i + 1; j < network.getAdjacencyMatrix()[i].length; j++) {
				if (network.getAdjacencyMatrix()[i][j] == 1) {
					g.drawLine(xIni + screenPositionMatrix[i][0], height - screenPositionMatrix[i][1] + yAdj, xIni
							+ screenPositionMatrix[j][0], height - screenPositionMatrix[j][1] + yAdj);
				}
			}
		}
		
		int r = 5;
		for (int i = 0; i < screenPositionMatrix.length; i++) {
			r = (int) (Math.round((network.getDegreeMatrix()[i][i] * 1.0) / network.getDegreeMatrix().length * 50)) + 5;
			g.setColor(Color.black);
//			if (screenPositionMatrix.length < 100) {
//				g.drawString((i + 1) + "", xIni + screenPositionMatrix[i][0] - r / 2, height
//						- +screenPositionMatrix[i][1] - r / 2);
//			}
			
			if (network.getDegreeMatrix()[i][i] == 0){
				g.setColor(Color.red);
			} 
			g.fillOval(xIni + screenPositionMatrix[i][0] - r / 2, height - +screenPositionMatrix[i][1] - r / 2 + yAdj, r, r);
		}
		g.setColor(Color.black);
	}

	private void drawMetricValues(Graphics g) {
		int i = 0;
		int lineHeight = 12;
		for (MetricExperimentSetup metricSetup : setup.getMetrics()) {
			double value = network.getMetricValues().get(metricSetup.getMetric());
			g.drawString(metricSetup.getMetric().toString() + " = " + FormatUtils.getInstance().getValue(value), width
					- INFO_WIDTH, (int) (i * 1.5 * lineHeight + borders));
			i++;
		}
	}

	private void drawWaterMark(Graphics g) {
		g.drawImage(ufpeIcon, width - borders - 190, height - 121 - borders, this);
		g.drawImage(upeIcon, width - borders - 100, height - 121 - borders, this);

		g.setFont(fontResearchGroupTitle);

		g.drawString("GRUPO DE PESQUISA EM REDES COMPLEXAS", width - INFO_WIDTH - 10, height - 15 - borders);

		g.drawString("PARCERIA UFPE-UPE", width - INFO_WIDTH + 50, height - borders);
	}

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public int[][] getScreenPositionMatrix() {
		return screenPositionMatrix;
	}

	public void setScreenPositionMatrix(int[][] screenPositionMatrix) {
		this.screenPositionMatrix = screenPositionMatrix;
	}

	public double getScale() {
		return xScale;
	}

	public void setScale(double scale) {
		this.xScale = scale;
	}

	public ComplexNetwork getNetwork() {
		return network;
	}

	public void setNetwork(ComplexNetwork network) {
		this.network = network;
		createScreenPositions(network, width, height);
	}

	public ApplicationSetup getSetup() {
		return setup;
	}

	public void setSetup(ApplicationSetup setup) {
		this.setup = setup;
		repaint();
	}

}
