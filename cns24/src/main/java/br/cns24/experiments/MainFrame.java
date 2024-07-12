/**
 * 
 */
package br.cns24.experiments;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.SplashScreen;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.NumberFormat;
import java.text.ParseException;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import br.cns24.TMetric;
import br.cns24.experiments.nodePositions.CircularNetwork;
import br.cns24.experiments.nodePositions.PowerLawNodePositions;
import br.cns24.experiments.setup.ApplicationSetup;
import br.cns24.experiments.setup.MetricExperimentSetup;
import br.cns24.experiments.setup.ParameterExperimentSetup;
import br.cns24.experiments.som.SomExperimentDialog;
import br.cns24.models.Barabasi;
import br.cns24.models.BarabasiDensity;
import br.cns24.models.ErdosRenyiM;
import br.cns24.models.ErdosRenyiP;
import br.cns24.models.GenerativeProcedure;
import br.cns24.models.Gilbert;
import br.cns24.models.KRegular;
import br.cns24.models.NewmanWatts;
import br.cns24.models.TModel;
import br.cns24.models.Toroid;
import br.cns24.models.WattsStrogatz;
import br.cns24.models.WattsStrogatzDensity;
import br.cns24.persistence.ApplicationSetupDaoXml;
import br.cns24.persistence.ComplexNetworkDaoXml;
import br.cns24.persistence.ResultSetDaoXml;
import br.cns24.persistence.SimpleEdgeDataSet;
import br.cns24.transformations.DegreeMatrix;
import br.cns24.util.FirstKindBesselFunction;
import br.cns24.util.FormatUtils;

/**
 * @author Danilo
 * 
 */
public class MainFrame implements ActionListener {
	private static final String LABORATORIO_DE_REDES_COMPLEXAS = "Laborat�rio de Redes Complexas UFPE-UPE :: CNS - Complex Network Simulator v1.0 beta";

	private static final JFileChooser fileChooser = new JFileChooser();

	private AboutDialog aboutDialog;

	private StatusBar statusBar;

	private JButton btnNewNodes;

	private JFrame frame;

	private GraphCanvas canvas;

	private XYGraphCanvas histogramDegreeDistrib;

	private XYGraphCanvas histogramLaplacianValues;

	private XYGraphCanvas histogramFourierTransform;

	private XYGraphCanvas histogramFourierImpares;

	private Integer[][] adjacencyMatrix;

	private double[][] positions;

	private static final NumberFormat nf = NumberFormat.getNumberInstance();

	private Image upeIcon;

	private Image ufpeIcon;

	private Toolbar toolbar;

	private int canvasWidth;

	private int canvasHeight;

	private int bottomCanvasWidth;

	private int bottomCanvasHeight;

	private ResultsTablePanel resultsPanel;

	private ConfigurationPanel configPanel;

	private SomExperimentDialog somDialog;

	private ApplicationSetup setup;

	private JTable tableResults;

	private void initialize() throws ParseException {
		nf.setMaximumFractionDigits(2);
		nf.setMinimumFractionDigits(2);
		frame = new ApplicationFrame(LABORATORIO_DE_REDES_COMPLEXAS);
		upeIcon = Util.loadImage("images/brasao_upe.png");
		ufpeIcon = Util.loadImage("images/brasao_ufpe.png");
		frame.setIconImage(Util.loadImage("images/icon_complenet.png"));
		frame.getContentPane().setBackground(Color.white);
		setup = loadConfigurations();
		statusBar = new StatusBar();
		toolbar = new Toolbar(this, setup);
		double percentCanvas = 0.40;

		Container content = frame.getContentPane();
		content.setLayout(new BorderLayout());

		canvasWidth = (int) (frame.getWidth() * percentCanvas) - 5;
		canvasHeight = frame.getHeight() - 320;

		bottomCanvasWidth = frame.getWidth() - 5;
		bottomCanvasHeight = 200;

		executeSimulation(true);

		JPanel simulations = new JPanel();
		simulations.setLayout(new BorderLayout());

		ResultsTableModel model = new ResultsTableModel(new String[] { "ID", "Modelo" }, new Object[][] { { "0",
				"Indefinido" } });
		criarTableResults();
		resultsPanel = new ResultsTablePanel((int) (frame.getWidth() * (1 - percentCanvas)) - 5, model, tableResults);

		JTabbedPane paneCanvas = new JTabbedPane();
		paneCanvas.add("Distribui��o de Grau dos nós", histogramDegreeDistrib);
		paneCanvas.add("Autovalores do Laplaciano", histogramLaplacianValues);
		paneCanvas.add("Transformada de Fourier do Laplaciano (pares)", histogramFourierTransform);
		paneCanvas.add("Transformada de Fourier do Laplaciano (impares)", histogramFourierImpares);
		paneCanvas.setPreferredSize(new Dimension(frame.getWidth() - 5, 220));

		simulations.add(canvas, BorderLayout.CENTER);
		simulations.add(resultsPanel, BorderLayout.WEST);

		content.add(toolbar, BorderLayout.NORTH);
		content.add(simulations, BorderLayout.CENTER);
		// content.add(statusBar, BorderLayout.SOUTH);
		content.add(paneCanvas, BorderLayout.SOUTH);

		configPanel = new ConfigurationPanel(frame, this, LABORATORIO_DE_REDES_COMPLEXAS);

		// somDialog = new SomExperimentDialog(frame,
		// LABORATORIO_DE_REDES_COMPLEXAS, setup);
		//
		// somDialog.carregarRedeExistente();
		aboutDialog = new AboutDialog(frame, LABORATORIO_DE_REDES_COMPLEXAS, "version 1.0 beta",
				Util.loadImage("images/about_screen.png"));

		frame.setJMenuBar(createMenu(this));

		createSplashScreen();
		executeSimulation(true);
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

	public void saveConfigurations(ApplicationSetup setup, boolean exit) {
		try {
			ApplicationSetupDaoXml.getInstance().save("cns-config.xml", setup);
		} catch (Exception e) {
			JOptionPane.showMessageDialog(canvas, "N�o foi poss�vel gravar as configura��es.");
		}
		if (!exit
				&& JOptionPane.showConfirmDialog(canvas,
						"Deseja realizar uma nova simula��o com as altera��es nas configura��es?",
						LABORATORIO_DE_REDES_COMPLEXAS, JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
			this.setup = setup;
			executeSimulation(true);
			canvas.setSetup(setup);
		}
	}

	public ApplicationSetup loadConfigurations() {
		ApplicationSetup appSetup = new ApplicationSetup();

		try {
			appSetup = ApplicationSetupDaoXml.getInstance().load("cns-config.xml");
		} catch (Exception e) {
			try {
				saveConfigurations(appSetup, true);
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		}

		return appSetup;
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

		menuItem = new JMenuItem(MenuActions.IMPORT.getDescription(), new ImageIcon(
				Util.loadImage("images/folder24.png")));
		menuItem.setActionCommand(MenuActions.IMPORT.toString());
		menuItem.addActionListener(al);
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_I, ActionEvent.CTRL_MASK));
		menuItem.setMnemonic(KeyEvent.VK_I);
		menuItem.getAccessibleContext().setAccessibleDescription("Importar uma rede previamente gravada.");
		menu.add(menuItem);

		menuItem = new JMenuItem(MenuActions.SAVE.getDescription(), new ImageIcon(Util.loadImage("images/save24.png")));
		menuItem.setActionCommand(MenuActions.SAVE.toString());
		menuItem.addActionListener(al);
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK));
		menuItem.setMnemonic(KeyEvent.VK_S);
		menu.add(menuItem);

		menu.addSeparator();

		menuItem = new JMenuItem(MenuActions.CONFIG.getDescription(), new ImageIcon(
				Util.loadImage("images/gear_wheel24.png")));
		menuItem.setActionCommand(MenuActions.CONFIG.toString());
		menuItem.addActionListener(al);
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, ActionEvent.CTRL_MASK));
		menuItem.setMnemonic(KeyEvent.VK_C);
		menu.add(menuItem);

		menuItem = new JMenuItem(MenuActions.NEW_SIMULATION.getDescription(), new ImageIcon(
				Util.loadImage("images/magic_wand24.png")));
		menuItem.setActionCommand(MenuActions.NEW_SIMULATION.toString());
		menuItem.addActionListener(al);
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, ActionEvent.CTRL_MASK));
		menuItem.setMnemonic(KeyEvent.VK_N);
		menu.add(menuItem);

		menuItem = new JMenuItem(MenuActions.SOM_EXPERIMENT.getDescription(), new ImageIcon(
				Util.loadImage("images/som.png")));
		menuItem.setActionCommand(MenuActions.SOM_EXPERIMENT.toString());
		menuItem.addActionListener(al);
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_M, ActionEvent.CTRL_MASK));
		menuItem.setMnemonic(KeyEvent.VK_M);
		menu.add(menuItem);

		menu.addSeparator();

		menuItem = new JMenuItem(MenuActions.PRINT.getDescription(),
				new ImageIcon(Util.loadImage("images/print24.png")));
		menuItem.setActionCommand(MenuActions.PRINT.toString());
		menuItem.addActionListener(al);
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, ActionEvent.CTRL_MASK));
		menuItem.setMnemonic(KeyEvent.VK_I);
		menu.add(menuItem);

		menuItem = new JMenuItem(MenuActions.EXPORT.getDescription(), new ImageIcon(
				Util.loadImage("images/picture24.png")));
		menuItem.setActionCommand(MenuActions.EXPORT.toString());
		menuItem.addActionListener(al);
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E, ActionEvent.CTRL_MASK));
		menuItem.setMnemonic(KeyEvent.VK_E);
		menu.add(menuItem);

		menuItem = new JMenuItem(MenuActions.EXPORT_TABLE_TXT.getDescription(), new ImageIcon(
				Util.loadImage("images/text24.png")));
		menuItem.setActionCommand(MenuActions.EXPORT_TABLE_TXT.toString());
		menuItem.addActionListener(al);
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_T, ActionEvent.CTRL_MASK));
		menuItem.setMnemonic(KeyEvent.VK_T);
		menu.add(menuItem);

		menuItem = new JMenuItem(MenuActions.EXPORT_TABLE_XML.getDescription(), new ImageIcon(
				Util.loadImage("images/workflow24.png")));
		menuItem.setActionCommand(MenuActions.EXPORT_TABLE_XML.toString());
		menuItem.addActionListener(al);
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, ActionEvent.CTRL_MASK));
		menuItem.setMnemonic(KeyEvent.VK_X);
		menu.add(menuItem);

		menu.addSeparator();

		menuItem = new JMenuItem(MenuActions.EXIT.getDescription(), new ImageIcon(Util.loadImage("images/exit.png")));
		menuItem.setMnemonic(KeyEvent.VK_R);
		menuItem.addActionListener(al);
		menuItem.setActionCommand(MenuActions.EXIT.toString());
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F4, ActionEvent.ALT_MASK));
		menuItem.getAccessibleContext().setAccessibleDescription("Finaliza a aplica��o.");
		menu.add(menuItem);

		menu = new JMenu("Ajuda");
		menu.setMnemonic(KeyEvent.VK_J);
		menu.getAccessibleContext().setAccessibleDescription("Sobre a aplica��o.");
		menuBar.add(menu);

		menuItem = new JMenuItem(MenuActions.ABOUT.getDescription(), new ImageIcon(Util.loadImage("images/info24.png")));
		menuItem.addActionListener(al);
		menuItem.setActionCommand(MenuActions.ABOUT.toString());
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.ALT_MASK));
		menuItem.setMnemonic(KeyEvent.VK_S);
		menuItem.getAccessibleContext().setAccessibleDescription("Informa��es sobre a aplica��o.");
		menu.add(menuItem);

		return menuBar;
	}

	public void executeSimulation(boolean forceNewNodes) {
		int numNodesTela = 0;
		try {
			numNodesTela = NumberFormat.getInstance().parse(toolbar.getTxtNodes().getText()).intValue();
		} catch (ParseException e) {
			JOptionPane.showMessageDialog(canvas, "Número de nós inv�lido.");
		}
		adjacencyMatrix = new Integer[setup.getDefaultNumNodes()][setup.getDefaultNumNodes()];

		if (numNodesTela != setup.getDefaultNumNodes() || forceNewNodes) {
			adjacencyMatrix = new Integer[numNodesTela][numNodesTela];
			setup.setDefaultNumNodes(numNodesTela);

			positions = CircularNetwork.getInstance().createNodePositions(setup.getDefaultNumNodes(),
					DegreeMatrix.getInstance().transform(adjacencyMatrix));
		}

		int i = 2;
		String[] header = new String[setup.getMetrics().size() + 2];
		header[0] = "ID";
		for (MetricExperimentSetup metric : setup.getMetrics()) {
			header[i] = metric.getMetric().toString();
			i++;
		}
		int qtdeExperimentos = 1;
		for (ParameterExperimentSetup parameter : setup.getParameters()) {
			if (parameter.getType().getModel() == (TModel) toolbar.getModelsList().getSelectedItem()
					&& parameter.isVariar()) {
				qtdeExperimentos = (int) ((parameter.getLastValue() - parameter.getFirstValue()) / parameter.getStep()) + 1;
			}
		}

		Object[][] data = new Object[qtdeExperimentos][setup.getMetrics().size() + 2];
		ComplexNetwork network = null;

		network = criarRedes(header, data, network);
		if (positions == null) {
			positions = PowerLawNodePositions.getInstance().createNodePositions(setup.getDefaultNumNodes(),
					DegreeMatrix.getInstance().transform(network.getAdjacencyMatrix()));
			network.setNodePositions(positions);
		}
		atualizarResultados(header, data, network);
	}

	private ComplexNetwork criarRedes(String[] header, Object[][] data, ComplexNetwork network) {
		switch ((TModel) toolbar.getModelsList().getSelectedItem()) {
		case ERDOS_RENYI_N_M:
			network = simulateErdosM(header, data, network);
			break;
		case ERDOS_RENYI_N_P:
			network = simulateErdosP(header, data, network);
			break;
		case GILBERT:
			network = simulateGilbert(header, data, network);
			break;
		case TOROID:
			network = simulateToroid(header, data, network);
			break;
		case K_REGULAR:
			network = simulatekRegular(header, data, network);
			break;
		case WATTS_STROGATZ:
			network = simulateWatts(header, data, network, false);
			break;
		case WATTS_STROGATZ_DENSITY:
			network = simulateWatts(header, data, network, true);
			break;
		case NEWMAN_WATTS:
			network = simulateNewmanWatts(header, data, network);
			break;
		case BARABASI:
			network = simulateBarabasi(header, data, network);
			break;
		case BARABASI_DENSITY:
			network = simulateBarabasiDensity(header, data, network);
			break;
		default:
		}
		return network;
	}

	private void atualizarResultados(String[] header, Object[][] data, ComplexNetwork network) {
		network.calculateRealEigenvalues();

		if (canvas == null) {
			canvas = new GraphCanvas(network, canvasWidth, canvasHeight, upeIcon, ufpeIcon, setup);
		} else {
			canvas.setNetwork(network);
		}
		if (histogramDegreeDistrib == null) {
			histogramDegreeDistrib = new XYGraphCanvas("Distribui��o de Grau dos nós",
					network.getSequenceDegreeDistribution(), bottomCanvasWidth, bottomCanvasHeight, "d", "Pr(d)");
		} else {
			histogramDegreeDistrib.setValues(network.getSequenceDegreeDistribution());
		}
		if (histogramLaplacianValues == null) {
			histogramLaplacianValues = new XYGraphCanvas("Autovalores da Matriz Laplaciana",
					network.getRealEigenvalues(), bottomCanvasWidth, bottomCanvasHeight);
		} else {
			histogramLaplacianValues.setValues(network.getRealEigenvalues());
		}
		if (histogramFourierTransform == null) {
			histogramFourierTransform = new XYGraphCanvas(
					"Transformada Discreta de Fourier dos Autovalores da Matriz Laplaciana (pares)",
					network.getFftValuesPares(), bottomCanvasWidth, bottomCanvasHeight);
		} else {
			histogramFourierTransform.setValues(network.getFftValuesPares());
		}

		FirstKindBesselFunction bessel = new FirstKindBesselFunction();
		double[] aBessel = new double[network.getFftValues().length];
		for (int i = 0; i < aBessel.length; i++) {
			aBessel[i] = bessel.getJ(0, i / 10.0);
		}
		if (histogramFourierImpares == null) {
			histogramFourierImpares = new XYGraphCanvas(
					"Transformada Discreta de Fourier dos Autovalores da Matriz Laplaciana (impares)",
					network.getFftValuesImpares(), bottomCanvasWidth, bottomCanvasHeight);
		} else {
			histogramFourierImpares.setValues(network.getFftValuesImpares());
		}

		ResultsTableModel model = new ResultsTableModel(header, data);
		if (tableResults == null) {
			criarTableResults();
		}
		tableResults.setModel(model);
		tableResults.getColumnModel().getColumn(0).setPreferredWidth(30);
		tableResults.repaint();
	}

	private ComplexNetwork simulateBarabasi(String[] header, Object[][] data, ComplexNetwork network) {
		Barabasi barabasi = null;
		double firstValue = 0;
		double lastValue = 0;
		header[1] = "nós";
		int firstValueNumLinks = 0;
		int lastValueNumLinks = 0;
		int stepNumLinks = 0;
		for (ParameterExperimentSetup parameter : setup.getParameters()) {
			if (parameter.getType() == TParameterModel.M_LINKS_BARABASI) {
				firstValueNumLinks = (int) parameter.getFirstValue();
				lastValueNumLinks = (int) parameter.getLastValue();
				stepNumLinks = (int) parameter.getStep();
				break;
			}
		}
		if (lastValue > setup.getDefaultNumNodes()) {
			lastValue = setup.getDefaultNumNodes();
		}
		if (firstValue > setup.getDefaultNumNodes()) {
			firstValue = setup.getDefaultNumNodes();
		}
		int k = 0;
		for (int d = firstValueNumLinks; d <= lastValueNumLinks && k < data.length; d += stepNumLinks) {
			barabasi = new Barabasi(d);
			adjacencyMatrix = barabasi.grow(null, setup.getDefaultNumNodes());
			network = new ComplexNetwork(k + 1, adjacencyMatrix, positions, (TModel) toolbar.getModelsList()
					.getSelectedItem(), setup);
			double[][] positions = CircularNetwork.getInstance().createNodePositions(setup.getDefaultNumNodes(),
					DegreeMatrix.getInstance().transform(network.getAdjacencyMatrix()));
			network.setNodePositions(positions);
			network.evaluate();
			int j = 2;
			data[k][0] = network;
			data[k][1] = new Double(setup.getDefaultNumNodes());
			for (MetricExperimentSetup metric : setup.getMetrics()) {
				data[k][j] = FormatUtils.getInstance().getValue(network.getMetricValues().get(metric.getMetric()));
				j++;
			}
			k++;
		}
		return network;
	}

	private ComplexNetwork simulateBarabasiDensity(String[] header, Object[][] data, ComplexNetwork network) {
		BarabasiDensity barabasi = null;
		double firstValue = 0;
		double lastValue = 0;
		header[1] = "nós";
		double stepNumLinks = 0;
		for (ParameterExperimentSetup parameter : setup.getParameters()) {
			if (parameter.getType() == TParameterModel.DENSITY_BARABASI) {
				firstValue = parameter.getFirstValue();
				lastValue = parameter.getLastValue();
				stepNumLinks = parameter.getStep();
				break;
			}
		}
		int k = 0;
		for (double d = firstValue; d <= lastValue && k < data.length; d += stepNumLinks) {
			barabasi = new BarabasiDensity(d);
			adjacencyMatrix = barabasi.grow(null, setup.getDefaultNumNodes());
			network = new ComplexNetwork(k + 1, adjacencyMatrix, positions, (TModel) toolbar.getModelsList()
					.getSelectedItem(), setup);
			double[][] positions = CircularNetwork.getInstance().createNodePositions(setup.getDefaultNumNodes(),
					DegreeMatrix.getInstance().transform(network.getAdjacencyMatrix()));
			network.setNodePositions(positions);
			network.evaluate();
			int j = 2;
			data[k][0] = network;
			data[k][1] = new Double(setup.getDefaultNumNodes());
			for (MetricExperimentSetup metric : setup.getMetrics()) {
				data[k][j] = FormatUtils.getInstance().getValue(network.getMetricValues().get(metric.getMetric()));
				j++;
			}
			k++;
		}
		return network;
	}

	private static int getK(double density, int n) {
		int k = Math.max((int) Math.floor((density * (n - 1)) / 2.0) + 1, 1);
		return k;
	}

	private ComplexNetwork simulateWatts(String[] header, Object[][] data, ComplexNetwork network, boolean flagDensity) {
		GenerativeProcedure watts = null;
		double firstValue = 0.02;
		double lastValue = 0.30;
		double step = 0.02;
		double density = 0.00;
		header[1] = "Probabilidade";
		int kWS = 2;
		if (flagDensity) {
			int k = 0;
			for (double d = firstValue; d < lastValue && k < data.length; d += step) {
				watts = new WattsStrogatzDensity(0.15, getK(d, setup.getDefaultNumNodes()), d);

				adjacencyMatrix = watts.transform(adjacencyMatrix);
				network = new ComplexNetwork(k + 1, adjacencyMatrix, positions, (TModel) toolbar.getModelsList()
						.getSelectedItem(), setup);
				double[][] positions = CircularNetwork.getInstance().createNodePositions(setup.getDefaultNumNodes(),
						DegreeMatrix.getInstance().transform(network.getAdjacencyMatrix()));
				network.setNodePositions(positions);
				network.evaluate();
				int j = 2;
				data[k][0] = network;
				data[k][1] = new Double(d);
				for (MetricExperimentSetup metric : setup.getMetrics()) {
					data[k][j] = FormatUtils.getInstance().getValue(network.getMetricValues().get(metric.getMetric()));
					j++;
				}
				k++;
			}

		} else {
			for (ParameterExperimentSetup parameter : setup.getParameters()) {
				if (parameter.getType() == TParameterModel.WS_PROBABILITY) {
					firstValue = parameter.getFirstValue();
					lastValue = parameter.getLastValue();
					step = parameter.getStep();
				}
				if (parameter.getType() == TParameterModel.K_WS) {
					kWS = (int) parameter.getFirstValue();
				}
			}
			int k = 0;
			for (double d = firstValue; d < lastValue && k < data.length; d += step) {
				watts = new WattsStrogatz(d, kWS);

				adjacencyMatrix = watts.transform(adjacencyMatrix);
				network = new ComplexNetwork(k + 1, adjacencyMatrix, positions, (TModel) toolbar.getModelsList()
						.getSelectedItem(), setup);
				double[][] positions = CircularNetwork.getInstance().createNodePositions(setup.getDefaultNumNodes(),
						DegreeMatrix.getInstance().transform(network.getAdjacencyMatrix()));
				network.setNodePositions(positions);
				network.evaluate();
				int j = 2;
				data[k][0] = network;
				data[k][1] = new Double(d);
				for (MetricExperimentSetup metric : setup.getMetrics()) {
					data[k][j] = FormatUtils.getInstance().getValue(network.getMetricValues().get(metric.getMetric()));
					j++;
				}
				k++;
			}

		}
		return network;
	}

	private ComplexNetwork simulateNewmanWatts(String[] header, Object[][] data, ComplexNetwork network) {
		NewmanWatts watts = null;
		double firstValue = 0;
		double lastValue = 0;
		double step = 0;
		header[1] = "Probabilidade";
		for (ParameterExperimentSetup parameter : setup.getParameters()) {
			if (parameter.getType() == TParameterModel.NW_PROBABILITY) {
				firstValue = parameter.getFirstValue();
				lastValue = parameter.getLastValue();
				step = parameter.getStep();
				break;
			}
		}
		int k = 0;
		for (double d = firstValue; d < lastValue && k < data.length; d += step) {
			watts = new NewmanWatts(d);
			adjacencyMatrix = watts.transform(adjacencyMatrix);
			network = new ComplexNetwork(k + 1, adjacencyMatrix, positions, (TModel) toolbar.getModelsList()
					.getSelectedItem(), setup);
			double[][] positions = CircularNetwork.getInstance().createNodePositions(setup.getDefaultNumNodes(),
					DegreeMatrix.getInstance().transform(network.getAdjacencyMatrix()));
			network.setNodePositions(positions);
			network.evaluate();
			int j = 2;
			data[k][0] = network;
			data[k][1] = new Double(d);
			for (MetricExperimentSetup metric : setup.getMetrics()) {
				data[k][j] = FormatUtils.getInstance().getValue(network.getMetricValues().get(metric.getMetric()));
				j++;
			}
			k++;
		}
		return network;
	}

	private ComplexNetwork simulatekRegular(String[] header, Object[][] data, ComplexNetwork network) {
		KRegular kRegular = null;
		double firstValue = 0;
		double lastValue = 0;
		double step = 0;
		header[1] = "Parâmetro k";
		for (ParameterExperimentSetup parameter : setup.getParameters()) {
			if (parameter.getType() == TParameterModel.K) {
				firstValue = parameter.getFirstValue();
				lastValue = parameter.getLastValue();
				step = parameter.getStep();
				break;
			}
		}
		if (lastValue > adjacencyMatrix.length - 1) {
			lastValue = adjacencyMatrix.length - 1;
		}
		int k = 0;
		for (int d = (int) firstValue; d < lastValue && k < data.length; d += step) {
			kRegular = new KRegular(d);
			adjacencyMatrix = kRegular.transform(adjacencyMatrix);
			network = new ComplexNetwork(k + 1, adjacencyMatrix, positions, (TModel) toolbar.getModelsList()
					.getSelectedItem(), setup);
			double[][] positions = CircularNetwork.getInstance().createNodePositions(setup.getDefaultNumNodes(),
					DegreeMatrix.getInstance().transform(network.getAdjacencyMatrix()));
			network.setNodePositions(positions);
			network.evaluate();
			int j = 2;
			data[k][0] = network;
			data[k][1] = new Double(d);
			for (MetricExperimentSetup metric : setup.getMetrics()) {
				data[k][j] = FormatUtils.getInstance().getValue(network.getMetricValues().get(metric.getMetric()));
				j++;
			}
			k++;
		}
		return network;
	}

	private ComplexNetwork simulateToroid(String[] header, Object[][] data, ComplexNetwork network) {
		Toroid kRegular = null;
		double firstValue = 0.1;
		double lastValue = 0.5;
		double step = 0;
		header[1] = "Density";
		for (ParameterExperimentSetup parameter : setup.getParameters()) {
			if (parameter.getType() == TParameterModel.TOROID_DENSITY) {
				firstValue = parameter.getFirstValue();
				lastValue = parameter.getLastValue();
				step = parameter.getStep();
				break;
			}
		}
		int k = 0;
		for (double d = firstValue; d < lastValue && k < data.length; d += step) {
			kRegular = new Toroid(d, setup.getDefaultNumNodes());
			adjacencyMatrix = kRegular.transform(adjacencyMatrix);
			network = new ComplexNetwork(0, adjacencyMatrix, positions, (TModel) toolbar.getModelsList()
					.getSelectedItem(), setup);
			double[][] positions = CircularNetwork.getInstance().createNodePositions(setup.getDefaultNumNodes(),
					DegreeMatrix.getInstance().transform(network.getAdjacencyMatrix()));
			network.setNodePositions(positions);
			network.evaluate();
			int j = 2;
			data[k][0] = network;
			data[k][1] = new Double(network.getMetricValues().get(TMetric.DENSITY));
			for (MetricExperimentSetup metric : setup.getMetrics()) {
				data[k][j] = FormatUtils.getInstance().getValue(network.getMetricValues().get(metric.getMetric()));
				j++;
			}
			k++;
		}
		return network;
	}

	private ComplexNetwork simulateGilbert(String[] header, Object[][] data, ComplexNetwork network) {
		Gilbert gilbert = null;
		double firstValue = 0;
		double lastValue = 0;
		double step = 0;
		header[1] = "Probabilidade";
		for (ParameterExperimentSetup parameter : setup.getParameters()) {
			if (parameter.getType() == TParameterModel.PROBABILITY_GILBERT) {
				firstValue = parameter.getFirstValue();
				lastValue = parameter.getLastValue();
				step = parameter.getStep();
				break;
			}
		}
		int k = 0;
		for (double d = firstValue; d < lastValue && k < data.length; d += step) {
			gilbert = new Gilbert(d);
			adjacencyMatrix = gilbert.transform(adjacencyMatrix);
			network = new ComplexNetwork(k + 1, adjacencyMatrix, positions, (TModel) toolbar.getModelsList()
					.getSelectedItem(), setup);
			double[][] positions = CircularNetwork.getInstance().createNodePositions(setup.getDefaultNumNodes(),
					DegreeMatrix.getInstance().transform(network.getAdjacencyMatrix()));
			network.setNodePositions(positions);
			network.evaluate();
			int j = 2;
			data[k][0] = network;
			data[k][1] = new Double(d);
			for (MetricExperimentSetup metric : setup.getMetrics()) {
				data[k][j] = FormatUtils.getInstance().getValue(network.getMetricValues().get(metric.getMetric()));
				j++;
			}
			k++;
		}
		return network;
	}

	private ComplexNetwork simulateErdosM(String[] header, Object[][] data, ComplexNetwork network) {
		ErdosRenyiM erdos = null;
		double firstValue = 0;
		double lastValue = 0;
		double step = 0;
		header[1] = "Densidade";
		for (ParameterExperimentSetup parameter : setup.getParameters()) {
			if (parameter.getType() == TParameterModel.DENSITY_ERDOS) {
				firstValue = parameter.getFirstValue();
				lastValue = parameter.getLastValue();
				step = parameter.getStep();
				break;
			}
		}
		int k = 0;
		for (double d = firstValue; d < lastValue && k < data.length; d += step) {
			erdos = new ErdosRenyiM(d, setup.getDefaultNumNodes());
			adjacencyMatrix = erdos.transform(adjacencyMatrix);
			network = new ComplexNetwork(k + 1, adjacencyMatrix, positions, (TModel) toolbar.getModelsList()
					.getSelectedItem(), setup);
			double[][] positions = CircularNetwork.getInstance().createNodePositions(setup.getDefaultNumNodes(),
					DegreeMatrix.getInstance().transform(network.getAdjacencyMatrix()));
			network.setNodePositions(positions);
			network.evaluate();
			int j = 2;
			data[k][0] = network;
			data[k][1] = new Double(d);
			for (MetricExperimentSetup metric : setup.getMetrics()) {
				data[k][j] = FormatUtils.getInstance().getValue(network.getMetricValues().get(metric.getMetric()));
				j++;
			}
			k++;
		}
		return network;
	}

	private ComplexNetwork simulateErdosP(String[] header, Object[][] data, ComplexNetwork network) {
		ErdosRenyiP erdos = null;
		double firstValue = 0;
		double lastValue = 0;
		double step = 0;
		header[1] = "Probabilidade";
		for (ParameterExperimentSetup parameter : setup.getParameters()) {
			if (parameter.getType() == TParameterModel.PROBABILITY_ERDOS) {
				firstValue = parameter.getFirstValue();
				lastValue = parameter.getLastValue();
				step = parameter.getStep();
				break;
			}
		}
		int k = 0;
		for (double d = firstValue; d < lastValue && k < data.length; d += step) {
			erdos = new ErdosRenyiP(d);
			adjacencyMatrix = erdos.transform(adjacencyMatrix);
			network = new ComplexNetwork(k + 1, adjacencyMatrix, positions, (TModel) toolbar.getModelsList()
					.getSelectedItem(), setup);
			double[][] positions = CircularNetwork.getInstance().createNodePositions(setup.getDefaultNumNodes(),
					DegreeMatrix.getInstance().transform(network.getAdjacencyMatrix()));
			network.setNodePositions(positions);
			network.evaluate();
			int j = 2;
			data[k][0] = network;
			data[k][1] = new Double(d);
			for (MetricExperimentSetup metric : setup.getMetrics()) {
				data[k][j] = FormatUtils.getInstance().getValue(network.getMetricValues().get(metric.getMetric()));
				j++;
			}
			k++;
		}
		return network;
	}

	private void criarTableResults() {
		final MainFrame mFrame = this;
		tableResults = new JTable();
		tableResults.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent lse) {
				mFrame.selecionarRede();
			}
		});
	}

	/**
	 * Atualiza os dados do painel gr�fico quando uma linha � selecionada na
	 * tabela de resultados.
	 */
	public void selecionarRede() {
		if (tableResults.getSelectedRow() != -1 && tableResults.getValueAt(tableResults.getSelectedRow(), 0) != null) {
			ComplexNetwork network = (ComplexNetwork) tableResults.getValueAt(tableResults.getSelectedRow(), 0);
			updateNetworkOnScreen(network);
		}
	}

	/**
	 * Atualiza os dados da rede na visualiza��o gr�fica do grafo e nos
	 * histogramas.
	 * 
	 * @param network
	 *            Nova rede.
	 */
	private void updateNetworkOnScreen(ComplexNetwork network) {
		network.calculateRealEigenvalues();
		network.calculateDegreeSequenceDistribution();
		canvas.setNetwork(network);
		histogramDegreeDistrib.setValues(network.getSequenceDegreeDistribution());
		histogramFourierTransform.setValues(network.getFftValuesPares());
		histogramLaplacianValues.setValues(network.getRealEigenvalues());
		histogramFourierImpares.setValues(network.getFftValuesImpares());
	}

	public static void main(String[] args) {
		MainFrame app = new MainFrame();
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
			executeSimulation(true);
		} else if (e.getActionCommand().equals(MenuActions.NEW_SIMULATION.toString())) {
			executeSimulation(false);
		} else if (e.getActionCommand().equals(MenuActions.CONFIG.toString())) {
			configPanel.setVisible(true);
		} else if (e.getActionCommand().equals(MenuActions.SOM_EXPERIMENT.toString())) {
			somDialog.setSetup(setup);
			somDialog.setVisible(true);
		} else if (e.getActionCommand().equals(MenuActions.ABOUT.toString())) {
			aboutDialog.setVisible(true);
		} else if (e.getActionCommand().equals(MenuActions.EXPORT.toString())) {
			saveImage();
		} else if (e.getActionCommand().equals(MenuActions.PRINT.toString())) {
			printImage();
		} else if (e.getActionCommand().equals(MenuActions.EXPORT_TABLE_TXT.toString())) {
			exportTableToTxt();
		} else if (e.getActionCommand().equals(MenuActions.EXPORT_TABLE_XML.toString())) {
			exportTableToXml();
		} else if (e.getActionCommand().equals(MenuActions.EXIT.toString())) {
			fecharAplicacao();
		}
	}

	private void fecharAplicacao() {
		if (JOptionPane.showConfirmDialog(canvas, "Confirma o encerramento da aplica��o?",
				LABORATORIO_DE_REDES_COMPLEXAS, JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
			setup.setDefaultModel((TModel) toolbar.getModelsList().getSelectedItem());
			saveConfigurations(setup, true);
			System.exit(0);
		}
	}

	private void salvarRede() {
		int returnVal = fileChooser.showSaveDialog(this.statusBar);

		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File file = fileChooser.getSelectedFile();
			try {
				ComplexNetworkDaoXml.getInstance().save(file.getAbsolutePath(), canvas.getNetwork());
			} catch (IOException e1) {
				e1.printStackTrace();
				JOptionPane.showMessageDialog(this.btnNewNodes,
						"N�o foi poss�vel salvar a rede. Escolha outro nome de arquivo.");
			}
		}
	}

	private void abrirRede() {
		int returnVal = fileChooser.showOpenDialog(this.statusBar);

		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File file = fileChooser.getSelectedFile();
			try {
				ComplexNetwork network = ComplexNetworkDaoXml.getInstance().load(file.getAbsolutePath());
				network.evaluate();
				updateNetworkOnScreen(network);
			} catch (IOException e1) {
				JOptionPane.showMessageDialog(this.btnNewNodes, "Arquivo inv�lido!");
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
				updateNetworkOnScreen(network);
			} catch (IOException e1) {
				JOptionPane.showMessageDialog(this.btnNewNodes, "Arquivo inv�lido!");
			}
		}
	}

	private void exportTableToTxt() {
		StringBuffer content = new StringBuffer();
		for (int i = 0; i < tableResults.getRowCount(); i++) {
			if (tableResults.getValueAt(i, 0) == null) {
				break;
			}
			for (int j = 0; j < tableResults.getColumnCount(); j++) {
				content.append(tableResults.getValueAt(i, j)).append(";");
			}
			content.append("\n");
		}
		int returnVal = fileChooser.showSaveDialog(this.statusBar);

		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File file = fileChooser.getSelectedFile();
			try {
				FileWriter fw = new FileWriter(file);
				fw.write(content.toString());
				fw.close();
			} catch (IOException e1) {
				JOptionPane.showMessageDialog(this.btnNewNodes,
						"N�o foi poss�vel salvar os resultados. Escolha outro nome de arquivo.");
			}
		}
	}

	private void exportTableToXml() {
		ResultSet rs = new ResultSet();
		rs.setSetup(setup);
		for (int i = 0; i < tableResults.getRowCount(); i++) {
			if (tableResults.getValueAt(i, 0) == null) {
				break;
			}
			rs.getNetworks().add((ComplexNetwork) tableResults.getValueAt(i, 0));
		}
		int returnVal = fileChooser.showSaveDialog(this.statusBar);

		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File file = fileChooser.getSelectedFile();
			try {
				ResultSetDaoXml.getInstance().save(file.getAbsolutePath(), rs);
			} catch (IOException e1) {
				JOptionPane.showMessageDialog(this.btnNewNodes,
						"N�o foi poss�vel salvar os resultados. Escolha outro nome de arquivo.");
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

	/**
	 * M�todo acessor para obter o valor do atributo histogram.
	 * 
	 * @return O atributo histogram
	 */
	public XYGraphCanvas getHistogram() {
		return histogramDegreeDistrib;
	}

	/**
	 * M�todo acessor para alterar o valor do atributo histogram.
	 * 
	 * @param histogram
	 *            O valor a ser usado para o atributo histogram.
	 */
	public void setHistogram(XYGraphCanvas histogram) {
		this.histogramDegreeDistrib = histogram;
	}
}
