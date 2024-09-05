package scraping;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.PlaywrightException;

import article.Article;
import article.ArticleSet;
import scraping.settings.PageSelector;
import scraping.settings.ScraperOptions;
import utility.StringUtility;

class SingleThreadScraper extends ScrapingController implements Runnable {
	
	public static int threadCounter = 0;
	
	private final int MAXIMUM_ATTEMPT_PER_LINK = 5;

	private HashMap<String, Integer> nextURL = new HashMap<String, Integer>();
	private HashMap<String, Integer> visitedURL = new HashMap<String, Integer>();
	private HashSet<String> newURL = new HashSet<String>();
	
	private PageSelector pageSelector;
	private BrowserController browserController;
	private Page page;
	
	public SingleThreadScraper(ScraperOptions scraperOptions, ArticleSet articleSet, Vector<String> nextURL, HashSet<String> visitedURL)
			throws Exception {
		super(scraperOptions, articleSet);
		threadCounter++;

		for (String s : nextURL)
			this.nextURL.put(s, 0);
		for (String s : visitedURL)
			this.visitedURL.put(s, 0);
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

	private boolean isValidLinkPrefix(String link) {
		if (link.startsWith("https://"))
			link = link.substring(8);
		if (link.startsWith("www"))
			link = link.substring(4);
		return link.startsWith(scraperOptions.getValidLinkPrefix());
	}

	private Vector<String> scrapeElement(String instruction) 
			throws Exception {
		return (new ElementScraper(this.page, instruction)).getScrapedElements();
	}
	
	private boolean isLocalLink (String link) {
		return StringUtility.getWebNameFromURL(link).length() == 0;
	}

	private boolean isUnrelatedLink (String link, String currentPageLink) {
		String webName = StringUtility.getWebNameFromURL(link);
		return webName.length() > 0 && !webName.equals(StringUtility.getWebNameFromURL(currentPageLink));
	}
	
	private boolean isVisitedLink (String link) {
		return visitedURL.containsKey(link) || nextURL.containsKey(link) || newURL.contains(link);
	}
	
	private boolean isValidLink (String link, String currentPageLink) {
		return !isUnrelatedLink(link, currentPageLink) && 
			   !isVisitedLink(link);
	}

	private void scrapeAllLinkInPage() throws Exception {
		int total = page.locator("a").count();

		for (int i = 0; i < total; i++) {
			String link = page.locator("a").locator("nth=" + i).getAttribute("href");
			if (isLocalLink(link)) 
				link = "https://www." + StringUtility.getWebNameFromURL(page.url()) + link;
			if (isValidLink(link, page.url()))
				newURL.add(link);
		}
		
		Debugger.yell("Link count: " + newURL.size());
	}
	
	private void validateLinkPrefix (String link) 
			throws Exception {
		if (!isValidLinkPrefix(link)) {
			throw new Exception("Invalid link prefix! Valid prefix: " + scraperOptions.getValidLinkPrefix()
					+ " while link: " + link);
		}
	}

	private Article scrapePageToArticle(PageSelector pageSelector) throws Exception {
		validateLinkPrefix(this.page.url());
		
		Article article = new Article();

		article.link = this.page.url();
		article.webName = StringUtility.getWebNameFromURL(article.link);
		article.summary = scrapeElement(pageSelector.getSummary()).elementAt(0);
		article.title = scrapeElement(pageSelector.getTitle()).elementAt(0);
		article.content = scrapeElement(pageSelector.getContent());
		article.publishDate = scrapeElement(pageSelector.getPublishDate()).elementAt(0).substring(0, 19);
		article.authors = scrapeElement(pageSelector.getAuthors());
		article.hashtag = scrapeElement(pageSelector.getHashtag());
		article.category = scrapeElement(pageSelector.getCategory());
		
		return article;
	}

	private void savePage () throws Exception {
		Article article = scrapePageToArticle(pageSelector);
		article.save();
		articleSet.add(article);
		
		Debugger.yell("Completed, saved to " + article.ownID + ".json");
	}
	
	private boolean reachedMaximumAttemptAt (String link) {
		return nextURL.get(link) > MAXIMUM_ATTEMPT_PER_LINK;
	}
	
	private void handleConnectionError (String link, String errorMessage) {
		if (reachedMaximumAttemptAt(link)) {
			Debugger.yell("Stopped trying!");
			markLinkAsVisited(link);
			Debugger.yell("Something wrong with scraper. Check your proxy or internet connection.");
		}
		else {
			markLinkAsUnvisited(link);
			Debugger.yell(errorMessage + " at link: " + link);
			Debugger.yell("Trying again (" + nextURL.get(link) + ")");
		}
	}
	
	private boolean isLinkVisited (String link) {
		return visitedURL.containsKey(link);
	}

	private void scrapeSinglePage(String link) throws Exception {
		if (isLinkVisited(link)) {
			markLinkAsVisited(link);
			return;
		}
		markLinkAsVisited(link);

		final long startTime = System.currentTimeMillis();

		Debugger.yell("Remaining links: " + nextURL.size());
		Debugger.yell("Navigating to: " + link);

		try {
			this.page = this.browserController.openPageAt(link);
			scrapeAllLinkInPage();
			savePage();
		} catch (PlaywrightException e) {
			handleConnectionError(link, e.getMessage());
		} catch (Exception e) {
			Debugger.yell(e.getMessage());
		}
		
		this.page.context().close();
		final long endTime = System.currentTimeMillis();
		Debugger.yell("Total time: " + (endTime - startTime));
	}

	public void run() {
		Debugger.yell("Started");
		this.pageSelector = scraperOptions.getPageSelector();
		try {
			this.browserController = new BrowserController();
			
			while (nextURL.size() > 0) {
				scrapeSinglePage(getNextLink());
			}
			
			this.browserController.close();
			threadCounter--;
			
			super.reopenThread(newURL);
		} catch (Exception e) {
			Debugger.yell(e.getMessage());
		}
		Debugger.yell("Closed");
	}
}