package scraping;
import article.PageSelector;

public class ScraperOptions {
	private String startLink;
	private String validLinkPrefix;
	private String mainPagePrefix;
	private int jumpDistance;
	private String loadLinkMethod = "scroll";
	private int waitTime;
	private String loadLinkButtonText = "Load More";
	private String scrapeMethod = "BFS";
	private int maxDepth = 0;	
	private PageSelector pageSelector;
	private int maxDistance;
	private int startCount;
	
	public int getStartCount() {
		return startCount;
	}

	public synchronized void setStartCount(int startCount) {
		this.startCount = startCount;
	}

	public int getMaxDistance() {
		return maxDistance;
	}

	public synchronized void setMaxDistance(int maxDistance) {
		this.maxDistance = maxDistance;
	}

	public PageSelector getPageSelector() {
		return pageSelector;
	}

	public void setPageSelector(PageSelector pageSelector) {
		this.pageSelector = pageSelector;
	}

	public String getScrapeMethod() {
		return scrapeMethod;
	}

	public void setScrapeMethod(String scrapeMethod) 
	throws Exception {
		if (scrapeMethod != "LIST" && scrapeMethod != "BFS") 
			throw new Exception("Invalid scrape method");
		
		this.scrapeMethod = scrapeMethod;
	}

	/**
	 * <code>setDepth(-1)</code> to scrape whole site<br>
	 * <code>setDepth(0)</code> to scrape just startLink, then every link on startLink has depth of 1 and so on
	*/
	public void setDepth (int depth) {
		if (depth != -1)
			maxDepth = depth;
		else 
			maxDepth = 1000000;
	}
	
	public int depth() {
		return maxDepth;
	}
	
	public String getLoadLinkButtonText() {
		return loadLinkButtonText;
	}

	public void setLoadLinkButtonText(String loadLinkButtonText) {
		this.loadLinkButtonText = loadLinkButtonText;
	}

	public int getWaitTime() {
		return waitTime;
	}

	public void setWaitTime(int waitTime) {
		this.waitTime = waitTime;
	}

	public void setValidLinkPrefix (String validLinkPrefix) {
		this.validLinkPrefix = validLinkPrefix;
	}

	public void setStartLink(String startLink) {
		this.startLink = startLink;
	}
	
	public String getStartLink() {
		return startLink;
	}

	public String getValidLinkPrefix() {
		return validLinkPrefix;
	}

	public String getMainPagePrefix() {
		return mainPagePrefix;
	}

	public void setMainPagePrefix(String mainPagePrefix) {
		this.mainPagePrefix = mainPagePrefix;
	}

	public int getJumpDistance() {
		return jumpDistance;
	}

	public void setJumpDistance(int jumpDistance) {
		this.jumpDistance = jumpDistance;
	}
	
	public void setLoadLinkMethod (String loadLinkMethod) 
	throws Exception {
		if (loadLinkMethod != "click"
		 && loadLinkMethod != "scroll"
		 && loadLinkMethod != "new page") throw new Exception("Invalid load-link method");
		this.loadLinkMethod = loadLinkMethod;
	}
	
	public String getLoadLinkMethod () {
		return loadLinkMethod;
	}
	
	
}
