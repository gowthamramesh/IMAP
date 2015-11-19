import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JRadioButton;

public class MainPanel
{
	private static JPanel					mainpanel;
	private static javax.swing.JButton		calculateCI;
	private static javax.swing.JButton		calculateAhci;
	private static javax.swing.JButton		classifyType;
	private static javax.swing.JButton		plotGraph;
	private static javax.swing.JLabel		alphaValue;
	private static javax.swing.JLabel		SpeedThresholdLabel;
	private static javax.swing.JLabel		betaValue;
	private static javax.swing.JLabel		etaValue;
	private static javax.swing.JLabel		gammaValue;
	private static javax.swing.JLabel		msgLabel;
	private static javax.swing.JTextField	alphaField;
	private static javax.swing.JTextField	spdThreshField;
	private static javax.swing.JTextField	betaField;
	private static javax.swing.JTextField	etaField;
	private static javax.swing.JTextField	gammaField;
	private static JProgressBar				progressBar;

	public static JPanel getMainPanel()
	{
		if (mainpanel != null)
		{
			return mainpanel;
		}

		mainpanel = new JPanel();
		initComp();
		return mainpanel;
	}

	private static void initComp()
	{
		mainpanel.setPreferredSize(new Dimension(500, 200));
		mainpanel.setLayout(new BoxLayout(mainpanel, BoxLayout.Y_AXIS));
		mainpanel.add(getButtonPanel());
	}

	private static JPanel getButtonPanel()
	{
		JPanel allButtonPanel = new JPanel();
		JPanel plotPanel = new JPanel();
		plotPanel.setLayout(new BoxLayout(plotPanel, BoxLayout.X_AXIS));

		plotPanel.setBorder(BorderFactory.createTitledBorder("Setup information"));
		allButtonPanel.setLayout(new BoxLayout(allButtonPanel, BoxLayout.Y_AXIS));

		JPanel selectFilePanel = new JPanel();
		selectFilePanel.setLayout(new BoxLayout(selectFilePanel, BoxLayout.Y_AXIS));
		selectFilePanel.setMaximumSize(new Dimension(200, 300));

		/**
		 * CI classification button panel addition
		 */
		calculateCI = new javax.swing.JButton();
		calculateCI.setText("Calculate CI");
		calculateCI.setMinimumSize(new Dimension(175, 45));
		calculateCI.setMaximumSize(new Dimension(175, 45));
		calculateCI.addActionListener(new ActionListener()
		{

			@Override
			public void actionPerformed(ActionEvent e)
			{
				try
				{
					CICalculator.calculateCI(mainpanel);
				}
				catch (Exception e1)
				{
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		});
		selectFilePanel.add(Box.createVerticalStrut(10));
		selectFilePanel.add(calculateCI);

		calculateAhci = new javax.swing.JButton();
		calculateAhci.setText("Calculate AHCI");
		calculateAhci.setMinimumSize(new Dimension(175, 45));
		calculateAhci.setMaximumSize(new Dimension(175, 45));
		calculateAhci.addActionListener(new ActionListener()
		{

			@Override
			public void actionPerformed(ActionEvent e)
			{
				try
				{
					AHCICalculator.createAHCI(mainpanel);
				}
				catch (IOException e1)
				{
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		});
		selectFilePanel.add(Box.createVerticalStrut(10));
		selectFilePanel.add(calculateAhci);

		classifyType = new javax.swing.JButton();
		classifyType.setText("Classify crash");
		classifyType.setMinimumSize(new Dimension(175, 45));
		classifyType.setMaximumSize(new Dimension(175, 45));
		classifyType.addActionListener(new ActionListener()
		{

			@Override
			public void actionPerformed(ActionEvent e)
			{
				// CICalculator.calculateCI(mainpanel);
				int beta = Integer.parseInt(betaField.getText());
				double gamma = Double.parseDouble(gammaField.getText());
				double eta = Double.parseDouble(etaField.getText());
				File folder = new File("CI_Data");
				File[] listOfFiles = folder.listFiles();
				if (listOfFiles == null || listOfFiles.length == 0)
				{
					JOptionPane.showMessageDialog(mainpanel, "CI Data unavailable. Please create CI Data first", "CI Unavailable",
							JOptionPane.ERROR_MESSAGE, null);
					return;
				}
				ClassifyCrashData.classifyCrash(mainpanel, gamma, beta, eta);
			}
		});
		selectFilePanel.add(Box.createVerticalStrut(10));
		selectFilePanel.add(classifyType);

		final JRadioButton alphaButton = new JRadioButton("Alpha value", true);
		final JRadioButton threshButton = new JRadioButton("Threshold Value");
		ButtonGroup group = new ButtonGroup();
		group.add(alphaButton);
		group.add(threshButton);

		JPanel radioPanel = new JPanel();
		radioPanel.setLayout(new BoxLayout(radioPanel, BoxLayout.X_AXIS));
		radioPanel.setMaximumSize(new Dimension(500, 25));
		radioPanel.add(Box.createHorizontalStrut(10));
		radioPanel.add(alphaButton);
		radioPanel.add(Box.createHorizontalStrut(70));
		radioPanel.add(threshButton);

		alphaButton.addItemListener(new ItemListener()
		{
			public void itemStateChanged(ItemEvent e)
			{
				if (alphaButton.isSelected())
				{
					spdThreshField.setEnabled(false);
					alphaField.setEnabled(true);
				}
			}
		});

		threshButton.addItemListener(new ItemListener()
		{
			public void itemStateChanged(ItemEvent e)
			{
				if (threshButton.isSelected())
				{
					alphaField.setEnabled(false);
					spdThreshField.setEnabled(true);
				}
			}
		});

		JPanel calibValalphaPanel = new JPanel();
		calibValalphaPanel.setLayout(new BoxLayout(calibValalphaPanel, BoxLayout.X_AXIS));
		calibValalphaPanel.setMaximumSize(new Dimension(500, 25));

		alphaValue = new javax.swing.JLabel();
		alphaField = new javax.swing.JTextField();
		alphaField.setMaximumSize(new Dimension(200, 40));
		calibValalphaPanel.add(Box.createHorizontalStrut(10));
		calibValalphaPanel.add(alphaValue);
		calibValalphaPanel.add(Box.createHorizontalStrut(70));
		calibValalphaPanel.add(alphaField);

		JPanel spdThreshPanel = new JPanel();
		spdThreshPanel.setLayout(new BoxLayout(spdThreshPanel, BoxLayout.X_AXIS));
		spdThreshPanel.setMaximumSize(new Dimension(500, 25));

		SpeedThresholdLabel = new javax.swing.JLabel();
		spdThreshField = new javax.swing.JTextField();
		spdThreshField.setMaximumSize(new Dimension(200, 40));
		spdThreshField.setEnabled(false);
		spdThreshPanel.add(Box.createHorizontalStrut(10));
		spdThreshPanel.add(SpeedThresholdLabel);
		spdThreshPanel.add(Box.createHorizontalStrut(70));
		spdThreshPanel.add(spdThreshField);

		JPanel calibValbetaPanel = new JPanel();
		calibValbetaPanel.setLayout(new BoxLayout(calibValbetaPanel, BoxLayout.X_AXIS));
		calibValbetaPanel.setMaximumSize(new Dimension(500, 25));
		betaValue = new javax.swing.JLabel();
		betaField = new javax.swing.JTextField();
		betaField.setMaximumSize(new Dimension(200, 40));
		calibValbetaPanel.add(Box.createHorizontalStrut(10));
		calibValbetaPanel.add(betaValue);
		calibValbetaPanel.add(Box.createHorizontalStrut(70));
		calibValbetaPanel.add(betaField);

		JPanel calibValgammaPanel = new JPanel();
		calibValgammaPanel.setLayout(new BoxLayout(calibValgammaPanel, BoxLayout.X_AXIS));
		calibValgammaPanel.setMaximumSize(new Dimension(500, 25));
		gammaValue = new javax.swing.JLabel();
		gammaField = new javax.swing.JTextField();
		gammaField.setMaximumSize(new Dimension(200, 40));
		calibValgammaPanel.add(Box.createHorizontalStrut(10));
		calibValgammaPanel.add(gammaValue);
		calibValgammaPanel.add(Box.createHorizontalStrut(70));
		calibValgammaPanel.add(gammaField);

		JPanel calibValetaPanel = new JPanel();
		calibValetaPanel.setLayout(new BoxLayout(calibValetaPanel, BoxLayout.X_AXIS));
		calibValetaPanel.setMaximumSize(new Dimension(500, 25));
		etaValue = new javax.swing.JLabel();
		etaField = new javax.swing.JTextField();
		etaField.setMaximumSize(new Dimension(200, 40));
		calibValetaPanel.add(Box.createHorizontalStrut(10));
		calibValetaPanel.add(etaValue);
		calibValetaPanel.add(Box.createHorizontalStrut(70));
		calibValetaPanel.add(etaField);

		JPanel SpeedPanel = new JPanel();
		SpeedPanel.setLayout(new BoxLayout(SpeedPanel, BoxLayout.X_AXIS));
		SpeedPanel.setMaximumSize(new Dimension(500, 25));
		msgLabel = new JLabel("Progress");
		SpeedPanel.add(Box.createHorizontalStrut(10));
		SpeedPanel.add(msgLabel);

		JPanel progressPanel = new JPanel();
		progressPanel.setLayout(new BoxLayout(progressPanel, BoxLayout.X_AXIS));
		progressPanel.setMaximumSize(new Dimension(500, 25));
		progressPanel.add(Box.createHorizontalStrut(10));
		progressBar = new JProgressBar(0, 100);
		progressBar.setValue(0);
		progressBar.setStringPainted(true);
		progressPanel.add(progressBar);

		plotGraph = new JButton();

		alphaValue.setText("Alpha Value (0-1)     ");
		SpeedThresholdLabel.setText("Speed Threshold     ");
		betaValue.setText("Beta Value                 ");
		gammaValue.setText("Gamma Value (0-1) ");
		etaValue.setText("Eta Value (0-1)          ");

		alphaField.setText("0.8");
		betaField.setText("2");
		gammaField.setText("0.6");
		etaField.setText("0.2");
		spdThreshField.setText("45");

		plotGraph.setText("Plot I2D Graph");

		allButtonPanel.add(Box.createVerticalStrut(10));
		allButtonPanel.add(radioPanel);
		allButtonPanel.add(Box.createVerticalStrut(10));
		allButtonPanel.add(calibValalphaPanel);
		allButtonPanel.add(Box.createVerticalStrut(10));
		allButtonPanel.add(spdThreshPanel);
		allButtonPanel.add(Box.createVerticalStrut(10));
		allButtonPanel.add(calibValbetaPanel);
		allButtonPanel.add(Box.createVerticalStrut(10));
		allButtonPanel.add(calibValgammaPanel);
		allButtonPanel.add(Box.createVerticalStrut(10));
		allButtonPanel.add(calibValetaPanel);
		allButtonPanel.add(Box.createVerticalStrut(10));
		allButtonPanel.add(SpeedPanel);
		allButtonPanel.add(Box.createVerticalStrut(10));
		allButtonPanel.add(progressPanel);
		allButtonPanel.add(Box.createVerticalStrut(10));
		plotPanel.add(allButtonPanel);
		plotPanel.add(selectFilePanel);

		return plotPanel;
	}

	public static float getAlphaValue()
	{
		return Float.parseFloat(alphaField.getText());
	}

	public static void setMsg(String msg)
	{
		msgLabel.setText(msg);
		mainpanel.revalidate();
		mainpanel.repaint();
	}

	public static void setProgress(int value)
	{
		progressBar.setMaximum(value);
		progressBar.setValue(0);
	}

	public static void incProgress(int value)
	{
		progressBar.setValue(value);
	}

	public static boolean isAlphaSelection()
	{
		if (alphaField.isEnabled())
		{
			return true;
		}
		return false;
	}

	public static int getSpeedThreshold()
	{
		return Integer.parseInt(spdThreshField.getText());
	}
}
