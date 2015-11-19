import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Properties;

public class ConfigReader
{
	String									result			= "";
	InputStream								inputStream;
	private static HashMap<String, String>	configValues	= new HashMap<String, String>();

	public String getPropValues() throws IOException
	{
		try
		{
			Properties prop = new Properties();
			String propFileName = "config.properties";

			inputStream =  new FileInputStream(propFileName);

			if (inputStream != null)
			{
				prop.load(inputStream);
			}
			else
			{
				throw new FileNotFoundException("property file '" + propFileName + "' not found in the classpath");
			}

			// get the property value and print it out
			configValues.put("CI_SEPERATOR", prop.getProperty("CI_SEPERATOR"));
			configValues.put("AHCI_SEPERATOR", prop.getProperty("AHCI_SEPERATOR"));
			configValues.put("TEAS_SEPERATOR", prop.getProperty("TEAS_SEPERATOR"));
			configValues.put("SPEEDLIMIT_SEPERATOR", prop.getProperty("SPEEDLIMIT_SEPERATOR"));
			configValues.put("CRASHRECORD_SEPERATOR", prop.getProperty("CRASHRECORD_SEPERATOR"));

		}
		catch (Exception e)
		{
			System.out.println("Exception: " + e);
		}
		finally
		{
			inputStream.close();
		}
		return result;
	}

	public String getConfigValue(String key)
	{
		return configValues.get(key);
	}
}
