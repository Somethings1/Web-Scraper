package scraping;
//import playwright and the proxy packages
import com.microsoft.playwright.*;
import com.microsoft.playwright.options.Proxy;
//import some useful packages
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Vector;
import article.PageSelector;
import javafx.util.Pair;
import article.Article;
import java.util.Queue;
import javafx.util.Pair;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Scraper {	
	public Scraper () {
		Article.id = Article.getTotalArticle() + 1;
	}
	
	private String getWebNameFromURL (String url) {
		Pattern pattern = Pattern.compile("^(?:http(?:s?):\\/\\/(?:www\\.)?)?([A-Za-z0-9_:.-]+)\\/?");
	    Matcher matcher = pattern.matcher(url);
	    if (matcher.find())
	    	return matcher.group(1);
	    else return "";
	}
	
	private String scrapeSingleElement (Page page, String selector) 
			throws Exception {
		String content;
		
		if (page.locator(selector).count() == 0) 
			throw new Exception("Page format is not right");
		
		content = page.locator(selector).locator("nth=" + 0).innerText();
		return content;
	}
	
	private Vector<String> scrapeMultipleElement (Page page, String selector) 
			throws Exception {
		Vector<String> content = new Vector<String>();
		int total = page.locator(selector).count();
		
		if (total == 0)
			throw new Exception("Page format is not right");
		
		for (int i = 0; i < total; i++) {
			content.add(page.locator(selector).locator("nth=" + i).innerText());
		}
		return content;
	}
	
	public Vector<String> scrapeAllLinkInPage (Page page) {
		Vector<String> content = new Vector<String>();
		int total = page.locator("a").count();
		for (int i = 0; i < total; i++) {
			String URL = page.locator("a").locator("nth=" + i).getAttribute("href");
			if (getWebNameFromURL(URL) == "") URL = "https://" + getWebNameFromURL(page.url()) + URL;
			else if (getWebNameFromURL(URL) != getWebNameFromURL(page.url())) continue;
			content.add(URL);
		}
		return content;
	}
	
	
	private void scrapeSinglePage (Page page, PageSelector pageSelector, Article article) 
			throws Exception{
		article.link = page.url();
		article.webName = getWebNameFromURL(article.link);
		try {
			article.summary = scrapeSingleElement(page, pageSelector.getSummary());
			article.title = scrapeSingleElement(page, pageSelector.getTitle());
			article.content = scrapeSingleElement(page, pageSelector.getParagraph());
			article.publishDate = scrapeSingleElement(page, pageSelector.getPublishDate());
			article.author = scrapeSingleElement(page, pageSelector.getAuthorName());
			article.hashtag = scrapeMultipleElement(page, pageSelector.getHashtag());
			article.category = scrapeMultipleElement(page, pageSelector.getCategory());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			System.out.println(e.getMessage());
			throw e;
		}
	}
	
	public void scrapeSinglePage (String link, PageSelector pageSelector) {
		try (Playwright playwright = Playwright.create()) {
			
			BrowserType.LaunchOptions launchOptions = new BrowserType.LaunchOptions();
			Browser browser = playwright.chromium().launch(launchOptions);
			Page page = browser.newPage();
			Article article = new Article();
			
			page.navigate(link);
			try {
				Article.id = Article.getTotalArticle();
				scrapeSinglePage(page, pageSelector, article);
				Article.updateTotalArticle();
				
				article.saveToJSON();
			} catch (Exception e) {
				e.getStackTrace();
			}
		}
	}
	
	/**
	 * Scrape a site by continuously navigate to its hyperlink on each page
	*/
	public void scrapeWholeSite (String link, PageSelector pageSelector, int maxDepth) {
		try (Playwright playwright = Playwright.create()) {
			
			BrowserType.LaunchOptions launchOptions = new BrowserType.LaunchOptions();
			Browser browser = playwright.chromium().launch(launchOptions);
			Page page = browser.newPage();
			Article article = new Article();
			
			Queue<Pair<String, Integer>> q = new LinkedList<>();
			HashSet<String> listOfURL = new HashSet<String>();
			
			q.add(new Pair<String, Integer>(link, 0));
			
			while (q.peek() != null) {
				String curVal = q.peek().getKey();
				int curDepth = q.poll().getValue();
				System.out.println(curDepth + ": " + curVal);
				
				try {						
					page.navigate(curVal);
					scrapeSinglePage(page, pageSelector, article);
					article.saveToJSON();
				} catch (Exception e) {
					System.out.println(e.getMessage());
					continue;
				}
				
				
				if (curDepth == maxDepth) continue;
				
				Vector<String> nextURL = scrapeAllLinkInPage(page);
				System.out.println(nextURL.size());
				
				for (String URL: nextURL) 
					if(!listOfURL.contains(URL)) {
						q.add(new Pair<String, Integer>(URL, curDepth + 1));
						listOfURL.add(URL);
					}
			}
		}
	}
	
	public static void main(String[] args) {
		String link = "https://blockchain.news/news/vitalik-buterin-supporting-decentralized-staking-through-anti-correlation-incentives";
		PageSelector pageSelector = new PageSelector();
		pageSelector.setAuthorName("a.entry-cat");
		pageSelector.setCategory(".entry-label");
		pageSelector.setHashtag(".tagcloud a");
		pageSelector.setParagraph(".textbody");
		pageSelector.setPublishDate(":nth-match(.entry-date, 1)");
		pageSelector.setSummary(".text-size-big");
		pageSelector.setTitle(".title");
		Scraper scraper = new Scraper();
		scraper.scrapeWholeSite(link, pageSelector, 15);
        //scraper.scrapeWholeSite(link, pageSelector, 15);
    }
}
