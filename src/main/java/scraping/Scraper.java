package scraping;
//import playwright and the proxy packages
import com.microsoft.playwright.*;
import com.microsoft.playwright.options.Proxy;

import java.io.File;
//import some useful packages
import java.util.*;
import article.PageSelector;
import article.Article;
import javafx.util.Pair;
import utility.StringFormatter;

/**
 * <b>Scraper</b><br>
 * Provide proxy list int <code>prx.txt</code>
 * If you want to scrape a single page, use <code>Scraper.scrapeSinglePage(String link, PageSelector ps)<code>
 * If you want to scrape a set of page (same website), use <code>Scraper.scrapeWholeSite(String link, PageSelector ps, [optional] int maxDepth = 15)</code>
 */

public class Scraper extends Thread {	
	private HashSet<String> nextURL = new HashSet<String>();
	private static HashSet<String> visitedURL = new HashSet<String>();
	private static int proxyCounter;
	private Vector<String> proxy = new Vector<String>();
	private ScraperOptions scraperOptions;
	
	public Scraper (ScraperOptions scraperOptions) {
		Article.id = Article.getTotalArticle() + 1;
		loadProxyList();
		proxyCounter = 0;
		this.scraperOptions = scraperOptions;
	}
	
	public Scraper () {
		Article.id = Article.getTotalArticle() + 1;
		loadProxyList();
		proxyCounter = 0;
	}
	
	public synchronized void setOptions (ScraperOptions scraperOptions) {
		this.scraperOptions =  scraperOptions;
	}
	
	private boolean checkValidLinkPrefix (String link) {
		return link.startsWith(scraperOptions.getValidLinkPrefix());
	}
	
	public void loadProxyList () {
		try {
			File file = new File(System.getProperty("user.dir") + File.separator + "prx.txt");
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
	
	private Vector<String> scrapeElementByCSSSelector (Page page, String selector, boolean keepHTMLFormat) {
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
	
	private Vector<String> scrapeElementByJSONConfig (Page page, String selector) {
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
	
	private Vector<String> scrapeElementByMetaProperty (Page page, String selector) {
		Vector<String> content = new Vector<String>();
		
		content.add(page.locator("meta[property=\"" + selector + "\"]").getAttribute("content"));
		
		return content;
	}
	
	private Vector<String> scrapeElement (Page page, String _selector) 
			throws Exception {
		Vector<String> content = new Vector<String>();
		
		//System.out.println(_selector);
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
			content = scrapeElementByJSONConfig (page, selector);
			break;
		case "4": 
			content = scrapeElementByMetaProperty(page, selector);
			break;
		default:
			throw new Exception("Invalid selector format");
		}
		
		if (required == "1" && content.size() == 0)
			throw new Exception("Invalid page format");
		return content;
	}
	
	private void scrapeAllLinkInPage (Page page, boolean strict) {
		int total = page.locator("a").count();
		// Show as many links as possible in main page
		if (strict) {
			
		}
		
		// Scrape links
		for (int i = 0; i < total; i++) {
			String URL = page.locator("a").locator("nth=" + i).getAttribute("href");
			String webName = StringFormatter.getWebNameFromURL(URL);
			//System.out.println(URL + " " + webName);
			
			// Format local link
			if (webName == "") URL = "https://www." + StringFormatter.getWebNameFromURL(page.url()) + URL;
			//System.out.println(URL);
			
			// Remove link to other websites
			if (webName != "" && webName != StringFormatter.getWebNameFromURL(page.url())) continue;
			
			// Remove links that are not articles, in case we are in the main page
			if (strict && !checkValidLinkPrefix(URL)) continue; 
			
			// Remove visited links
			if (visitedURL.contains(URL)) continue;
			
			nextURL.add(URL);
		}
	}
	
	
	private void scrapePage (Page page, PageSelector pageSelector, Article article) 
			throws Exception{		
		if (!checkValidLinkPrefix(page.url())) {
			throw new Exception("Invalid link prefix!");
		}
		
		article.link = page.url();
		article.webName = StringFormatter.getWebNameFromURL(article.link);
		try {
			article.summary = scrapeElement(page, pageSelector.getSummary()).elementAt(0);
			article.title = scrapeElement(page, pageSelector.getTitle()).elementAt(0);
			article.content = scrapeElement(page, pageSelector.getContent());
			article.publishDate = scrapeElement(page, pageSelector.getPublishDate()).elementAt(0).substring(0, 19);
			article.authors = scrapeElement(page, pageSelector.getAuthors());
			article.hashtag = scrapeElement(page, pageSelector.getHashtag());
			article.category = scrapeElement(page, pageSelector.getCategory());
		} catch (Exception e) {
			throw e;
		}
	}
	
	private void scrapeSinglePage (String link, PageSelector pageSelector, boolean strict) {
		if (visitedURL.contains(link)) return;
		visitedURL.add(link);
		final long startTime = System.currentTimeMillis();
		try (Playwright playwright = Playwright.create()) {
			
			// Setting up proxy
			String[] t = proxy.elementAt(proxyCounter % proxy.size()).split(":");
			Proxy prox = new Proxy(t[0] + ":" + t[1]);
			prox.setUsername(t[2]).setPassword(t[3]);
			proxyCounter++;
			
			System.out.println("Navigating to: " + link);
			System.out.println("Proxy: " + t[0] + ":" + t[1]);
			
			// Open browser, add page
			BrowserType.LaunchOptions launchOptions = new BrowserType.LaunchOptions();
			launchOptions.setProxy(prox);
			Browser browser = playwright.chromium().launch(launchOptions);
			BrowserContext context = browser.newContext(new Browser.NewContextOptions().setJavaScriptEnabled(false));
			Page page = context.newPage();
			page.setDefaultTimeout(0);
			page.route("**/*.{png,jpg,jpeg,css,ico,svg}", route -> route.abort());
			page.navigate(link);
			
			try {
				Article article = new Article();

				scrapeAllLinkInPage(page, strict);
				System.out.println("Link count: " + nextURL.size());
				scrapePage(page, pageSelector, article);		
				
				article.saveToJSON();
			}
			catch (Exception e) {
				System.out.println(e.getMessage() + " at link: " + link);
			}
		}
		final long endTime = System.currentTimeMillis();
		System.out.println("Time: " + (endTime - startTime));
	}
	
	private void scrapeWholeSiteByBFS (String link, PageSelector pageSelector, int maxDepth) {
		PriorityQueue<Pair<String, Integer>> q = new PriorityQueue<Pair<String, Integer>>((a, b) -> {
//			if (checkValidLinkPrefix(a.getKey())) return 1;
//			else if (checkValidLinkPrefix(b.getKey())) return -1;
//			else return 0;
			return b.getKey().length() - a.getKey().length();
		});
		
		q.add(new Pair<String, Integer>(link, 0));
		
		while (q.peek() != null) {
			String curVal = q.peek().getKey();
			int curDepth = q.poll().getValue();
				
			scrapeSinglePage(curVal, pageSelector, false);	
			
			if (curDepth == maxDepth) continue;
			
			for (String URL: this.nextURL) 
				if(!visitedURL.contains(URL)) {
					q.add(new Pair<String, Integer>(URL, curDepth + 1));
					visitedURL.add(URL);
				}
			
			nextURL.clear();
		}
	}
	
	private void scrapeWholeSiteFromMainPage () {
		for (int cur = scraperOptions.getStartCount(); cur <= scraperOptions.getMaxDistance(); cur += scraperOptions.getJumpDistance()) {
			scrapeSinglePage(scraperOptions.getMainPagePrefix() + cur, scraperOptions.getPageSelector(), true);
		}
		while (nextURL.size() != 0) {
			ArrayList<String> currentURL = new ArrayList<>(nextURL);
			nextURL.clear();
			
			for (String URL: currentURL) {
				scrapeSinglePage(URL, scraperOptions.getPageSelector(), true);
			}
		}
	}
	
	public void scrape () {
		if (scraperOptions.depth() == 0) {
			scrapeSinglePage(scraperOptions.getStartLink(), scraperOptions.getPageSelector(), false);
		}
		else {
			if (scraperOptions.getScrapeMethod() == "BFS") {
				scrapeWholeSiteByBFS(scraperOptions.getStartLink(), scraperOptions.getPageSelector(), scraperOptions.depth());
			}
			else {
				scrapeWholeSiteFromMainPage();
			} 
		}
		Article.updateTotalArticle();
	}
	
	public void run () {
		scrape();
	}
}

/*
 * To-do:
 * - Make the scrape link smarter by clicking or scrolling to show more link
 * */
