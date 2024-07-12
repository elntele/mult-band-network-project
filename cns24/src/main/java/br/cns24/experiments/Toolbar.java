package br.cns24.experiments;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.event.ActionListener;
import java.text.NumberFormat;

import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JToolBar;

import br.cns24.experiments.setup.ApplicationSetup;
import br.cns24.models.TModel;

public class Toolbar extends JToolBar {
	private static final long serialVersionUID = 1L;

	private JComboBox<TModel> modelsList;

	private JFormattedTextField txtNodes;

	public Toolbar(ActionListener al, ApplicationSetup setup) {
		Insets margins = new Insets(0, 0, 0, 0);
		setLayout(new FlowLayout(FlowLayout.LEFT));

		ToolBarButton button = new ToolBarButton(new ImageIcon(
				Util.loadImage("images/" + "document24.png")));
		button.setToolTipText(MenuActions.NEW.getDescription());
		button.setActionCommand(MenuActions.NEW.toString());
		button.setMargin(margins);
		button.addActionListener(al);
		add(button);

		button = new ToolBarButton(new ImageIcon(Util.loadImage("images/"
				+ "folder24.png")));
		button.setToolTipText(MenuActions.OPEN.getDescription());
		button.setActionCommand(MenuActions.OPEN.toString());
		button.setMargin(margins);
		button.addActionListener(al);
		add(button);

		button = new ToolBarButton(new ImageIcon(Util.loadImage("images/"
				+ "save24.png")));
		button.setToolTipText(MenuActions.SAVE.getDescription());
		button.setActionCommand(MenuActions.SAVE.toString());
		button.setMargin(margins);
		button.addActionListener(al);
		add(button);

		addSeparator();

		button = new ToolBarButton(new ImageIcon(Util.loadImage("images/"
				+ "gear_wheel24.png")));
		button.setToolTipText(MenuActions.CONFIG.getDescription());
		button.setActionCommand(MenuActions.CONFIG.toString());
		button.setMargin(margins);
		button.addActionListener(al);
		add(button);

		addSeparator();

		button = new ToolBarButton(new ImageIcon(Util.loadImage("images/"
				+ "print24.png")));
		button.setToolTipText(MenuActions.PRINT.getDescription());
		button.setActionCommand(MenuActions.PRINT.toString());
		button.setMargin(margins);
		button.addActionListener(al);
		add(button);

		button = new ToolBarButton(new ImageIcon(Util.loadImage("images/"
				+ "picture24.png")));
		button.setToolTipText(MenuActions.EXPORT.getDescription());
		button.setActionCommand(MenuActions.EXPORT.toString());
		button.setMargin(margins);
		button.addActionListener(al);
		add(button);

		button = new ToolBarButton(new ImageIcon(Util.loadImage("images/"
				+ "text24.png")));
		button.setToolTipText(MenuActions.EXPORT_TABLE_TXT.getDescription());
		button.setActionCommand(MenuActions.EXPORT_TABLE_TXT.toString());
		button.setMargin(margins);
		button.addActionListener(al);
		add(button);

		button = new ToolBarButton(new ImageIcon(Util.loadImage("images/"
				+ "workflow24.png")));
		button.setToolTipText(MenuActions.EXPORT_TABLE_XML.getDescription());
		button.setActionCommand(MenuActions.EXPORT_TABLE_XML.toString());
		button.setMargin(margins);
		button.addActionListener(al);
		add(button);

		addSeparator();

		modelsList = new JComboBox<TModel>(TModel.values());
		modelsList.setPreferredSize(new Dimension(120, 24));
		modelsList.setSelectedItem(setup.getDefaultModel());
		add(new JLabel(" Modelo:"));
		add(modelsList);

		txtNodes = new JFormattedTextField(NumberFormat.getNumberInstance());
		txtNodes.setText(String.valueOf(setup.getDefaultNumNodes()));
		txtNodes.setAlignmentX(RIGHT_ALIGNMENT);
		txtNodes.setPreferredSize(new Dimension(50, 24));
		add(new JLabel(" Número de nós:"));
		add(txtNodes);

		button = new ToolBarButton(new ImageIcon(Util.loadImage("images/"
				+ "magic_wand24.png")));
		button.setToolTipText(MenuActions.NEW_SIMULATION.getDescription());
		button.setActionCommand(MenuActions.NEW_SIMULATION.toString());
		button.setMargin(margins);
		button.addActionListener(al);
		add(button);

		button = new ToolBarButton(new ImageIcon(Util.loadImage("images/"
				+ "som.png")));
		button.setToolTipText(MenuActions.SOM_EXPERIMENT.getDescription());
		button.setActionCommand(MenuActions.SOM_EXPERIMENT.toString());
		button.setMargin(margins);
		button.addActionListener(al);
		add(button);

		addSeparator();

		button = new ToolBarButton(new ImageIcon(Util.loadImage("images/"
				+ "info24.png")));
		button.setToolTipText(MenuActions.ABOUT.getDescription());
		button.setActionCommand(MenuActions.ABOUT.toString());
		button.setMargin(margins);
		button.addActionListener(al);
		add(button);

		button = new ToolBarButton(new ImageIcon(Util.loadImage("images/"
				+ "exit.png")));
		button.setToolTipText(MenuActions.EXIT.getDescription());
		button.setActionCommand(MenuActions.EXIT.toString());
		button.setMargin(margins);
		button.addActionListener(al);
		add(button);

		addSeparator();
	}

	public void setTextLabels(boolean labelsAreEnabled) {
		Component c;
		int i = 0;
		while ((c = getComponentAtIndex(i++)) != null) {
			ToolBarButton button = (ToolBarButton) c;
			if (labelsAreEnabled) {
				button.setText(button.getToolTipText());
			} else {
				button.setText(null);
			}
		}
	}

	public JComboBox<TModel> getModelsList() {
		return modelsList;
	}

	public JFormattedTextField getTxtNodes() {
		return txtNodes;
	}
}
