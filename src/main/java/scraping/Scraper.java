package scraping;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Scanner;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.options.Proxy;

import article.Article;
import article.ArticleSet;
import gui.pages.CrawlPageController;
import javafx.application.Platform;

class SingleThreadScraper extends Scraper implements Runnable {
	private static final String PROXY_FILE = "config" + File.separator + "prx.txt";
	public static int threadCounter = 0;
	private final int AFTER_SCROLLING_WAIT_TIME = 5000;
	private final int DEFAULT_WAIT_TIME = 60000;

	private HashMap<String, Integer> nextURL = new HashMap<String, Integer>();
	private HashMap<String, Integer> visitedURL = new HashMap<String, Integer>();
	private HashSet<String> newURL = new HashSet<String>();
	private int proxyCounter;
	private Vector<String> proxy = new Vector<String>();
	private Browser browser;
	

	public SingleThreadScraper(ScraperOptions scraperOptions, ArticleSet articleSet, Vector<String> nextURL, HashSet<String> visitedURL)
			throws Exception {
		super(scraperOptions, articleSet);
		loadProxyList();
		proxyCounter = 0;
		threadCounter++;

		for (String s : nextURL)
			this.nextURL.put(s, 0);
		for (String s : visitedURL)
			this.visitedURL.put(s, 0);
	}

	private void loadProxyList() {
		try {
			File file = new File(PROXY_FILE);
			Scanner reader = new Scanner(file);

			while (reader.hasNextLine()) {
				String prx = reader.nextLine();
				if (prx.isBlank())
					continue;
				proxy.add(prx);
			}

			reader.close();
		} catch (Exception e) {
			debug(e.getMessage());
		}
	}

	private void markLinkAsVisited(String link) {
		visitedURL.put(link, nextURL.get(link) + 1);
		nextURL.remove(link);
	}

	private void markLinkAsUnvisited(String link) {
		nextURL.put(link, visitedURL.get(link));
		visitedURL.remove(link);
	}

	private String getNextLink() {
		Iterator<HashMap.Entry<String, Integer>> it = nextURL.entrySet().iterator();
		ConcurrentHashMap.Entry<String, Integer> entry = it.next();
		return entry.getKey();
	}

	private String getWebNameFromURL(String url) {
		if (url == null)
			return "";
		Pattern pattern = Pattern.compile("^(?:http(?:s?):\\/\\/(?:www\\.)?)?([A-Za-z0-9_:.-]+)\\/?");
		Matcher matcher = pattern.matcher(url);
		if (matcher.find())
			return matcher.group(1);
		else
			return "";
	}

	private boolean checkValidLinkPrefix(String link) {
		if (link.startsWith("https://"))
			link = link.substring(8);
		if (link.startsWith("www"))
			link = link.substring(4);
		return link.startsWith(scraperOptions.getValidLinkPrefix());
	}

	private Vector<String> scrapeElementByCSSSelector(Page page, String selector, boolean keepHTMLFormat) {
		Vector<String> content = new Vector<String>();
		int total = page.locator(selector).count();

		for (int i = 0; i < total; i++) {
			if (keepHTMLFormat)
				content.add(page.locator(selector).nth(i).innerHTML());
			else
				content.add(page.locator(selector).nth(i).innerText());
		}

		return content;
	}

	private Vector<String> scrapeElementByJSONConfig(Page page, String selector) {
		Vector<String> content = new Vector<String>();
		String JSONConfig = page.locator("script[type=\"application/ld+json\"]").innerText();

		int start = JSONConfig.indexOf("\"" + selector + "\"");
		while (start != -1) {
			int attrStart = start + selector.length() + 4;

			content.add(JSONConfig.substring(attrStart, JSONConfig.indexOf("\"", attrStart)));

			start = JSONConfig.indexOf("\"" + selector + "\"", start + 1);
		}

		return content;
	}

	private Vector<String> scrapeElementByMetaProperty(Page page, String selector) {
		Vector<String> content = new Vector<String>();

		selector = "meta[property=\"" + selector + "\"]";
		int cnt = page.locator(selector).count();
		for (int i = 0; i < cnt; i++) {
			content.add(page.locator(selector).nth(i).getAttribute("content"));
		}

		return content;
	}

	private Vector<String> scrapeElement(Page page, String _selector) throws Exception {
		Vector<String> content = new Vector<String>();

		String[] tmp = _selector.split(";");
		String type = tmp[0];
		String selector = tmp[1];
		String required = tmp[2];

		switch (type) {
		case "1":
			content = scrapeElementByCSSSelector(page, selector, false);
			break;
		case "2":
			content = scrapeElementByCSSSelector(page, selector, true);
			break;
		case "3":
			content = scrapeElementByJSONConfig(page, selector);
			break;
		case "4":
			content = scrapeElementByMetaProperty(page, selector);
			break;
		default:
			throw new Exception("Invalid selector format");
		}

		if (required.equals("1") && content.size() == 0)
			throw new Exception("Invalid page format");
		return content;
	}

	private void scrapeAllLinkInPage(Page page) throws Exception {
		int total = page.locator("a").count();
		// Show as many links as possible in main page
		if (scraperOptions.getLoadLinkMethod().equals("scroll")) {
			for (int i = 0; i < 10; i++) {
				page.mouse().wheel(0, 100000);
				Thread.sleep(AFTER_SCROLLING_WAIT_TIME);
			}
		} else if (scraperOptions.getLoadLinkMethod().equals("click")) {
			while (page.getByText(scraperOptions.getLoadLinkButtonText()) != null) {
				page.getByText(scraperOptions.getLoadLinkButtonText()).click();
				Thread.sleep(AFTER_SCROLLING_WAIT_TIME);
			}

		}

		// Scrape links
		for (int i = 0; i < total; i++) {
			String URL = page.locator("a").locator("nth=" + i).getAttribute("href");
			String webName = getWebNameFromURL(URL);

			// Format local link
			if (webName == "")
				URL = "https://www." + getWebNameFromURL(page.url()) + URL;

			// Remove link to other websites
			if (webName.length() > 0 && !webName.equals(getWebNameFromURL(page.url())))
				continue;

			// Remove visited links
			if (visitedURL.containsKey(URL) || nextURL.containsKey(URL) || newURL.contains(URL))
				continue;

			newURL.add(URL);
		}
	}

	private void scrapePageToArticle(Page page, PageSelector pageSelector, Article article) throws Exception {
		if (!checkValidLinkPrefix(page.url())) {
			throw new Exception("Invalid link prefix! Valid prefix: " + scraperOptions.getValidLinkPrefix()
					+ " while link: " + page.url());
		}

		article.link = page.url();
		article.webName = getWebNameFromURL(article.link);
		article.summary = scrapeElement(page, pageSelector.getSummary()).elementAt(0);
		article.title = scrapeElement(page, pageSelector.getTitle()).elementAt(0);
		article.content = scrapeElement(page, pageSelector.getContent());
		article.publishDate = scrapeElement(page, pageSelector.getPublishDate()).elementAt(0).substring(0, 19);
		article.authors = scrapeElement(page, pageSelector.getAuthors());
		article.hashtag = scrapeElement(page, pageSelector.getHashtag());
		article.category = scrapeElement(page, pageSelector.getCategory());
	}

	private BrowserContext createBrowserContext() {
		BrowserContext context;
		if (proxy.size() > 0) {
			// Setting up proxy
			String[] t = proxy.elementAt(proxyCounter % proxy.size()).split(":");
			Proxy prox = new Proxy(t[0] + ":" + t[1]);
			prox.setUsername(t[2]).setPassword(t[3]);
			proxyCounter++;
			context = browser.newContext(new Browser.NewContextOptions().setJavaScriptEnabled(false).setProxy(prox));
			debug("Proxy: " + t[0] + ":" + t[1]);
		} else {
			context = browser.newContext(new Browser.NewContextOptions().setJavaScriptEnabled(false));
		}
		return context;
	}

	private void scrapeSinglePage(String link, PageSelector pageSelector) throws Exception {
		if (visitedURL.containsKey(link)) {
			markLinkAsVisited(link);
			return;
		}
		markLinkAsVisited(link);

		final long startTime = System.currentTimeMillis();

		debug("Remaining links: " + nextURL.size());
		debug("Navigating to: " + link);

		// Open browser, add page
		BrowserContext context = createBrowserContext();
		Page page = context.newPage();

		// Ignore ERR_TIMED_OUT Exception
		page.setDefaultTimeout(DEFAULT_WAIT_TIME);

		// Block all images and css then go to link
		page.route("**/*.{png,jpg,jpeg,css,ico,svg}", route -> route.abort());

		try {
			// Go to link
			long navigateTimeStart = System.currentTimeMillis();
			page.navigate(link); // must be put in a try
			long navigateTimeEnd = System.currentTimeMillis();
			debug("Naviga ted in " + (navigateTimeEnd - navigateTimeStart));

			// Get all hyperlinks in the page, write them to newURL
			scrapeAllLinkInPage(page);
			debug("Link count: " + newURL.size());

			// Real scraping process
			Article article = new Article();
			scrapePageToArticle(page, pageSelector, article);
			
			// If no exception were thrown
			article.saveToJSON();
			articleSet.add(article);
			
			
			debug("Completed, saved to " + article.ownID + ".json");
		} catch (PlaywrightException e) {
			// PlaywrightException usually due to internet connection error
			// So we try a few times before terminate it
			markLinkAsUnvisited(link);
			debug(e.getMessage() + " at link: " + link);

			// If we tried a few times and same thing happened
			if (nextURL.get(link) > 5) {
				debug("Stopped trying!");
				markLinkAsVisited(link);
				
				debug("Something wrong with scraper. Check your proxy or internet connection.");
			}
			debug("Trying again (" + nextURL.get(link) + ")");
		} catch (Exception e) {
			debug(e.getMessage());
		}

		context.close();

		final long endTime = System.currentTimeMillis();
		debug("Total time: " + (endTime - startTime));
	}

	public void run() {
		debug("Started");
		try {
			Playwright playwright = Playwright.create();
			// Open a browser for each thread
			BrowserType.LaunchOptions launchOptions = new BrowserType.LaunchOptions();
			// launchOptions.setProxy("per-context");
			browser = playwright.chromium().launch(launchOptions);

			while (nextURL.size() > 0) {
				scrapeSinglePage(getNextLink(), scraperOptions.getPageSelector());
			}
			browser.close();

			threadCounter--;
			// Send new links to thread manager
			super.reopenThread(newURL);
		} catch (Exception e) {
			debug(e.getMessage());
		}
		debug("Closed");
	}
}

/**
 * <b>Scraper</b><br>
 * Provide proxy list int <code>prx.txt</code>
 */
public class Scraper {
	/**
	 * The options for this scraper
	*/
	protected final ScraperOptions scraperOptions;

	/** 
	 * All URLs distributed to threads
	*/
	private HashSet<String> visitedURL = new HashSet<String>();

	/**
	 * URLs waiting to be distributed
	 */
	private HashSet<String> newURL = new HashSet<String>();

	/**
	 * Article set to write new articles to
	 */
	protected ArticleSet articleSet;

	/**
	 * Start the scraper. 
	 * @param scraperOptions the ScraperOptions object to be set to the scraper
	 * @param articleSet the current ArticleSet to put new articles to and to avoid duplicating while scraping
	 */
	public Scraper(ScraperOptions scraperOptions, ArticleSet articleSet) throws Exception {
		Article.beginIDCount();
		this.scraperOptions = scraperOptions;
		this.articleSet = articleSet;
	}
	
	/**
	 * Print the log to the CrawlPageController._console TextArea. 
	 * This is protected because only Scraper and SingleThreadScraper use it
	 * @param t the String to print out
	*/
	protected void debug(String t) {
		String s = "(" + Thread.currentThread().getName() + ")" + t + "\n";
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				if (CrawlPageController._console.getText().length() > 10000) {
					CrawlPageController._console.setText("");
				}
				CrawlPageController._console.appendText(s);
			}
		});
		//System.out.print(s);
	}

	/**
	 * Calling this method sends a list of new URLs to the Scraper. 
	 * If there is still a SingleThreadScraper running, it will collect the URLs then terminate
	 * until all threads are all terminated. Only by then, it will open <code>scraperOptions.thread</code>
	 * new SingleThreadScraper(s) and distribute the new URLs to them
	 * @param _newURL a HashSet of String contains all the URLs sent by SingleThreadScraper(s)
	*/
	protected synchronized void reopenThread(HashSet<String> _newURL) throws Exception {
		// If there's still a thread running, just add new URLs to the set
		debug(String.valueOf(_newURL.size()));
		debug(String.valueOf(SingleThreadScraper.threadCounter));
		newURL.addAll(_newURL);
		if (SingleThreadScraper.threadCounter != 0)
			return;

		debug("All threads closed.");
		// Distribute next URLs to the threads evenly
		Vector<String> nextURL = new Vector<String>(newURL);

		debug("New URL count: " + nextURL.size());
		// Abort the app when there's no URL left
		if (nextURL.size() == 0)
			return;

		// If the number of new URL < number of thread then open only one new thread
		if (nextURL.size() < scraperOptions.getThread()) {
			new Thread(
					new SingleThreadScraper(scraperOptions, articleSet, nextURL, visitedURL)
					).start();
			return;
		}

		long t = scraperOptions.getThread() - SingleThreadScraper.threadCounter;
		int block = (int) (nextURL.size() / t);
		// backup();
		debug("Total link: " + nextURL.size());

		for (int i = 0; i < t; i++) {
			int start = i * block;
			int end = (i + 1) * block;

			Vector<String> tmp = new Vector<String>();
			for (int j = start; j < end && j < nextURL.size(); j++) {
				tmp.add(nextURL.elementAt(j));
			}
			
			// Start new thread
			new Thread(
				new SingleThreadScraper(scraperOptions, articleSet, tmp, visitedURL)
				).start();
			
			// URLs distributed to a thread should be ignored in the future calls
			visitedURL.addAll(tmp);
		}

		// ready for the next distributing event
		newURL.clear();
	}

	/**
	 * this.visitedURL can be accessed (write only) via this method. 
	 * @param s a Collection of String to be put to this.visitedURL
	*/
	public void addToVisitedList(Collection<String> s) {
		for (String t: s) this.visitedURL.add(t);
	}

	/**
	 * Scrape by the settings in <code>scraper.json</code> and
	 * <code>selector.json</code>. Provide proxy list in <code>prx.txt</code>. The
	 * number of proxy should at least double the number of thread
	 */
	public void scrape() throws Exception {
		if (newURL.size() == 0)
			newURL.add(scraperOptions.getStartLink());
		reopenThread(newURL);
	}
}
