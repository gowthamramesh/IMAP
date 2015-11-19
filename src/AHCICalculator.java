import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

public class AHCICalculator
{
	private static String							speedLimitSeperator	= ",";
	private static String							crashDataSeperator	= ",";
	private static String							commaDelimt			= ",";
	private static float							alpha				= (float) 0.8;
	private static float							defSpdLmt			= (float) 60.0;
	private static JPanel							parentPanel;
	private static Thread							thread;
	private static String							dateTimeFormat		= "yyyy-MM-dd HH:mm:ss";
	private static int								speedThreshold		= 45;
	private static LinkedHashMap<String, Integer>	tmcOrderMap			= new LinkedHashMap<String, Integer>();

	public static void createAHCI(JPanel mainPanel) throws FileNotFoundException, IOException
	{
		speedLimitSeperator = CrashClassifier.getConfig().getConfigValue("SPEEDLIMIT_SEPERATOR");
		crashDataSeperator = CrashClassifier.getConfig().getConfigValue("CRASHRECORD_SEPERATOR");
		alpha = MainPanel.getAlphaValue();
		speedThreshold = MainPanel.getSpeedThreshold();
		parentPanel = mainPanel;
		thread = new Thread()
		{
			public void run()
			{
				try
				{
					readTMCSppedLimit();
					readCSV();
					JOptionPane.showMessageDialog(parentPanel, "AHCI_Output.csv created. Process complete");
				}
				catch (IOException v)
				{
					System.out.println(v);
				}
			}
		};
		thread.start();

	}

	private static void readTMCSppedLimit() throws FileNotFoundException, IOException
	{
		JOptionPane.showMessageDialog(parentPanel, "Select the speedlimit TMC file");
		JFileChooser filechooser = new JFileChooser(System.getProperty("user.dir"));
		filechooser.showOpenDialog(parentPanel);
		File selectedFile = filechooser.getSelectedFile();
		String csvFile = selectedFile.getAbsolutePath();
		BufferedReader br = null;
		try
		{
			br = new BufferedReader(new FileReader(csvFile));
			String line = br.readLine();
			while ((line = br.readLine()) != null)
			{
				String[] crashRecord;
				// use comma as separator
				crashRecord = line.split(speedLimitSeperator);
				String tmc = crashRecord[0];
				String spdLimit = crashRecord[2];
				String order = crashRecord[3];
				CrashDataMap.addTMCMap(tmc, Float.parseFloat(spdLimit));
				tmcOrderMap.put(tmc, Integer.parseInt(order));
			}
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		catch (NumberFormatException e)
		{
			System.out.println(e);
		}
		finally
		{
			if (br != null)
			{
				try
				{
					br.close();
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
			}
		}
	}

	public static void readCSV()
	{
		HashMap<String, Float> tmcMap = CrashDataMap.getTMCMap();
		// ArrayList<String> tmcInOutput = new ArrayList<String>();
		ArrayList<String> tmcValues = new ArrayList<String>(tmcMap.keySet());
		int[][] CIArray = new int[tmcValues.size() * 2][100];
		int[][] CICounter = new int[tmcValues.size() * 2][100];
		SimpleDateFormat sdf = new SimpleDateFormat(dateTimeFormat);

		JOptionPane.showMessageDialog(parentPanel, "Select the crash incident directory containing all speed data");
		JFileChooser filechooser = new JFileChooser(System.getProperty("user.dir"));
		filechooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		filechooser.setAcceptAllFileFilterUsed(false);
		filechooser.showOpenDialog(parentPanel);
		File selectedFile = filechooser.getSelectedFile();
		String directoryPath = selectedFile.getAbsolutePath();
		File[] filesInDirectory = new File(directoryPath).listFiles();
		String filePath = "";
		String fileExtenstion = "";
		String fileName = "";
		String csvFile = "";
		boolean isAlphaEnabled = MainPanel.isAlphaSelection();
		for (File f : filesInDirectory)
		{
			filePath = f.getAbsolutePath();
			fileExtenstion = filePath.substring(filePath.lastIndexOf(".") + 1, filePath.length());
			fileName = filePath.substring(filePath.lastIndexOf('\\') + 1, filePath.length());

			System.out.println(fileName);
			if ("csv".equals(fileExtenstion))
			{
				MainPanel.setMsg("Calculating CSV File   ::  " + fileName);
			}
			csvFile = filePath.trim();
			BufferedReader br = null;

			try
			{
				int lines = 0;
				int currLine = 0;
				LineNumberReader lineNumberReader = new LineNumberReader(new FileReader(csvFile));
				lineNumberReader.skip(Long.MAX_VALUE);
				lines = lineNumberReader.getLineNumber();
				MainPanel.setProgress(lines);
				br = new BufferedReader(new FileReader(csvFile));
				String line = br.readLine();
				currLine++;

				while ((line = br.readLine()) != null)
				{
					currLine++;
					MainPanel.incProgress(currLine);
					// use comma as separator
					String[] crashRecord = line.split(crashDataSeperator);
					if (crashRecord[0].length() != 0 && crashRecord[1].length() != 0 && crashRecord[2].length() != 0)
					{
						int day = -1;
						int hours = -1;
						int minutes = -1;
						int minIndex = -1;
						float speed = -1;
						Float speedLmt = null;
						Calendar calendar = Calendar.getInstance();

						calendar.setTime(sdf.parse(crashRecord[1]));
						if (tmcValues.indexOf(crashRecord[0]) == -1)
						{
							CrashDataMap.addTMCMap(crashRecord[0], defSpdLmt);
							tmcValues.add(crashRecord[0]);
						}
						// tmcInOutput.add(crashRecord[0]);
						day = calendar.get(Calendar.DAY_OF_WEEK);
						hours = calendar.get(Calendar.HOUR_OF_DAY);
						minutes = calendar.get(Calendar.MINUTE);
						minIndex = minutes / 15;
						minIndex = (hours * 4) + minIndex;

						if (day != 6 && day != 7 && tmcValues.indexOf(crashRecord[0]) != -1)
						{
							speed = Float.parseFloat(crashRecord[2]);
							speedLmt = tmcMap.get(crashRecord[0]);
							boolean isCongestion = true;
							float spdRatio = speed / speedLmt;
							if (isAlphaEnabled)
							{
								if (spdRatio < alpha)
								{
									isCongestion = true;
								}
								else
								{
									isCongestion = false;
								}
							}
							else
							{
								if (speed < speedThreshold)
								{
									isCongestion = true;
								}
								else
								{
									isCongestion = false;
								}
							}

							if (speedLmt != null && isCongestion)
							{
								CIArray[tmcValues.indexOf(crashRecord[0])][minIndex] += 1;
							}
							CICounter[tmcValues.indexOf(crashRecord[0])][minIndex] += 1;
						}
						calendar = null;
					}
					crashRecord = null;
					line = null;
					if (currLine % 100000 == 0)
					{
						System.gc();
					}
				}
				System.gc();

			}
			catch (ParseException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			catch (FileNotFoundException e)
			{
				e.printStackTrace();
			}
			catch (IOException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			finally
			{
				if (br != null)
				{
					try
					{
						br.close();
						br = null;
					}
					catch (IOException e)
					{
						e.printStackTrace();
					}
				}
			}
			break;
		}
		String timeStamp = String.valueOf(System.currentTimeMillis());
		CsvFileWriter writer = new CsvFileWriter("AHCI_Output_" + timeStamp + ".csv");
		MainPanel.setMsg("AHCI OUTPUT SAVED AS    ::  " + "AHCI_Output_" + timeStamp + ".csv");
		int[] congestionTime = new int[2];
		for (int i = 0; i < tmcValues.size(); i++)
		{
			boolean flag = false;
			String congTime = "";
			StringBuffer buffer = new StringBuffer();
			buffer.append(Integer.toString(i));
			buffer.append(commaDelimt);
			buffer.append(tmcValues.get(i));
			for (int j = 0; j < 96; j++)
			{
				buffer.append(commaDelimt);
				float ahciVal = 0;
				if (CIArray[i][j] != 0 && CICounter[i][j] != 0)
				{
					ahciVal = (float) CIArray[i][j] / (float) CICounter[i][j];
				}
				else
				{
					ahciVal = 0;
				}
				buffer.append(Float.toString(ahciVal));
				if (flag == false && ahciVal > 0.2)
				{
					congestionTime[0] = j;
					int min = ((congestionTime[0]) % 4);
					int hour = (congestionTime[0]) / 4;
					congTime = hour + ":" + min * 15 + " - ";
					flag = true;
				}
				if (flag == true && ahciVal < 0.2)
				{
					congestionTime[1] = j;
					if (congestionTime[0] == 0)
					{
						congTime = ":";
					}
					int min = ((congestionTime[1] - 1) % 4);
					int hour = (congestionTime[1] - 1) / 4;
					if (congestionTime[1] == 0)
					{
						congTime = ":";
					}
					else
					{
						congTime = congTime + hour + ":" + min * 15 + "/";
					}
					flag = false;
				}
			}
			buffer.append(commaDelimt);
			buffer.append(congTime);

			writer.writeCsvFile(buffer.toString());
		}
		writer.closeFile();

	}

	public class HelloRunnable implements Runnable
	{

		public void run()
		{
			System.out.println("Hello from a thread!");
		}

	}
}
