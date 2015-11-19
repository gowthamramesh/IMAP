import java.awt.List;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
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

public class CICalculator
{

	private static JPanel							parentPanel;
	private static Thread							thread;
	private static HashMap<String, Float>			tmcSpdMap			= new HashMap<String, Float>();
	private static LinkedHashMap<String, Integer>	tmcOrderMap			= new LinkedHashMap<String, Integer>();
	private static HashMap<String, boolean[]>		singleDateMap		= new HashMap<String, boolean[]>();
	private static String							currDate			= "";
	private static String[]							crashRecord;
	private static String							line				= "";
	private static String							ciSeperator			= ",";
	private static String							speedLimitSeperator	= ",";
	private static String							crashDataSeperator	= ",";
	private static String							CIFolderName		= "CI_Data";
	private static Float							defSpdLmt			= (float) 60.0;
	private static float							alpha				= (float) 0.8;
	private static HashMap<String, String>			tmcLength;
	private static int								speedThreshold		= 45;

	public static void calculateCI(JPanel mainpanel)
	{
		ciSeperator = CrashClassifier.getConfig().getConfigValue("CI_SEPERATOR");
		speedLimitSeperator = CrashClassifier.getConfig().getConfigValue("SPEEDLIMIT_SEPERATOR");
		crashDataSeperator = CrashClassifier.getConfig().getConfigValue("CRASHRECORD_SEPERATOR");
		// tmcLength = TMCDistanceWriter.createDistanceFile();
		alpha = MainPanel.getAlphaValue();
		speedThreshold = MainPanel.getSpeedThreshold();
		parentPanel = mainpanel;
		thread = new Thread()
		{
			public void run()
			{
				try
				{
					createDirectory();
					readTMCSppedLimit();
					readCSV();
					JOptionPane.showMessageDialog(parentPanel, "Process complete. CI Data created. ");
				}
				catch (Exception v)
				{
					MainPanel.setMsg("Error while CI creation. Please check the File format");
					System.out.println(v);
				}
			}
		};

		thread.start();

	}

	protected static void createDirectory()
	{
		File theDir = new File(CIFolderName);
		// if the directory does not exist, create it
		if (!theDir.exists())
		{
			System.out.println("creating directory: " + CIFolderName);
			boolean result = false;

			try
			{
				theDir.mkdir();
				result = true;
			}
			catch (SecurityException se)
			{
				// handle it
			}
			if (result)
			{
				System.out.println("DIR created");
			}
		}
		else
		{
			for (File file : theDir.listFiles())
				file.delete();
		}
	}

	protected static void readTMCSppedLimit()
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
			line = br.readLine();
			while ((line = br.readLine()) != null)
			{
				// use comma as separator
				crashRecord = line.split(speedLimitSeperator);
				String tmc = crashRecord[0];
				String spdLimit = crashRecord[2];
				String order = crashRecord[3];
				tmcSpdMap.put(tmc, Float.parseFloat(spdLimit));
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

	protected static void readCSV()
	{

		JOptionPane.showMessageDialog(parentPanel, "Select the crash incident directory containing all speed data");
		JFileChooser filechooser = new JFileChooser(System.getProperty("user.dir"));
		filechooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		filechooser.setAcceptAllFileFilterUsed(false);
		filechooser.showOpenDialog(parentPanel);
		File selectedFile = filechooser.getSelectedFile();
		String directoryPath = selectedFile.getAbsolutePath();
		File[] filesInDirectory = new File(directoryPath).listFiles();
		boolean isAlphaEnabled = MainPanel.isAlphaSelection();

		for (File f : filesInDirectory)
		{
			String filePath = f.getAbsolutePath();
			String fileExtenstion = filePath.substring(filePath.lastIndexOf(".") + 1, filePath.length());
			String fileName = filePath.substring(filePath.lastIndexOf('\\') + 1, filePath.length());

			System.out.println(fileName);
			if ("csv".equals(fileExtenstion))
			{
				MainPanel.setMsg("Calculating CSV File   ::  " + fileName);

				String csvFile = filePath.trim();
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
					line = br.readLine();
					currLine++;

					Calendar calendar = Calendar.getInstance();
					Date currentDate = new Date();
					String tmc = null;
					int day = -1;
					int hours = -1;
					int minutes = -1;
					int minIndex = -1;
					float speed = -1;
					Float speedLmt = null;

					while ((line = br.readLine()) != null)
					{
						currLine++;
						MainPanel.incProgress(currLine);
						if (currLine % 1000 == 0)
							MainPanel.setMsg("Calculating CSV File   ::  " + fileName + "       " + currLine + "/" + lines);
						// use comma as separator
						crashRecord = line.split(crashDataSeperator);
						if (crashRecord[0].length() != 0 && crashRecord[1].length() != 0 && crashRecord[2].length() != 0)
						{
							currentDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(crashRecord[1]);
							if (currDate.length() != 0 && !currDate.equals(crashRecord[1].substring(0, 10)))
							{
								writeCurrData();
								singleDateMap.clear();
							}
							currDate = crashRecord[1].substring(0, 10);
							calendar.setTime(currentDate);
							tmc = crashRecord[0];
							day = calendar.get(Calendar.DAY_OF_WEEK);
							hours = calendar.get(Calendar.HOUR_OF_DAY);
							minutes = calendar.get(Calendar.MINUTE);
							minIndex = minutes / 15;
							minIndex = (hours * 4) + minIndex;

							// if (day != 1 && day != 7)
							{
								speed = Float.parseFloat(crashRecord[2]);
								speedLmt = tmcSpdMap.get(tmc);
								if (!singleDateMap.containsKey(tmc))
								{
									boolean[] array = new boolean[96];
									singleDateMap.put(tmc, array);
								}
								boolean[] bs = singleDateMap.get(tmc);
								if (speedLmt == null)
								{
									speedLmt = defSpdLmt;
								}
								float spdRatio = speed / speedLmt;
								boolean isCongestion = true;
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
									bs[minIndex] = true;
								}
								else
								{
									bs[minIndex] = false;
								}
							}
						}
					}
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
						}
						catch (IOException e)
						{
							e.printStackTrace();
						}
					}
				}

				System.gc();
			}
		}
	}

	private static void writeCurrData()
	{
		LinkedHashMap<String, Integer> sortByValues = sortByValues(tmcOrderMap);
		String date = currDate.substring(0, 10);
		File dateFile = new File(CIFolderName + "/" + date + ".csv");
		CsvFileWriter writer = new CsvFileWriter(dateFile.getAbsolutePath());
		for (String tmc : sortByValues.keySet())
		{
			StringBuffer buffer = new StringBuffer();
			buffer.append(tmc);
			buffer.append(ciSeperator);
			buffer.append("need to write tmc length");
			boolean[] CIData = singleDateMap.get(tmc);
			if (CIData == null)
			{
				continue;
			}
			for (int j = 0; j < 96; j++)
			{
				buffer.append(ciSeperator);
				if (CIData[j])
				{
					buffer.append("1");
				}
				else
				{
					buffer.append("0");
				}
			}
			singleDateMap.remove(tmc);
			writer.writeCsvFile(buffer.toString());
		}
		for (String tmc : singleDateMap.keySet())
		{
			StringBuffer buffer = new StringBuffer();
			buffer.append(tmc);
			buffer.append(ciSeperator);
			buffer.append("need to write tmc length");
			boolean[] CIData = singleDateMap.get(tmc);
			for (int j = 0; j < 96; j++)
			{
				buffer.append(ciSeperator);
				if (CIData[j])
				{
					buffer.append("1");
				}
				else
				{
					buffer.append("0");
				}
			}
			writer.writeCsvFile(buffer.toString());
		}
		writer.closeFile();
	}

	private static LinkedHashMap<String, Integer> sortByValues(HashMap map)
	{
		LinkedList list = new LinkedList(map.entrySet());
		// Defined Custom Comparator here
		Collections.sort(list, new Comparator()
		{
			public int compare(Object o1, Object o2)
			{
				return ((Comparable) ((Map.Entry) (o2)).getValue()).compareTo(((Map.Entry) (o1)).getValue());
			}
		});

		// Here I am copying the sorted list in HashMap
		// using LinkedHashMap to preserve the insertion order
		LinkedHashMap<String, Integer> sortedHashMap = new LinkedHashMap<String, Integer>();
		for (Iterator it = list.iterator(); it.hasNext();)
		{
			Map.Entry entry = (Map.Entry) it.next();
			sortedHashMap.put((String) entry.getKey(), (int) entry.getValue());
		}
		return sortedHashMap;
	}
}
