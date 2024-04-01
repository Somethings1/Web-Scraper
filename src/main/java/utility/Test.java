package utility;

import java.util.Vector;
import article.PageSelector;
import scraping.Scraper;
import scraping.ScraperOptions;

public class Test {	
	public static void main(String[] args) {
		PageSelector pageSelector = new PageSelector();

		// Create options for scraper
		ScraperOptions scraperOptions = new ScraperOptions();
		try {
			pageSelector.setByFile("selector-config.info");
			scraperOptions.setValidLinkPrefix("https://www.theblock.co/post");
			scraperOptions.setStartLink("https://www.theblock.co/");
			scraperOptions.setLoadLinkMethod("new page");
			scraperOptions.setScrapeMethod("LIST");
			scraperOptions.setMainPagePrefix("https://www.theblock.co/latest?start=");
			scraperOptions.setDepth(-1);
			scraperOptions.setJumpDistance(10);
			scraperOptions.setPageSelector(pageSelector);
		} catch (Exception e) {

		}

		for (int i = 0; i < 5; i++) {
			scraperOptions.setStartCount(i * 4000);
			scraperOptions.setMaxDistance((i + 1) * 4000 - 10);
			Scraper scraper = new Scraper(scraperOptions);
			scraper.start();
			
			try {
				Thread.sleep(1000);
			}
			catch (Exception e) {}
			
			
		}
	}
}
