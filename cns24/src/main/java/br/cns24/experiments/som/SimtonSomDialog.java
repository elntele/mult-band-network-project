package br.cns24.experiments.som;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import br.cns24.experiments.ComplexNetwork;
import br.cns24.experiments.ResultSet;
import br.cns24.experiments.TParameterModel;
import br.cns24.experiments.setup.ApplicationSetup;
import br.cns24.experiments.setup.MetricExperimentSetup;
import br.cns24.experiments.setup.ParameterExperimentSetup;
import br.cns24.models.TModel;
import br.cns24.persistence.RedeKohonenXmlDao;
import br.cns24.persistence.ResultSetDaoXml;
import br.cns24.persistence.SimonSolutionDao;
import br.cns24.som.Neuronio;
import br.cns24.som.PadraoTreinamento;
import br.cns24.som.RedeException;
import br.cns24.som.RedeKohonen;
import br.cns24.util.RandomUtils;

public class SimtonSomDialog extends JDialog implements ActionListener {
	private static final String EXPORTAR_PARA_PNG = "Exportar para PNG";

	private static final String INICIAR = "Iniciar Treinamento";

	private static final String FECHAR = "Fechar";

	private static final String EXPORTAR_PARA_TXT = "Exportar para XML";

	private static final String SALVAR_TREINAMENTO = "Salvar treinamento";

	private static final long serialVersionUID = 1L;

	private SomCanvas canvas;

	private int WIDTH = 810;

	private int HEIGHT = 590;

	private JButton btnExportarPng;

	private JButton btnIniciar;

	private JButton btnSalvarTreinamento;

	private JButton btnFechar;

	private JButton btnExportarTxt;

	private RedeKohonen kohonen;

	private ApplicationSetup setup;

	private Map<TModel, Color> mapColors;

	private ResultSet resultSet = new ResultSet();

	private int widthCanvas;

	private int heightCanvas;

	private int bordersCanvas;

	private static final Color[] colors = new Color[] { Color.RED, Color.GREEN, Color.BLUE, Color.BLACK, Color.ORANGE,
			Color.YELLOW, Color.CYAN, Color.MAGENTA, Color.BLACK };

	private static final JFileChooser fileChooser = new JFileChooser();

	public SimtonSomDialog(JFrame parent, String title, ApplicationSetup setup) {
		super(parent, title, true);
		setLayout(new BorderLayout());
		HEIGHT = (int) (parent.getHeight() * 0.85);
		WIDTH = HEIGHT + 220;
		setPreferredSize(new Dimension(WIDTH, HEIGHT));
		this.setup = setup;
		resultSet.setSetup(setup);
		kohonen = new RedeKohonen(setup.getMetrics().size(), setup.getSomSetup().getTamanhoGrid());

		List<PointSprite> sprites = new Vector<>();

		widthCanvas = WIDTH - 20;
		heightCanvas = HEIGHT - 50;
		bordersCanvas = 20;
		canvas = new SomCanvas(widthCanvas, HEIGHT - 50, sprites, setup);

		mapColors = new HashMap<>();
		int i = 0;
		for (TModel model : TModel.values()) {
			mapColors.put(model, colors[i]);
			i++;
		}
		canvas.setMapColors(mapColors);

		JPanel panelButtons = new JPanel();

		btnIniciar = new JButton(INICIAR);
		btnIniciar.addActionListener(this);
		panelButtons.add(btnIniciar);

		btnSalvarTreinamento = new JButton(SALVAR_TREINAMENTO);
		btnSalvarTreinamento.setEnabled(false);
		btnSalvarTreinamento.addActionListener(this);
		panelButtons.add(btnSalvarTreinamento);

		btnExportarTxt = new JButton(EXPORTAR_PARA_TXT);
		btnExportarTxt.setEnabled(false);
		btnExportarTxt.addActionListener(this);
		panelButtons.add(btnExportarTxt);

		btnExportarPng = new JButton(EXPORTAR_PARA_PNG);
		btnExportarPng.addActionListener(this);
		btnExportarPng.setEnabled(false);
		panelButtons.add(btnExportarPng);

		btnFechar = new JButton(FECHAR);
		btnFechar.addActionListener(this);
		panelButtons.add(btnFechar);

		add(canvas, BorderLayout.CENTER);
		add(panelButtons, BorderLayout.SOUTH);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		center(parent);
		pack();
		setVisible(false);
	}

	public void carregarRedeExistente() {
	}

	public void center(JFrame parent) {
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		int x = (screenSize.width - WIDTH) / 2;
		int y = (screenSize.height - HEIGHT) / 2;
		setLocation(x, y);
	}

	public void iniciarExperimento() {
		btnExportarTxt.setEnabled(false);
		btnExportarPng.setEnabled(false);
		btnIniciar.setEnabled(false);
		btnFechar.setEnabled(false);
		repaint();
		List<PointSprite> sprites = new Vector<>();
		PadraoTreinamento padrao = new PadraoTreinamento();
		List<Double> caracteristicas;
		List<Double> caracteristicasO;
		List<ComplexNetwork> networks = new Vector<>();
		ComplexNetwork network;

		double[][] nodePositions = new double[14][2];
		int i;
		for (i = 0; i < 14; i++) {
			nodePositions[i][0] = RandomUtils.getInstance().nextInt(500);
			nodePositions[i][1] = RandomUtils.getInstance().nextInt(500);
		}
		networks.clear();
		try {
			for (int d = 6; d <= 8; d++ ){
				List<Integer[][]> customs = SimonSolutionDao.getInstance().readNetworksFromFileObject(
						"C:\\Temp\\exp_2012080" + d + "\\_nsgaii_C2_M10_50_1,0000_0,0600_0.400_var.txt");

				for (Integer[][] matrix : customs) {
					network = new ComplexNetwork(i, matrix, new double[14][2], TModel.CUSTOM, setup);
					network.evaluate();
					network.setSetup(new ParameterExperimentSetup(TParameterModel.NUM_NODES_CUSTOM, 14));
					networks.add(network);
					i++;
				}
			}
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		
		List<PadraoTreinamento> padroes = new Vector<>();
		List<PadraoTreinamento> padroesO = new Vector<>();
		for (ComplexNetwork net : networks) {
			caracteristicas = new Vector<>();
			caracteristicasO = new Vector<>();

			net.evaluate();
			for (MetricExperimentSetup metric : setup.getMetrics()) {
				if (net.getMetricValues().get(metric.getMetric()) > 50) {
					caracteristicas.add(new Double(50));
					caracteristicasO.add(new Double(50));
				} else if (net.getMetricValues().get(metric.getMetric()) < -50) {
					caracteristicas.add(new Double(-50));
					caracteristicasO.add(new Double(-50));
				} else {
					caracteristicas.add(net.getMetricValues().get(metric.getMetric()));
					caracteristicasO.add(net.getMetricValues().get(metric.getMetric()));
				}
			}

			padrao = new PadraoTreinamento(String.valueOf(net.getId()), caracteristicas);
			padroes.add(padrao);
			padroesO.add(new PadraoTreinamento(String.valueOf(net.getId()), caracteristicas));
		}

		kohonen.setPadroesTreinamento(padroes);
		kohonen.setPadroesTreinamentoOriginal(padroes);
		try {
			System.out.println("Iniciando aprendizado com " + networks.size() + " padr�es...");
			kohonen.iniciarAprendizado(networks.size() * setup.getSomSetup().getNumIteracoes(), setup.getSomSetup()
					.getRaioVizinhanca() / 100.0, setup.getSomSetup().getTaxaAprendizado() / 100.0);

			btnExportarTxt.setEnabled(true);
			btnExportarPng.setEnabled(true);
			btnSalvarTreinamento.setEnabled(true);
			btnIniciar.setEnabled(true);
			btnFechar.setEnabled(true);
			resultSet.getNetworks().clear();
			PointSprite sprite = null;
			i = 0;
			for (PadraoTreinamento padraoTreinado : padroes) {
				Neuronio neuronio = kohonen.buscarMelhorNeuronio(padraoTreinado.getCaracteristicasArray());
				sprite = new PointSprite((int) (bordersCanvas + neuronio.getPosX()
						* 1.0
						* ((widthCanvas - SomCanvas.LEGEND_WIDTH - 10 - 2.0 * bordersCanvas) / setup.getSomSetup()
								.getTamanhoGrid())), (int) (SomCanvas.yIniLegend + neuronio.getPosY() * 1.0
						* ((heightCanvas - SomCanvas.yIniLegend - 60.0) / setup.getSomSetup().getTamanhoGrid())),
						networks.get(i), mapColors.get(networks.get(i).getModel()), networks.get(i).getModel().getId());
				sprites.add(sprite);
				networks.get(i).setxSOM(neuronio.getPosX());
				networks.get(i).setySOM(neuronio.getPosY());
				resultSet.getNetworks().add(networks.get(i));
				i++;
			}
			canvas.setPoints(sprites);
			canvas.setMapColors(mapColors);
		} catch (RedeException e) {
			e.printStackTrace();
		}
	}

	private void exportarPng() {

	}

	private void salvarTreinamento() {
		File file = new File("kohonen.xml");
		try {
			RedeKohonenXmlDao.getInstance().save(file.getAbsolutePath(), kohonen);
			ResultSetDaoXml.getInstance().save("kohonen-networks.xml", resultSet);
			JOptionPane.showMessageDialog(this.btnExportarPng, "Treinamento salvo com sucesso.");
		} catch (IOException e1) {
			e1.printStackTrace();
			JOptionPane.showMessageDialog(this.btnExportarPng, "N�o foi poss�vel salvar o treinamento.");
		}
	}

	private void exportarTxt() {
		int returnVal = fileChooser.showSaveDialog(this.btnExportarPng);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File file = fileChooser.getSelectedFile();
			try {
				ResultSetDaoXml.getInstance().save(file.getAbsolutePath(), resultSet);
			} catch (IOException e1) {
				JOptionPane.showMessageDialog(this.btnExportarPng,
						"N�o foi poss�vel salvar o maps. Escolha outro nome de arquivo.");
			}
		}
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals(FECHAR)) {
			setVisible(false);
			dispose();
		} else if (e.getActionCommand().equals(INICIAR)) {
			iniciarExperimento();
		} else if (e.getActionCommand().equals(EXPORTAR_PARA_PNG)) {
			exportarPng();
		} else if (e.getActionCommand().equals(EXPORTAR_PARA_TXT)) {
			exportarTxt();
		} else if (e.getActionCommand().equals(SALVAR_TREINAMENTO)) {
			salvarTreinamento();
		}
	}

	public ApplicationSetup getSetup() {
		return setup;
	}

	public void setSetup(ApplicationSetup setup) {
		this.setup = setup;
	}
}