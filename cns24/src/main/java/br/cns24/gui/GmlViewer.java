/*
 * ****************************************************************************
 * Copyright (c) 2013
 * Todos os direitos reservados, com base nas leis brasileiras de copyright
 * Este software � confidencial e de propriedade intelectual da UFPE
 * ****************************************************************************
 * Projeto: BONS - Brazilian Optical Network Simulator
 * Arquivo: GmlViewer.java
 * ****************************************************************************
 * Hist�rico de revis�es
 * Nome				Data		Descri��o
 * ****************************************************************************
 * Danilo Ara�jo	12/10/2013		Vers�o inicial
 * ****************************************************************************
 */
package br.cns24.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.SplashScreen;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.KeyStroke;

import br.cns24.experiments.ApplicationFrame;
import br.cns24.experiments.ComplexNetwork;
import br.cns24.experiments.MenuActions;
import br.cns24.experiments.PrintUtilities;
import br.cns24.experiments.StatusBar;
import br.cns24.experiments.Util;
import br.cns24.model.GmlData;
import br.cns24.persistence.ComplexNetworkDaoXml;
import br.cns24.persistence.GmlDao;
import br.cns24.persistence.SimpleEdgeDataSet;

/**
 * 
 * @author Danilo
 * @since 12/10/2013
 */
public class GmlViewer implements ActionListener, MouseListener, MouseWheelListener, MouseMotionListener {
	private static final String LABORATORIO_DE_REDES_COMPLEXAS = "Laborat�rio de Redes Complexas UFPE-UPE :: CNS - Complex Network Simulator v1.0 beta";

	private static final JFileChooser fileChooser = new JFileChooser();

	private StatusBar statusBar;

	private JFrame frame;

	private GmlCanvas canvas;

	private GmlDao dao = new GmlDao();

	private List<GmlData> networks = new ArrayList<>();

	private int networkId;

	private void initialize() throws ParseException {
		frame = new ApplicationFrame(LABORATORIO_DE_REDES_COMPLEXAS, 915, 680);
		frame.setIconImage(Util.loadImage("images/icon_complenet.png"));
		frame.getContentPane().setBackground(Color.white);
		statusBar = new StatusBar();
		List<String> specificExclusions = new Vector();
		specificExclusions.add("Iceland RHnet");
		specificExclusions.add("Switzerland SWITCH");
		specificExclusions.add("Poland PIONIER");

		Container content = frame.getContentPane();
		content.setLayout(new BorderLayout());

		JPanel simulations = new JPanel();
		simulations.setLayout(new BorderLayout());
		StringBuffer sb1 = new StringBuffer();
		StringBuffer sb2 = new StringBuffer();
		String basePath = "C:\\doutorado\\submiss�es\\momag_2014\\dataset\\";
		File file = new File(basePath);
		Map<String, Integer> anos = new HashMap<String, Integer>();
		boolean generateSummary = true;
		for (String s : file.list()) {
			if (s.endsWith("gml")) {
				GmlData data = dao.loadGmlData(basePath + s);
				if (!specificExclusions.contains(data.getName())
						&& data.getNodes().size() > 10
						&& data.getEdges().size() > 9
						&& !data.containsIsolatedNodes()
						&& data.containsGbLink()
						&& data.getInformations().get("Backbone").toLowerCase().equals("1")
						// &&
						// data.getInformations().get("Layer").toLowerCase().equals("fibre")
						&& !data.getInformations().get("GeoExtent").toLowerCase().equals("continent")
						&& !data.getInformations().get("GeoExtent").toLowerCase().equals("continent+")
						&& !data.getInformations().get("GeoExtent").toLowerCase().equals("global")
						&& !data.getInformations().get("GeoExtent").toLowerCase().equals("unclassified")
						&& !data.getInformations().get("GeoExtent").toLowerCase().equals("metro")
						&& data.getInformations().get("Type").toLowerCase().equals("ren")
						&& data.getInformations().get("Source").contains("http")) {
					if (anos.get(data.getName()) != null) {
						if (anos.get(data.getName()) < Integer.parseInt(data.getInformations().get("DateYear"))) {
							networks.remove(data);
							anos.remove(data.getName());
						} else {
							continue;
						}
					}
					networks.add(data);
					anos.put(data.getName(), Integer.parseInt(data.getInformations().get("DateYear")));
				}
			}
		}
		if (generateSummary) {
			for (GmlData data : networks) {
				String[] aSummary = data.createComplexNetwork().getSummary("");
				sb1.append(aSummary[0]);
				sb2.append(aSummary[1]);
			}
			try {
				FileWriter fw = new FileWriter(new File("C:\\doutorado\\datasets\\internet topology\\summary.txt"));
				fw.write(sb1.toString());
				fw.close();
				fw = new FileWriter(new File("C:\\doutorado\\datasets\\internet topology\\summary_nds.txt"));
				fw.write(sb2.toString());
				fw.close();
			} catch (IOException e) {
				e.printStackTrace();
			}

		}

		canvas = new GmlCanvas(networks.get(0), (int) (1.3 * (640 + 2 * 30)), (int) (1.3 * (425 + 2 * 30)),
				networkId + 1, networks.size(), Util.loadImage("images/map.png"));
		canvas.addMouseListener(this);
		canvas.addMouseMotionListener(this);
		canvas.addMouseWheelListener(this);
		simulations.add(canvas, BorderLayout.CENTER);
		content.add(simulations, BorderLayout.CENTER);

		frame.setJMenuBar(createMenu(this));

		createSplashScreen();
		frame.pack();
		frame.setVisible(true);
	}

	private void createSplashScreen() {
		final SplashScreen splash = SplashScreen.getSplashScreen();
		if (splash != null) {
			Graphics2D g = splash.createGraphics();
			if (g != null) {
				for (int i = 0; i < 100; i++) {
					renderSplashFrame(g, Util.loadImage("images/about_screen.png"));
					splash.update();
					try {
						Thread.sleep(90);
					} catch (InterruptedException e) {
					}
				}
			}
			splash.close();
		}
	}

	static void renderSplashFrame(Graphics2D g, Image image) {
		g.drawImage(image, 0, 0, null);
	}

	private JMenuBar createMenu(ActionListener al) {
		JMenuBar menuBar;
		JMenu menu;
		JMenuItem menuItem;

		menuBar = new JMenuBar();

		menu = new JMenu("Ferramentas");
		menu.setMnemonic(KeyEvent.VK_F);
		menu.getAccessibleContext().setAccessibleDescription("Menu principal da aplica��o.");
		menu.addActionListener(al);
		menuBar.add(menu);

		menuItem = new JMenuItem(MenuActions.NEW.getDescription(), new ImageIcon(
				Util.loadImage("images/document24.png")));
		menuItem.addActionListener(al);
		menuItem.setActionCommand(MenuActions.NEW.toString());
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, ActionEvent.CTRL_MASK));
		menuItem.setMnemonic(KeyEvent.VK_R);
		menuItem.getAccessibleContext().setAccessibleDescription("Criar uma nova simula��o de redes complexas.");
		menu.add(menuItem);

		menuItem = new JMenuItem(MenuActions.OPEN.getDescription(),
				new ImageIcon(Util.loadImage("images/folder24.png")));
		menuItem.setActionCommand(MenuActions.OPEN.toString());
		menuItem.addActionListener(al);
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, ActionEvent.CTRL_MASK));
		menuItem.setMnemonic(KeyEvent.VK_A);
		menuItem.getAccessibleContext().setAccessibleDescription("Abrir uma rede previamente gravada.");
		menu.add(menuItem);

		menuItem = new JMenuItem(MenuActions.SAVE.getDescription(), new ImageIcon(Util.loadImage("images/save24.png")));
		menuItem.setActionCommand(MenuActions.SAVE.toString());
		menuItem.addActionListener(al);
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK));
		menuItem.setMnemonic(KeyEvent.VK_S);
		menu.add(menuItem);

		menu.addSeparator();

		menuItem = new JMenuItem(MenuActions.EXIT.getDescription(), new ImageIcon(Util.loadImage("images/exit.png")));
		menuItem.setMnemonic(KeyEvent.VK_R);
		menuItem.addActionListener(al);
		menuItem.setActionCommand(MenuActions.EXIT.toString());
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F4, ActionEvent.ALT_MASK));
		menuItem.getAccessibleContext().setAccessibleDescription("Finaliza a aplica��o.");
		menu.add(menuItem);

		return menuBar;
	}

	public static void main(String[] args) {
		GmlViewer app = new GmlViewer();
		try {
			app.initialize();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals(MenuActions.OPEN.toString())) {
			abrirRede();
		} else if (e.getActionCommand().equals(MenuActions.SAVE.toString())) {
			salvarRede();
		} else if (e.getActionCommand().equals(MenuActions.IMPORT.toString())) {
			importarRede();
		} else if (e.getActionCommand().equals(MenuActions.NEW.toString())) {
		}
	}

	private void fecharAplicacao() {
		if (JOptionPane.showConfirmDialog(canvas, "Confirma o encerramento da aplica��o?",
				LABORATORIO_DE_REDES_COMPLEXAS, JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
			System.exit(0);
		}
	}

	private void salvarRede() {
	}

	private void abrirRede() {
		int returnVal = fileChooser.showOpenDialog(this.statusBar);

		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File file = fileChooser.getSelectedFile();
			try {
				ComplexNetwork network = ComplexNetworkDaoXml.getInstance().load(file.getAbsolutePath());
				network.evaluate();
			} catch (IOException e1) {
				JOptionPane.showMessageDialog(this.canvas, "Arquivo inv�lido!");
			}
		}
	}

	private void importarRede() {
		int returnVal = fileChooser.showOpenDialog(this.statusBar);

		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File file = fileChooser.getSelectedFile();
			try {
				ComplexNetwork network = SimpleEdgeDataSet.importNetwork(file.getAbsolutePath());
				network.evaluate();
			} catch (IOException e1) {
				JOptionPane.showMessageDialog(this.canvas, "Arquivo inv�lido!");
			}
		}
	}

	private void saveImage() {
		// TODO
		// if (selecionado canvas){
		//
		// }

		Rectangle r = canvas.getBounds();

		try {
			BufferedImage i = new BufferedImage(r.width, r.height, BufferedImage.TYPE_INT_RGB);
			Graphics g = i.getGraphics();
			canvas.paint(g);

			int returnVal = fileChooser.showSaveDialog(canvas);

			if (returnVal == JFileChooser.APPROVE_OPTION) {
				File file = fileChooser.getSelectedFile();
				ImageIO.write(i, "png", file);
			}
		} catch (IOException e) {
			JOptionPane.showMessageDialog(canvas, "N�o foi poss�vel gravar a imagem.");
		}
	}

	private void printImage() {
		PrintUtilities.printComponent(canvas);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
	 */
	@Override
	public void mouseClicked(MouseEvent e) {
		if (e.getButton() == MouseEvent.BUTTON1) {
			if (networkId == networks.size() - 1) {
				networkId = 0;
			} else {
				networkId++;
			}
		} else {
			if (networkId == 0) {
				networkId = networks.size() - 1;
			} else {
				networkId--;
			}
		}
		canvas.updateNetwork(networks.get(networkId), networkId + 1);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent)
	 */
	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.MouseListener#mouseExited(java.awt.event.MouseEvent)
	 */
	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
	 */
	@Override
	public void mousePressed(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
	 */
	@Override
	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.MouseWheelListener#mouseWheelMoved(java.awt.event.
	 * MouseWheelEvent)
	 */
	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		if (e.getWheelRotation() < 0) {
			canvas.zoom(true);
		} else {
			canvas.zoom(false);
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.awt.event.MouseMotionListener#mouseDragged(java.awt.event.MouseEvent
	 * )
	 */
	@Override
	public void mouseDragged(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.awt.event.MouseMotionListener#mouseMoved(java.awt.event.MouseEvent)
	 */
	@Override
	public void mouseMoved(MouseEvent e) {
		canvas.over(e.getX(), e.getY());
	}

}
