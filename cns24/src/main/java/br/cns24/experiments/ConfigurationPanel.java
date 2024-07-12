package br.cns24.experiments;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.border.Border;

import br.cns24.TMetric;
import br.cns24.experiments.setup.ApplicationSetup;
import br.cns24.experiments.setup.MetricExperimentSetup;
import br.cns24.experiments.setup.ParameterExperimentSetup;
import br.cns24.models.TModel;

public class ConfigurationPanel extends JDialog implements ActionListener {
	private static final long serialVersionUID = 1L;

	private MainFrame parent;

	private JFormattedTextField txtNumNodes;

	private JComboBox<TModel> modelsList;

	private JFormattedTextField txtTaxaAprendizadoSom;

	private JFormattedTextField txtVizinhancaSom;

	private JFormattedTextField txtTamanhoGridSom;

	private JFormattedTextField txtNumIteracoesSom;

	private static final int WIDTH = 500;

	private static final int HEIGHT = 300;

	private ApplicationSetup setup;

	private JTable tableMetrics;

	private JTable tableParameterModels;

	public ConfigurationPanel(JFrame frame, MainFrame parent, String title) {
		super(frame, title, true);
		this.parent = parent;
		setup = parent.loadConfigurations();

		setPreferredSize(new Dimension(WIDTH, HEIGHT));

		JPanel generalConfigs = createGeneralConfigsPanel();
		JPanel modelsParameters = createModelsParameter();
		JPanel metricsPanel = createMetrics();
		JPanel metricsSOM = createSOM();

		Border padding = BorderFactory.createEmptyBorder(20, 20, 5, 20);
		generalConfigs.setBorder(padding);
		modelsParameters.setBorder(padding);
		metricsPanel.setBorder(padding);

		JTabbedPane tabbedPane = new JTabbedPane();
		tabbedPane.addTab("Configura��es gerais", null, generalConfigs,
				"Configura��es gerais");
		tabbedPane.addTab("Parâmetros dos modelos", null, modelsParameters,
				"Parâmetros dos modelos");
		tabbedPane.addTab("métricas consideradas", null, metricsPanel,
				"métricas consideradas");
		tabbedPane.addTab("Mapas auto-organiz�veis", null, metricsSOM,
				"Mapas auto-organiz�veis");

		JPanel panelBotoes = new JPanel();
		JButton button = new JButton("Salvar");
		button.addActionListener(this);
		panelBotoes.add(button);
		button = new JButton("Fechar");
		button.addActionListener(this);
		panelBotoes.add(button);
		button = new JButton("Restaurar Padr�o");
		button.addActionListener(this);
		panelBotoes.add(button);

		add(tabbedPane, BorderLayout.CENTER);
		add(panelBotoes, BorderLayout.SOUTH);
		center(frame);
		pack();
		setVisible(false);
	}

	public void center(JFrame parent) {
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		int x = (screenSize.width - WIDTH) / 2;
		int y = (screenSize.height - HEIGHT) / 2;
		setLocation(x, y);
	}

	private JPanel createGeneralConfigsPanel() {
		JPanel panel = new JPanel();

		panel.setLayout(null);

		JLabel label = new JLabel("Modelo:");
		label.setBounds(10, 10, 100, 24);
		modelsList = new JComboBox<TModel>(TModel.values());
		modelsList.setSelectedItem(setup.getDefaultModel());
		modelsList.setBounds(110, 10, 120, 24);

		panel.add(label);
		panel.add(modelsList);

		label = new JLabel("Número de nós:");
		label.setBounds(10, 50, 100, 24);
		txtNumNodes = new JFormattedTextField(NumberFormat.getNumberInstance());
		txtNumNodes.setText(String.valueOf(setup.getDefaultNumNodes()));
		txtNumNodes.setAlignmentX(RIGHT_ALIGNMENT);
		txtNumNodes.setBounds(110, 50, 50, 24);

		panel.add(label);
		panel.add(txtNumNodes);

		panel.add(new JLabel());
		panel.add(new JLabel());
		panel.add(new JLabel());
		panel.add(new JLabel());

		return panel;
	}

	private JPanel createSOM() {
		JPanel panel = new JPanel();

		panel.setLayout(null);

		JLabel label = new JLabel("Número de itera��es:");
		label.setBounds(10, 10, 120, 24);
		txtNumIteracoesSom = new JFormattedTextField(
				NumberFormat.getNumberInstance());
		txtNumIteracoesSom.setText(String.valueOf(setup.getSomSetup()
				.getNumIteracoes()));
		txtNumIteracoesSom.setAlignmentX(RIGHT_ALIGNMENT);
		txtNumIteracoesSom.setBounds(130, 10, 50, 24);

		panel.add(label);
		panel.add(txtNumIteracoesSom);

		label = new JLabel("Raio de vizinhan�a (%):");
		label.setBounds(10, 50, 120, 24);
		txtVizinhancaSom = new JFormattedTextField(
				NumberFormat.getNumberInstance());
		txtVizinhancaSom.setText(String.valueOf(setup.getSomSetup()
				.getRaioVizinhanca()));
		txtVizinhancaSom.setAlignmentX(RIGHT_ALIGNMENT);
		txtVizinhancaSom.setBounds(130, 50, 50, 24);

		panel.add(label);
		panel.add(txtVizinhancaSom);

		label = new JLabel("Taxa de aprendizado:");
		label.setBounds(10, 90, 120, 24);
		txtTaxaAprendizadoSom = new JFormattedTextField(
				NumberFormat.getNumberInstance());
		txtTaxaAprendizadoSom.setText(String.valueOf(setup.getSomSetup()
				.getTaxaAprendizado()));
		txtTaxaAprendizadoSom.setAlignmentX(RIGHT_ALIGNMENT);
		txtTaxaAprendizadoSom.setBounds(130, 90, 50, 24);

		panel.add(label);
		panel.add(txtTaxaAprendizadoSom);

		label = new JLabel("Tamanho do grid:");
		label.setBounds(10, 130, 120, 24);
		txtTamanhoGridSom = new JFormattedTextField(
				NumberFormat.getNumberInstance());
		txtTamanhoGridSom.setText(String.valueOf(setup.getSomSetup()
				.getTamanhoGrid()));
		txtTamanhoGridSom.setAlignmentX(RIGHT_ALIGNMENT);
		txtTamanhoGridSom.setBounds(130, 130, 50, 24);

		panel.add(label);
		panel.add(txtTamanhoGridSom);

		return panel;
	}

	private JPanel createMetrics() {
		JPanel panel = new JPanel();

		Object[][] data = new Object[TMetric.values().length][2];

		int i = 0;
		for (TMetric metric : TMetric.values()) {
			if (setup.getMetrics().contains(new MetricExperimentSetup(metric))) {
				data[i][0] = new Boolean(true);
			} else {
				data[i][0] = new Boolean(false);
			}

			data[i][1] = metric.toString();
			i++;
		}

		MetricsConfigurationTabelModel model = new MetricsConfigurationTabelModel(
				data);
		tableMetrics = new JTable(model);
		tableMetrics.setPreferredScrollableViewportSize(new Dimension(
				WIDTH - 45, 140));
		tableMetrics.setFillsViewportHeight(true);
		tableMetrics.getColumnModel().getColumn(0).setPreferredWidth(60);
		tableMetrics.getColumnModel().getColumn(1)
				.setPreferredWidth(WIDTH - 45 - 60);
		JScrollPane scrollPane = new JScrollPane(tableMetrics);

		panel.add(scrollPane);

		return panel;
	}

	private JPanel createModelsParameter() {

		JPanel panel = new JPanel();

		Object[][] data = new Object[TMetric.values().length][6];

		int i = 0;
		boolean find = false;
		for (TParameterModel metric : TParameterModel.values()) {
			find = false;
			for (ParameterExperimentSetup configSetup : setup.getParameters()) {
				if (configSetup.getType() == metric) {
					if (configSetup.isVariar()) {
						data[i][0] = new Boolean(true);
					} else {
						data[i][0] = new Boolean(false);
					}
					data[i][3] = new Double(configSetup.getFirstValue());
					data[i][4] = new Double(configSetup.getLastValue());
					data[i][5] = new Double(configSetup.getStep());
					find = true;
					break;
				}
			}
			data[i][1] = metric.getModel().toString();
			data[i][2] = metric.getDescription();
			if (!find) {
				data[i][0] = new Boolean(true);
				data[i][3] = new Double(0.0);
				data[i][4] = new Double(0.0);
				data[i][5] = new Double(0.0);
			}
			i++;
		}

		ModelsConfigurationTableModel model = new ModelsConfigurationTableModel(
				data);
		tableParameterModels = new JTable(model);
		tableParameterModels.setPreferredScrollableViewportSize(new Dimension(
				WIDTH - 45, 140));
		tableParameterModels.setFillsViewportHeight(true);
		tableParameterModels.getColumnModel().getColumn(0)
				.setPreferredWidth(60);
		tableParameterModels.getColumnModel().getColumn(1)
				.setPreferredWidth(100);
		JScrollPane scrollPane = new JScrollPane(tableParameterModels);

		panel.add(scrollPane);

		return panel;
	}

	private void salvar() {
		setup.getParameters().clear();
		ParameterExperimentSetup parameter = new ParameterExperimentSetup();
		Map<TModel, Integer> mapQtdeVariations = new HashMap<>();
		for (TModel model : TModel.values()) {
			mapQtdeVariations.put(model, 0);
		}

		try {
			setup.getSomSetup().setNumIteracoes(
					NumberFormat.getInstance()
							.parse(txtNumIteracoesSom.getText()).intValue());
			setup.getSomSetup().setRaioVizinhanca(
					NumberFormat.getInstance()
							.parse(txtVizinhancaSom.getText()).intValue());
			setup.getSomSetup().setTamanhoGrid(
					NumberFormat.getInstance()
							.parse(txtTamanhoGridSom.getText()).intValue());
			setup.getSomSetup().setTaxaAprendizado(
					NumberFormat.getInstance()
							.parse(txtTaxaAprendizadoSom.getText()).intValue());
		} catch (ParseException e1) {
			JOptionPane.showMessageDialog(tableMetrics,
					"Parâmetro de mapas auto-organiz�veis inv�lido!");
		}

		for (int i = 0; i < tableParameterModels.getRowCount(); i++) {
			if (tableParameterModels.getValueAt(i, 1) == null) {
				break;
			}

			parameter = new ParameterExperimentSetup();
			parameter.setType(TParameterModel.getType(tableParameterModels
					.getValueAt(i, 1).toString(), tableParameterModels
					.getValueAt(i, 2).toString()));
			if ((Boolean) tableParameterModels.getValueAt(i, 0)) {
				if (mapQtdeVariations.get(parameter.getType().getModel()) == 1) {
					JOptionPane
							.showMessageDialog(tableMetrics,
									"� permitido variar apenas um Parâmetro para cada modelo!");
				} else {
					mapQtdeVariations.put(parameter.getType().getModel(), 1);
					parameter.setVariar(true);
				}
			}
			parameter.setFirstValue((Double) tableParameterModels.getValueAt(i,
					3));
			parameter.setLastValue((Double) tableParameterModels.getValueAt(i,
					4));
			parameter.setStep((Double) tableParameterModels.getValueAt(i, 5));

			setup.getParameters().add(parameter);
		}

		setup.setDefaultModel((TModel) modelsList.getSelectedItem());
		try {
			setup.setDefaultNumNodes(NumberFormat.getInstance()
					.parse(txtNumNodes.getText()).intValue());
		} catch (ParseException e) {
			JOptionPane.showMessageDialog(tableMetrics,
					"Número de nós inv�lido!");
		}

		setup.getMetrics().clear();
		MetricExperimentSetup metricSetup = new MetricExperimentSetup();
		int i = 0;
		for (TMetric metric : TMetric.values()) {
			if ((Boolean) tableMetrics.getValueAt(i, 0)) {
				metricSetup = new MetricExperimentSetup();
				metricSetup.setMetric(metric);
				setup.getMetrics().add(metricSetup);
			}
			i++;
		}

		parent.saveConfigurations(setup, false);
	}

	@Override
	public void actionPerformed(ActionEvent ae) {
		if (ae.getActionCommand().equals("Salvar")) {
			salvar();
		} else if (ae.getActionCommand().equals("Fechar")) {
			setVisible(false);
			dispose();
		} else if (ae.getActionCommand().equals("Restaurar Padr�o")) {
			this.setup = new ApplicationSetup();
		}
	}

}