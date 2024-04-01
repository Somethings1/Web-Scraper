package utility;

import java.util.regex.Matcher;
import java.util.regex.Pattern;	

public class StringFormatter {
	public static String getWebNameFromURL (String url) {
		if (url == null) return "";
		Pattern pattern = Pattern.compile("^(?:http(?:s?):\\/\\/(?:www\\.)?)?([A-Za-z0-9_:.-]+)\\/?");
	    Matcher matcher = pattern.matcher(url);
	    if (matcher.find())
	    	return matcher.group(1);
	    else return "";
	}
}
