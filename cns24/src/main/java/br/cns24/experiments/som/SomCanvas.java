package br.cns24.experiments.som;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.swing.JComponent;

import br.cns24.experiments.setup.ApplicationSetup;
import br.cns24.experiments.setup.MetricExperimentSetup;
import br.cns24.models.TModel;
import br.cns24.util.FormatUtils;

public class SomCanvas extends JComponent implements MouseMotionListener {
	private static final long serialVersionUID = 1L;

	private List<PointSprite> points = new Vector<>();

	private Map<TModel, Color> mapColors = new HashMap<TModel, Color>();

	private int width;

	private int height;

	private ApplicationSetup setup;

	private static final Font fontTitle = new Font("Arial Narrow, Arial",
			Font.BOLD, 14);

	private static final Font defaultFont = new Font("Arial Narrow, Arial",
			Font.PLAIN, 12);

	private static final String title = "ESTUDO DO AGRUPAMENTO DE MODELOS DE REDES COMPLEXAS USANDO MAPAS AUTO-ORGANIZ�VEIS";

	public static final int LEGEND_WIDTH = 175;
	public static final int yIniLegend = 80;
	public static final int xIni = 10;
	public static final int lineHeightLegend = 16;
	public static final int widthSquareLegend = 10;

	private List<PointSprite> sprites = new Vector<>();

	public SomCanvas(int width, int height, List<PointSprite> points,
			ApplicationSetup setup) {
		this.points = points;
		this.width = width;
		this.height = height;
		this.setup = setup;
		addMouseMotionListener(this);
	}

	@Override
	public void update(Graphics g) {
		paint(g);
	}

	@Override
	public void paint(Graphics g) {
		g.setColor(Color.white);
		g.fillRect(0, 0, width, height);
		((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);

		Graphics2D g2 = (Graphics2D) g;

		for (PointSprite sprite : points) {
			if (sprite.isShowDetails()) {
				// desenhar raio de influ�ncia
				int xRaio = (int) ((width - LEGEND_WIDTH - 2 * 20.0) * (setup
						.getSomSetup().getRaioVizinhanca() / 100.0));
				int yRaio = (int) ((height - yIniLegend - 50) * (setup
						.getSomSetup().getRaioVizinhanca() / 100.0));
				g.setColor(Color.LIGHT_GRAY);
				g.fillOval(sprite.getX() - xRaio / 2,
						sprite.getY() - yRaio / 2, xRaio, yRaio);
				g.setColor(Color.white);
				g.fillRect(0, 0, width, yIniLegend - 10);
				g.fillRect(0, height - 50, width, height);
				g.fillRect(0, 0, xIni, height);
				g.fillRect(width - LEGEND_WIDTH - 20, 0, LEGEND_WIDTH, height);
			}
		}

		g.setFont(fontTitle);
		g.setColor(Color.BLACK);
		g.drawString(title,
				(width - getFontMetrics(fontTitle).stringWidth(title)) / 2, 30);

		g.setFont(defaultFont);

		for (PointSprite sprite : points) {
			sprite.draw(g2);
			if (sprite.isShowDetails()) {
				// desenhar raio de influ�ncia

				g.setColor(Color.BLACK);
				g.drawString(
						sprite.getNetwork().getModel().toString()
								+ "; "
								+ sprite.getNetwork().getSetup().getType()
										.getDescription()
								+ " = "
								+ FormatUtils.getInstance().getValue(
										sprite.getNetwork().getSetup()
												.getFirstValue()), 10,
						height - 40);
				String metricas = "";
				for (MetricExperimentSetup metric : setup.getMetrics()) {
					double value = sprite.getNetwork().getMetricValues()
							.get(metric.getMetric());
					metricas += metric.getMetric().getShortDescription()
							+ " = " + FormatUtils.getInstance().getValue(value)
							+ ". ";
				}
				g.drawString(metricas, 10, height - 28);
			}
		}

		int i = 0;
		g.setColor(Color.BLACK);
		g.drawRect(width - LEGEND_WIDTH - widthSquareLegend, yIniLegend
				- lineHeightLegend, LEGEND_WIDTH - widthSquareLegend,
				mapColors.size() * lineHeightLegend + widthSquareLegend);
		if (!mapColors.isEmpty()) {
			for (PointSprite pointSprite : sprites) {
				pointSprite.draw((Graphics2D) g);
			}
			for (TModel model : TModel.values()) {
				g.setColor(Color.BLACK);
				g.drawString(model.toString(), width - LEGEND_WIDTH
						+ lineHeightLegend, i * lineHeightLegend + yIniLegend);
				i++;
			}
		}
	}

	private boolean containsX(int xTela, int xSprite) {
		return xTela > xSprite - PointSprite.WIDTH
				&& xTela < xSprite + PointSprite.WIDTH;
	}

	private boolean containsY(int yTela, int ySprite) {
		return yTela > ySprite - PointSprite.WIDTH
				&& yTela < ySprite + PointSprite.WIDTH;
	}

	public void mouseMoved(int x, int y) {
		for (PointSprite sprite : points) {
			sprite.setShowDetails(false);
		}
		for (PointSprite sprite : points) {
			if (containsX(x, sprite.getX()) && containsY(y, sprite.getY())) {
				sprite.setShowDetails(true);
				break;
			}
		}
		repaint();
	}

	public List<PointSprite> getPoints() {
		return points;
	}

	public void setPoints(List<PointSprite> points) {
		this.points = points;
		repaint();
	}

	public Map<TModel, Color> getMapColors() {
		return mapColors;
	}

	public void setMapColors(Map<TModel, Color> mapColors) {
		this.mapColors = mapColors;
		sprites.clear();
		int i = 0;
		for (TModel model : TModel.values()) {
			sprites.add(new PointSprite(width - LEGEND_WIDTH, i
					* lineHeightLegend + yIniLegend - widthSquareLegend / 2,
					null, mapColors.get(model), i));
			i++;
		}
		repaint();
	}

	@Override
	public void mouseDragged(MouseEvent e) {
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		mouseMoved(e.getX(), e.getY());
	}
}
