/*
 * ****************************************************************************
 * Copyright (c) 2013
 * Todos os direitos reservados, com base nas leis brasileiras de copyright
 * Este software � confidencial e de propriedade intelectual da UFPE
 * ****************************************************************************
 * Projeto: BONS - Brazilian Optical Network Simulator
 * Arquivo: GmlCanvasViewer.java
 * ****************************************************************************
 * Hist�rico de revis�es
 * Nome				Data		Descri��o
 * ****************************************************************************
 * Danilo Ara�jo	12/10/2013		Vers�o inicial
 * ****************************************************************************
 */
package br.cns24.gui;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Vector;

import javax.imageio.ImageIO;
import javax.swing.JComponent;

import br.cns24.model.GmlData;
import br.cns24.model.GmlEdge;
import br.cns24.model.GmlNode;

/**
 * 
 * @author Danilo
 * @since 12/10/2013
 */
public class GmlCanvas extends JComponent {
	private GmlData gml = new GmlData();

	private int size = 6;

	private int width;

	private int height;

	private double xScale;

	private double yScale;

	private int outBorders = 30;

	private int innerBorders = 30;

	private int rNode = size * 4;

	private String title;

	private int numNetworks;

	private List<Line2D> edges = new Vector<>();

	private List<String> edgeInfos = new Vector<>();

	private List<Ellipse2D> nodes = new Vector<>();

	private Font titleFont = new Font("Arial", Font.PLAIN, 3 * size);

	private Font nodeFont = new Font("Arial", Font.PLAIN, 2 * size);

	private String informations = "";
	
	private String status = "";

	private Image map;

	public GmlCanvas(GmlData gml, int width, int height, int networkIndex, int numNetworks, Image map) {
		this.gml = gml;
		this.height = height;
		this.width = width;
		this.map = map;
		calculateScales(gml, width, height);
		this.numNetworks = numNetworks;
		title = buildTitle(gml, networkIndex, numNetworks);

		buildSprites(gml, width);
	}

	/**
	 * @param gml
	 * @param width
	 * @param height
	 */
	private void calculateScales(GmlData gml, int width, int height) {
		double dy = gml.getMaxLatitude() - gml.getMinLatitude();
		double dx = gml.getMaxLongitude() - gml.getMinLongitude();
		double diff = 0;
		if (dx / 2 > dy) {
			diff = dx / 2 - dy;

			gml.setMinLatitude(gml.getMinLatitude() - diff / 2);
			gml.setMaxLatitude(gml.getMaxLatitude() + diff / 2);
		} else {
			diff = dy - dx / 2;

			gml.setMinLongitude(gml.getMinLongitude() - 2 * diff);
			gml.setMaxLongitude(gml.getMaxLongitude() + 2 * diff);
		}
		dy = gml.getMaxLatitude() - gml.getMinLatitude();
		dx = gml.getMaxLongitude() - gml.getMinLongitude();
		
		double min = dy;
		if (dx > min) {
			min = dx;
		}
		this.xScale = (width - 2 * outBorders - 2 * innerBorders) / dx;
		this.yScale = (height - 2 * outBorders - 2 * innerBorders) / dy;
	}

	/**
	 * @param gml
	 * @param width
	 */
	private void buildSprites(GmlData gml, int width) {
		edges.clear();
		edgeInfos.clear();
		nodes.clear();
		for (GmlEdge edge : gml.getEdges()) {
			if (edge.getSource().visible() && edge.getTarget().visible()) {
				if (edge.getSource().getLongitude() - edge.getTarget().getLongitude() > 260) {
					edges.add(new Line2D.Double(getXScreen(edge.getSource().getLongitude()), getYScreen(edge
							.getSource().getLatitude()), width - outBorders, getYScreen(edge.getTarget().getLatitude())));
					edges.add(new Line2D.Double(outBorders, getYScreen(edge.getSource().getLatitude()), getXScreen(edge
							.getTarget().getLongitude()), getYScreen(edge.getTarget().getLatitude())));
					edgeInfos.add(edge.getLabel());
				} else if (edge.getTarget().getLongitude() - edge.getSource().getLongitude() > 260) {
					edges.add(new Line2D.Double(outBorders, getYScreen(edge.getTarget().getLatitude()), getXScreen(edge
							.getSource().getLongitude()), getYScreen(edge.getSource().getLatitude())));
					edges.add(new Line2D.Double(getXScreen(edge.getTarget().getLongitude()), getYScreen(edge
							.getTarget().getLatitude()), width - outBorders, getYScreen(edge.getSource().getLatitude())));
					edgeInfos.add(edge.getLabel());
				} else {
					edges.add(new Line2D.Double(getXScreen(edge.getSource().getLongitude()), getYScreen(edge
							.getSource().getLatitude()), getXScreen(edge.getTarget().getLongitude()), getYScreen(edge
							.getTarget().getLatitude())));
				}
				edgeInfos.add(edge.getLabel());
			}
		}

		for (GmlNode node : gml.getNodes()) {
			if (node.visible()) {
				nodes.add(new Ellipse2D.Double(getXScreen(node.getLongitude()) - rNode / 2, getYScreen(node
						.getLatitude()) - rNode / 2, rNode, rNode));
			}
		}
		status = gml.getStatus();
		BufferedImage bImg = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		Graphics2D g2 = bImg.createGraphics();
		draw(g2, true);
		try {
			ImageIO.write(bImg, "PNG", new File("C:\\doutorado\\datasets\\internet topology\\"
					+ gml.getName().toLowerCase() + ".png"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void zoom(boolean aumentar) {
		if (aumentar) {
			if (size < 40) {
				size += 2;
			}
		} else if (size > 2) {
			size -= 2;
		}
		rNode = size * 4;
		nodeFont = new Font("Arial", Font.PLAIN, size * 2);
		nodes.clear();

		for (GmlNode node : gml.getNodes()) {
			if (node.visible()) {
				nodes.add(new Ellipse2D.Double(getXScreen(node.getLongitude()) - rNode / 2, getYScreen(node
						.getLatitude()) - rNode / 2, rNode, rNode));
			}
		}
		repaint();
	}

	/**
	 * @param gml
	 * @param networkIndex
	 * @param numNetworks
	 * @return
	 */
	private String buildTitle(GmlData gml, int networkIndex, int numNetworks) {
		// return gml.getInformations().get("GeoLocation") + " " +
		// gml.getInformations().get("Network") + ": "
		// + gml.getNodes().size() + " nodes, " + gml.getEdges().size() +
		// " edges, "
		// + gml.getInformations().get("DateYear") + ". More information: " +
		// gml.getInformations().get("Source")
		// + " (" + networkIndex + "/" + numNetworks + ").";
		return gml.getNodes().size() + "-node " + gml.getInformations().get("GeoLocation") + " "
				+ gml.getInformations().get("Network") + " (" + gml.getInformations().get("DateYear") + ").";
	}

	public void updateNetwork(GmlData gml, int networkIndex) {
		this.gml = gml;
		calculateScales(gml, width, height);
		title = buildTitle(gml, networkIndex, numNetworks);

		buildSprites(gml, width);
		repaint();
	}

	public int getXScreen(double longitude) {
		return outBorders + innerBorders + (int) Math.floor((longitude - gml.getMinLongitude()) * xScale);
	}

	public int getYScreen(double latitude) {
		return (height - 2 * outBorders - 2 * innerBorders - (int) Math.floor((latitude - gml.getMinLatitude())
				* yScale))
				+ outBorders + innerBorders;
	}

	public void over(int x, int y) {
		informations = "";
		int i = 0;
		for (Line2D line : edges) {
			if (line.intersects(x, y, size, size)) {
				informations = "Link specification: " + edgeInfos.get(i);
				break;
			}
			i++;
		}
		i = 0;
		for (Ellipse2D node : nodes) {
			if (node.contains(x, y)) {
				informations = "Node location: " + gml.getNodes().get(i).getLabel();
				break;
			}
			i++;
		}
		repaint();
	}

	@Override
	public void update(Graphics g) {
		paint(g);
	}

	@Override
	public void paint(Graphics g) {
		draw(g, false);
	}

	public void draw(Graphics g, boolean export) {
		g.setColor(Color.white);
		g.fillRect(0, 0, width, height);
		((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		// g.drawImage(map, outBorders + innerBorders, outBorders +
		// innerBorders, width - 2 * (outBorders + innerBorders),
		// height - 60 - 2 * (outBorders + innerBorders), this);
		// g.drawImage(map, outBorders, outBorders, width - 2 * (outBorders),
		// height - 60 - 2 * (outBorders ), this);
		g.setColor(Color.BLACK);
		g.setFont(titleFont);
		if (!export){
			g.drawString(title, (outBorders + innerBorders) / 2, (outBorders + innerBorders) / 2);
			g.drawString(informations, (outBorders + innerBorders) / 2, height - outBorders - 20);
			
			g.drawString(status, (outBorders + innerBorders) / 2, height - (outBorders + innerBorders) / 2);
		}
		g.setFont(nodeFont);

		for (Line2D edge : edges) {
			((Graphics2D) g).draw(edge);
		}
		int i = 0;
		for (GmlNode node : gml.getNodes()) {
			if (node.visible()) {
				g.setColor(Color.BLACK);
				((Graphics2D) g).fill(nodes.get(i));
				g.setColor(Color.WHITE);
				if (node.getId() > 9) {
					g.drawString(node.getId() + "", getXScreen(node.getLongitude()) - nodeFont.getSize() / 2 - 1,
							getYScreen(node.getLatitude()) + nodeFont.getSize() / 4);
				} else {
					g.drawString(node.getId() + "", getXScreen(node.getLongitude()) - nodeFont.getSize() / 4,
							getYScreen(node.getLatitude()) + nodeFont.getSize() / 4);
				}
				i++;
			}

		}
	}

	/**
	 * @return o valor do atributo status
	 */
	public String getStatus() {
		return status;
	}

	/**
	 * Altera o valor do atributo status
	 * @param status O valor para setar em status
	 */
	public void setStatus(String status) {
		this.status = status;
		repaint();
	}
}
