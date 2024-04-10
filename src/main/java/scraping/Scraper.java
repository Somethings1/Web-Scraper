package scraping;

//import playwright and the proxy packages
import com.microsoft.playwright.*;
import com.microsoft.playwright.options.Proxy;

import java.io.File;
//import some useful packages
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import article.PageSelector;
import article.Article;

/**
 * <b>Scraper</b><br>
 * Provide proxy list int <code>prx.txt</code> If you want to scrape a single
 * page, use <code>Scraper.scrapeSinglePage(String link, PageSelector ps)<code>
 * If you want to scrape a set of page (same website), use <code>Scraper.scrapeWholeSite(String link, PageSelector ps, [optional] int maxDepth = 15)</code>
 */

class SingleThreadScraper extends Thread {
	private static final String PROXY_FILE = "config" + File.separator + "prx.txt";
	public static int threadCounter = 0;

	private HashMap<String, Integer> nextURL = new HashMap<String, Integer>();
	private HashMap<String, Integer> visitedURL = new HashMap<String, Integer>();
	private HashSet<String> newURL = new HashSet<String>();
	private int proxyCounter;
	private Vector<String> proxy = new Vector<String>();
	private ScraperOptions scraperOptions;
	private Browser browser;

	public SingleThreadScraper(ScraperOptions scraperOptions,
			Vector<String> nextURL, HashSet<String> visitedURL) {
		loadProxyList();
		proxyCounter = 0;
		this.scraperOptions = scraperOptions;
		threadCounter++;

		for (String s: nextURL) this.nextURL.put(s, 0);
		for (String s: visitedURL) this.visitedURL.put(s, 0);
	}
	
	private void debug(String t) {
		System.out.println("(" + Thread.currentThread().getName() + ") " + t);
	}

	public void loadProxyList() {
		try {
			File file = new File(PROXY_FILE);
			Scanner reader = new Scanner(file);

			while (reader.hasNextLine()) {
				String prx = reader.nextLine();
				proxy.add(prx);
			}

			reader.close();
		} catch (Exception e) {
			e.getStackTrace();
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

	private void scrapeAllLinkInPage(Page page) {
		int total = page.locator("a").count();
		// Show as many links as possible in main page
		

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
			// System.out.println(URL + " " + webName);

			// Remove links that are not articles, in case we are in the main page
			if (!checkValidLinkPrefix(URL))
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

	private void scrapeSinglePage(String link, PageSelector pageSelector) {
		if (visitedURL.containsKey(link)) {
			markLinkAsVisited(link);
			return;
		}
		markLinkAsVisited(link);

		final long startTime = System.currentTimeMillis();

		// Setting up proxy
		String[] t = proxy.elementAt(proxyCounter % proxy.size()).split(":");
		Proxy prox = new Proxy(t[0] + ":" + t[1]);
		prox.setUsername(t[2]).setPassword(t[3]);
		proxyCounter++;

		debug("Link count: " + nextURL.size());
		debug("Navigating to: " + link);
		debug("Proxy: " + t[0] + ":" + t[1]);

		// Open browser, add page
		BrowserContext context = browser.newContext(new Browser.NewContextOptions().setProxy(prox));
		Page page = context.newPage();

		// Ignore ERR_TIMED_OUT Exception
		page.setDefaultTimeout(60000);

		// Block all images and css then go to link
		page.route("**/*.{png,jpg,jpeg,css,ico,svg}", route -> route.abort());

		try {
			long navigateTimeStart = System.currentTimeMillis();
			page.navigate(link); // must be put in a try
			long navigateTimeEnd = System.currentTimeMillis();
			debug("Navigated in " + (navigateTimeEnd - navigateTimeStart));
			Article article = new Article();

			scrapeAllLinkInPage(page);
			scrapePageToArticle(page, pageSelector, article);
			article.saveToJSON();
		}
		catch (PlaywrightException e) {
			markLinkAsUnvisited(link);
			debug(e.getMessage() + " at link: " + link);
			debug("Trying again (" + nextURL.get(link) + ")");
		}
		catch (Exception e) {
			debug(e.getMessage() + " at link: " + link);
		}

		context.close();

		final long endTime = System.currentTimeMillis();
		debug("Time: " + (endTime - startTime));
	}

	public void run() {
		debug("Started");
		try (Playwright playwright = Playwright.create()) {
			// Open a browser for each thread
			BrowserType.LaunchOptions launchOptions = new BrowserType.LaunchOptions();
			launchOptions.setProxy("per-context");
			browser = playwright.chromium().launch(launchOptions);

			while (nextURL.size() > 0) {
				scrapeSinglePage(getNextLink(), scraperOptions.getPageSelector());
			}
			browser.close();
			
			threadCounter--;
			// Send unvisited and visited links to thread manager
			Scraper.reopenThread(newURL, visitedURL);
		}
		debug("Closed");
	}
}

public class Scraper {
	private static ScraperOptions scraperOptions;
	private static HashSet<String> nextURL = new HashSet<String>();
	private static HashSet<String> visitedURL = new HashSet<String>();

	public Scraper(ScraperOptions _scraperOptions) {
		Article.id = Article.getTotalArticle() + 1;
		scraperOptions = _scraperOptions;
	}
	
	private static void openThread (Vector<String> nextURL) {
		SingleThreadScraper scraper = new SingleThreadScraper(scraperOptions, nextURL, visitedURL);
		scraper.start();
		
	}
	
	public static synchronized void reopenThread (HashSet<String> newURL, HashMap<String, Integer> _visitedURL) {
		visitedURL.addAll(_visitedURL.keySet());
		newURL.removeAll(_visitedURL.keySet());
		nextURL.addAll(newURL);
		nextURL.removeAll(visitedURL);
		
		if (SingleThreadScraper.threadCounter < scraperOptions.getThread()) {
			Vector<String> _nextURL = new Vector<String>(nextURL);
			
			if (nextURL.size() < scraperOptions.getThread()) {
				openThread(_nextURL);
				return;
			}
			
			long t = scraperOptions.getThread() - SingleThreadScraper.threadCounter;
			int block = (int) (_nextURL.size() / t);
			System.out.println("Expected: " + scraperOptions.getThread());
			System.out.println("Current: " + Thread.activeCount());
			
			for (int i = 0; i < t; i++) {
				int start = i * block;
				int end = (i + 1) * block;
				
				Vector<String> tmp = new Vector<String>();
				for (int j = start; j < end && j < _nextURL.size(); j++) {
					tmp.add(_nextURL.elementAt(j));
				}
				openThread(tmp);
			}
		}
	}

	public void scrape() {
		Vector<String> tmp = new Vector<String>();
		tmp.add(scraperOptions.getStartLink());
		openThread(tmp);
	}
}
