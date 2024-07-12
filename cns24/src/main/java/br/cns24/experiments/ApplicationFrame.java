/**
 * 
 */
package br.cns24.experiments;

import java.awt.Dimension;
import java.awt.GraphicsConfiguration;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.UIManager;

/**
 * @author Danilo
 * 
 */
public class ApplicationFrame extends JFrame {
	private static final long serialVersionUID = 1L;

	public ApplicationFrame() {
		this("");
	}

	public ApplicationFrame(String title) {
		super(title);
		createUI();
	}

	public ApplicationFrame(String title, int width, int height) {
		super(title);
		createUI(width, height);
	}

	protected void createUI() {
		GraphicsConfiguration config = getGraphicsConfiguration();

		int left = Toolkit.getDefaultToolkit().getScreenInsets(config).left;
		int right = Toolkit.getDefaultToolkit().getScreenInsets(config).right;
		int top = Toolkit.getDefaultToolkit().getScreenInsets(config).top;
		int bottom = Toolkit.getDefaultToolkit().getScreenInsets(config).bottom;

		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		int width = screenSize.width - left - right;
		int height = screenSize.height - top - bottom;

		setResizable(false);
		setPreferredSize(new Dimension(width, height));
		setSize(width, height);
		cornerLeftTop();
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				dispose();
				System.exit(0);
			}
		});
		setNativeLookAndFeel();
	}

	protected void createUI(int width, int height) {
		setResizable(false);
		setPreferredSize(new Dimension(width, height));
		setSize(width, height);
		cornerLeftTop();
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				dispose();
				System.exit(0);
			}
		});
		setNativeLookAndFeel();
	}

	public void center() {
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		Dimension frameSize = getSize();
		int x = (screenSize.width - frameSize.width) / 2;
		int y = (screenSize.height - frameSize.height) / 2;
		setLocation(x, y);
	}

	public void cornerLeftTop() {
		setLocation(10, 10);
	}

	public static void setNativeLookAndFeel() {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			setJavaLookAndFeel();
			System.out.println("Error setting native LAF: " + e);
		}
	}

	public static void setJavaLookAndFeel() {
		try {
			UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
		} catch (Exception e) {
			System.out.println("Error setting Java LAF: " + e);
		}
	}

	public static void setMotifLookAndFeel() {
		try {
			UIManager.setLookAndFeel("com.sun.java.swing.plaf.motif.MotifLookAndFeel");
		} catch (Exception e) {
			System.out.println("Error setting Motif LAF: " + e);
		}
	}
}