package scraping;

import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class ScraperOptions {
	private static final String FILE = "config" + File.separator + "scraper.json";
	private String validLinkPrefix;
	private String loadLinkMethod;
	private String loadLinkButtonText;
	private String startLink;
	private long maxLinkCount = 0;
	private long thread;

	public long getThread() {
		return thread;
	}

	public void setThread(long thread) {
		this.thread = thread;
	}

	private PageSelector pageSelector;

	public ScraperOptions() {

	}

	public String getValidLinkPrefix() {
		return validLinkPrefix;
	}

	public void setValidLinkPrefix(String validLinkPrefix) {
		this.validLinkPrefix = validLinkPrefix;
	}

	public String getLoadLinkMethod() {
		return loadLinkMethod;
	}

	public void setLoadLinkMethod(String loadLinkMethod) {
		this.loadLinkMethod = loadLinkMethod;
	}

	public String getLoadLinkButtonText() {
		return loadLinkButtonText;
	}

	public void setLoadLinkButtonText(String loadLinkButtonText) {
		this.loadLinkButtonText = loadLinkButtonText;
	}

	public String getStartLink() {
		return startLink;
	}

	public void setStartLink(String startLink) {
		this.startLink = startLink;
	}

	public long getMaxLinkCount() {
		return maxLinkCount;
	}

	public void setMaxLinkCount(long maxLinkCount) {
		this.maxLinkCount = maxLinkCount;
	}

	public PageSelector getPageSelector() {
		return pageSelector;
	}

	public void setPageSelector(PageSelector pageSelector) {
		this.pageSelector = pageSelector;
	}

	/**
	 * Load options from JSON File in config
	*/
	public void setByJSONFile() throws Exception {
		JSONParser parser = new JSONParser();
		Object obj = parser.parse(new FileReader(FILE));
		JSONObject options = (JSONObject) obj;

		setValidLinkPrefix((String) options.get("validLinkPrefix"));
		setLoadLinkMethod((String) options.get("loadLinkMethod"));
		setLoadLinkButtonText((String) options.get("loadLinkButtonText"));
		setMaxLinkCount((long) options.get("maxLinkCount"));
		setThread((long) options.get("thread"));
		setStartLink((String) options.get("startLink"));
	}

	@SuppressWarnings("unchecked")
	public void writeToJSONFile () throws Exception {
		JSONObject main = new JSONObject();
		main.put("validLinkPrefix", this.validLinkPrefix);
		main.put("loadLinkMethod", this.loadLinkMethod);
		main.put("loadLinkButtonText", this.loadLinkButtonText);
		main.put("maxLinkCount", this.maxLinkCount);
		main.put("thread", thread);
		main.put("startLink", startLink);
		
		PrintWriter file = new PrintWriter(FILE);
		file.write(main.toJSONString());
		file.close();
	}
}
