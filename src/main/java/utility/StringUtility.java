package utility;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtility {
	public static final boolean contains (String a, String b) {
		if (a == null) return false;
		if (a.length() < b.length()) return false;
		a = a.toLowerCase().trim();
		b = b.toLowerCase().trim();
		return a.contains(b);
	}
	
	public static final String getWebNameFromURL(String url) {
		if (url == null)
			return "";
		Pattern pattern = Pattern.compile("^(?:http(?:s?):\\/\\/(?:www\\.)?)?([A-Za-z0-9_:.-]+)\\/?");
		Matcher matcher = pattern.matcher(url);
		if (matcher.find())
			return matcher.group(1);
		else
			return "";
	}
}
