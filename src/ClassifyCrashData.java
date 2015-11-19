import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;

import javax.print.attribute.standard.JobMessageFromOperator;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

public class ClassifyCrashData
{
	private static LinkedHashMap<String, double[]>	ahciVals		= new LinkedHashMap<String, double[]>();
	private static String							ahciSeperator	= ",";
	private static String							ciSeperator		= ",";
	private static String							teasSeperator	= ",";
	private static String							semiColon		= ";";
	private static String[]							crashRecord;
	private static String							line			= "";
	private static double[][]						ahciArray;
	private static ArrayList<String>				tmcList			= new ArrayList<String>();
	private static JPanel							mainPanel;
	private static Thread							thread;
	private static SimpleDateFormat					sdf				= new SimpleDateFormat("yyyy-MM-dd");
	private static double							gamma			= 0.6;
	private static int								beta			= 2;
	private static double							eta				= 0.2;

	public static void classifyCrash(JPanel mainpanel, double gammaVal, int betaVal, double etaVal)
	{
		ciSeperator = CrashClassifier.getConfig().getConfigValue("CI_SEPERATOR");
		ahciSeperator = CrashClassifier.getConfig().getConfigValue("AHCI_SEPERATOR");
		teasSeperator = CrashClassifier.getConfig().getConfigValue("TEAS_SEPERATOR");
		mainPanel = mainpanel;
		beta = betaVal;
		gamma = gammaVal;
		eta = etaVal;
		thread = new Thread()
		{
			public void run()
			{
				readAHCIFile();
				createAHCIArray();
				classifyData();
			}
		};

		thread.start();

	}

	private static void createAHCIArray()
	{
		int size = ahciVals.size();
		ahciArray = new double[size][97];
		int i = 0;
		for (String key : ahciVals.keySet())
		{
			// ...
			double[] ds = ahciVals.get(key);
			for (int j = 1; j < 97; j++)
			{
				ahciArray[i][j - 1] = ds[j];
			}
			i++;
		}
	}

	private static void readAHCIFile()
	{
		JOptionPane.showMessageDialog(mainPanel, "Select the ordered AHCI file");
		JFileChooser filechooser = new JFileChooser(System.getProperty("user.dir"));
		filechooser.showOpenDialog(mainPanel);
		File selectedFile = filechooser.getSelectedFile();
		String csvFile = selectedFile.getAbsolutePath();
		String tmc = "";
		BufferedReader br = null;
		try
		{
			br = new BufferedReader(new FileReader(csvFile));
			line = br.readLine();
			while ((line = br.readLine()) != null)
			{
				try
				{
					double[] ahci = new double[97];
					// use comma as separator
					crashRecord = line.split(ahciSeperator);
					tmc = crashRecord[1];
					ahci[0] = Double.parseDouble(crashRecord[6]);
					for (int i = 0; i < 96; i++)
					{
						ahci[i + 1] = Double.parseDouble(crashRecord[i + 8]);
					}
					tmcList.add(tmc);
					ahciVals.put(tmc, ahci);
				}
				catch (NumberFormatException e)
				{
					System.out.println("Number format exception");
				}
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
	}

	private static void classifyData()
	{
		SimpleDateFormat df = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss aa");
		JOptionPane.showMessageDialog(mainPanel, "Select the TEAS crashData File");
		JFileChooser filechooser = new JFileChooser(System.getProperty("user.dir"));
		filechooser.showOpenDialog(mainPanel);
		File selectedFile = filechooser.getSelectedFile();
		String csvFile = selectedFile.getAbsolutePath();

		String tmc = "";
		BufferedReader br = null;
		int counter = 0;
		String lineToWrite = "";
		try
		{
			br = new BufferedReader(new FileReader(csvFile));
			lineToWrite = br.readLine();
			lineToWrite = lineToWrite + semiColon + "CRASH TYPE" + semiColon + "TIMELAG" + semiColon + "DISTANCE";
			CsvFileWriter.setFileHeader(lineToWrite);
			String timeStamp = String.valueOf(System.currentTimeMillis());
			CsvFileWriter writer = new CsvFileWriter("ResultFile" + timeStamp + ".csv");
			while ((lineToWrite = br.readLine()) != null)
			{
				try
				{
					// use comma as separator
					crashRecord = lineToWrite.split(teasSeperator);
					tmc = crashRecord[7];
					int type = -1;
					int[] congestionTime = new int[2];
					int timeLag = -1;
					float distance = 0;
					float spdLimit = Float.parseFloat(crashRecord[6]);
					String cdate = crashRecord[1];
					String ctime = crashRecord[4];
					Date crashTime = df.parse(cdate + " " + ctime);
					Calendar calendar = Calendar.getInstance();
					calendar.setTime(crashTime);
					int day = calendar.get(Calendar.DAY_OF_WEEK);
					// float speed = getSpeed(tmc, crashTime);
					if (day != 1 && day != 7)
					{
						int CI = getCI(tmc, crashTime);
						if (CI == -1)
						{
							type = 99;
						}
						else
						{

							type = getCrashType(tmc, crashTime, CI, spdLimit, congestionTime);
							if (type == 2)
							{
								// timeLag = getTimeLag(tmc, crashTime, speed,
								// spdLimit);
								// distance = getDistance(tmc, crashTime, speed,
								// spdLimit);
							}
						}
					}
					else
					{
						type = 99;
					}

					lineToWrite = lineToWrite + semiColon + type + semiColon + timeLag + semiColon + distance + semiColon;
					writer.writeCsvFile(lineToWrite);
					System.out.println(counter);
					MainPanel.setMsg("Total records classified = " + counter);
					counter++;
				}
				catch (NumberFormatException | ParseException e)
				{
					MainPanel.setMsg("Line trying to read : " + lineToWrite);
					System.out.println("Number format exception");
				}
			}
			writer.closeFile();
			JOptionPane.showMessageDialog(mainPanel, "ResultFile.csv created. Process complete");
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
	}

	private static int getCrashType(String tmc, Date crashTime, int CI, float spdLimit, int[] congestionTime)
	{
		if (CI == 0)
		{
			return 1;
		}
		int hours = crashTime.getHours();
		int minutes = crashTime.getMinutes();
		int minIndex = minutes / 15;
		minIndex = (hours * 4) + minIndex;
		minIndex++;
		double[] ds = ahciVals.get(tmc);
		if (ds != null)
		{
			for (int i = minIndex; i > 0; i--)
			{
				if (ds[i] > 0.2)
				{
					congestionTime[0] = i;
				}
				else
				{
					i = -1;
				}
			}
			for (int i = minIndex; i <= 96; i++)
			{
				if (ds[i] > 0.2)
				{
					congestionTime[1] = i;
				}
				else
				{
					i = 96;
				}
			}
			if (ds[minIndex] >= gamma) // gamma
			{
				return 3;
			}
			else if (ds[minIndex] < eta) // eta?
			{
				return 2;
			}
			else
			{
				int i = tmcList.indexOf(tmc);
				if (i > 1)
				{
					if (isRecurringBottleneckArea(ds, minIndex, i))
					{
						return 3;
					}
					else
					{
						int curr = minIndex;
						int prev = curr - 1;
						while (prev != 0 && ds[curr] < ds[prev])
						{
							curr--;
							prev--;
						}
						if (isRecurringBottleneckArea(ds, curr, prev))
						{
							return 3;
						}
						else
						{
							return 2;
						}
					}
				}
			}
		}
		return 0;
	}

	private static boolean isRecurringBottleneckArea(double[] ds, int minIndex, int i)
	{
		double[] y1Tmc = ahciVals.get(tmcList.get(i - 1));
		double[] y2Tmc = ahciVals.get(tmcList.get(i - 2));
		double y1 = ds[minIndex] - beta * y1Tmc[minIndex]; // 2 = beta
		double y2 = ds[minIndex] - beta * y2Tmc[minIndex];
		if (ds[minIndex] >= 0.5 && (y1 > 0 || y2 > 0))
		{
			return true;
		}
		return false;
	}

	private static int getCI(String tmc, Date crashTime)
	{
		int CI = 0;
		String csvFile = "CI_Data/" + sdf.format(crashTime) + ".csv";
		BufferedReader br = null;
		String csvLine = "";
		String crashRec[];
		try
		{
			File file = new File(csvFile);
			if (!file.exists())
			{
				return -1;
			}
			br = new BufferedReader(new FileReader(csvFile));
			csvLine = br.readLine();
			while ((csvLine = br.readLine()) != null)
			{
				// use comma as separator
				crashRec = csvLine.split(ciSeperator);
				if (crashRec[0].length() != 0 && crashRec[0].equals(tmc))
				{
					Calendar calendar = Calendar.getInstance();
					calendar.setTime(crashTime);
					// tmcInOutput.add(crashRecord[0]);
					int hours = calendar.get(Calendar.HOUR_OF_DAY);
					int minutes = calendar.get(Calendar.MINUTE);
					int minIndex = minutes / 15;
					minIndex = (hours * 4) + minIndex + 2;
					CI = Integer.parseInt(crashRec[minIndex]);
					return CI;
				}
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
		return CI;
	}
}
