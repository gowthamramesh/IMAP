import java.util.HashMap;
import java.util.LinkedHashMap;

public class CrashDataMap
{
	private static LinkedHashMap<String, Float>	tmcMap	= new LinkedHashMap<String, Float>();

	public static void addTMCMap(String tmc, float speedlimit)
	{
		if (tmcMap.get(tmc) == null)
		{
			tmcMap.put(tmc, speedlimit);
		}
	}

	public static HashMap<String, Float> getTMCMap()
	{
		return tmcMap;
	}
}
