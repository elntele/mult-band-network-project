package br.cns24.experiments;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.IntervalXYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.text.NumberFormat;

import javax.swing.JComponent;



public class XYGraphCanvas extends JComponent {
	private String title = "Distribui��o de graus dos nós";

	private static final long serialVersionUID = 1L;

	private int[][] screenPositionMatrix;

	private int width;

	private int height;

	private double[] values;

	private JFreeChart chart;

	private ChartPanel panel;

	private static final NumberFormat nf = NumberFormat.getInstance();

	private static final NumberFormat formmater1FractionDigit = NumberFormat.getInstance();
	
	private String eixoX = "\u03C9";
	
	private String eixoY = "DFT(\u03C9)";

	static {
		nf.setMaximumFractionDigits(2);
		nf.setMinimumFractionDigits(2);
		formmater1FractionDigit.setMaximumFractionDigits(1);
		formmater1FractionDigit.setMinimumFractionDigits(1);
	}

	public XYGraphCanvas(String title, double[] values, int width, int height, String eixoX, String eixoY) {
		super();
		this.title = title;
		this.values = values;
		this.width = width;
		this.height = height;
		chart = createChart(eixoX, eixoY);
		panel = new ChartPanel(chart);
		panel.setPreferredSize(new Dimension(width - 5, height - 5));
		setLayout(new BorderLayout());
		add(panel, BorderLayout.CENTER);
		this.eixoX = eixoX;
		this.eixoY = eixoY;
	}


	public XYGraphCanvas(String title, double[] values, int width, int height) {
		this(title, values, width, height, "\u03C9", "DFT(\u03C9)");
	}
	
	private JFreeChart createChart(String eixoX, String eixoY) {
		XYSeries series = new XYSeries("");
		int i = 1;
		for (double d : values) {
			series.add(i, d);
			i++;
		}
		IntervalXYDataset dataset = new XYSeriesCollection(series);
		JFreeChart chart = ChartFactory.createXYBarChart(null, null, false, null, dataset, PlotOrientation.VERTICAL,
				false, true, false);
		chart.setTextAntiAlias(true);
//		chart.setTitle(new TextTitle(title,
//			    new java.awt.Font("Arial", Font.BOLD, 6)));
		chart.getXYPlot().setBackgroundPaint(Color.white);
		ValueAxis axis = chart.getXYPlot().getDomainAxis();
//		axis.setRange(0, values.length);
		axis.setLabel(eixoX);
//		axis.setTickLabelFont(new Font("Lucida Sans Unicode", Font.PLAIN, 18));
		axis.setTickLabelFont(new Font("Arial", Font.PLAIN, 18));
		
		axis = chart.getXYPlot().getRangeAxis();
		axis.setLabel(eixoY);
//		axis.setTickLabelFont(new Font("Lucida Sans Unicode", Font.PLAIN, 18));
		axis.setTickLabelFont(new Font("Arial", Font.PLAIN, 18));
		
		chart.setBackgroundPaint(Color.white);
		return chart;
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

	/**
	 * M�todo acessor para obter o valor do atributo values.
	 * 
	 * @return O atributo values
	 */
	public double[] getValues() {
		return values;
	}

	/**
	 * M�todo acessor para alterar o valor do atributo values.
	 * 
	 * @param values
	 *            O valor a ser usado para o atributo values.
	 */
	public void setValues(double[] values) {
		this.values = values;
		chart = createChart(eixoX, eixoY);
		panel.setChart(chart);
		repaint();
	}
}
