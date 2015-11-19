import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Toolkit;
import java.io.File;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.border.EtchedBorder;

public class CrashClassifier extends JFrame
{
	private static CrashClassifier	mainFrame;
	private static File				selectedExcel;
	private JPanel					basePanel;
	private JPanel					mainPanel;
	private JLabel					myLabel;
	private static ConfigReader		config	= new ConfigReader();

	public CrashClassifier() throws IOException
	{
		this.setTitle("Crash Classifier");
		initComponents();
	}

	private void initComponents() throws IOException
	{
		config.getPropValues();
		basePanel = new JPanel();
		basePanel.setLayout(new BoxLayout(basePanel, BoxLayout.Y_AXIS));
		myLabel = new JLabel();
		Font font = myLabel.getFont();
		myLabel.setText("Crash Classification Algorithm             ");
		// same font but bold
		Font boldFont = new Font(font.getFontName(), Font.BOLD, 20);
		myLabel.setFont(boldFont);
		mainPanel = MainPanel.getMainPanel();
		basePanel.add(myLabel);
		basePanel.add(mainPanel);
		basePanel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
		setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
		getContentPane().add(basePanel);
		setPreferredSize(new Dimension(800, 350));
		final Toolkit toolkit = Toolkit.getDefaultToolkit();
		final Dimension screenSize = toolkit.getScreenSize();
		setLocation(screenSize.width / 2 - 400, screenSize.height / 2 - 175);

		pack();

	}

	public static void main(String args[])
	{
		showMainForm();
	}

	public static ConfigReader getConfig()
	{
		return config;
	}

	private static void showMainForm()
	{
		// Create mainFrame in EDT thread
		EventQueue.invokeLater(new Runnable()
		{
			public void run()
			{
				try
				{
					mainFrame = new CrashClassifier();
					mainFrame.setVisible(true);
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}

		});
	}

	public static void setSelectedExcelFile(File selectedFile)
	{
		selectedExcel = selectedFile;

	}

	public static File getSelectedExcelFile()
	{
		return selectedExcel;

	}

}
