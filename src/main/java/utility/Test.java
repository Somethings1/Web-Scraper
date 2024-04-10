package utility;

import java.io.File;
import java.io.PrintWriter;
import article.PageSelector;
import scraping.Scraper;
import scraping.ScraperOptions;

public class Test {	
	private static final String SELECTOR_OPTION_FILE = "config" + File.separator + "selector.json";
	private static final String SCRAPER_OPTION_FILE = "config" + File.separator + "scraper.json";
	
	private static final void reset () {
		try {
			File nextURLFile = new File("config" + File.separator + "next-url.info");
			File visitedURLFile = new File("config" + File.separator + "visited-url.info");
			File totalFile = new File("config" + File.separator + "total.info");
			
			PrintWriter p1 = new PrintWriter(nextURLFile);
			PrintWriter p2 = new PrintWriter(visitedURLFile);
			PrintWriter p3 = new PrintWriter(totalFile);
			
			p1.print("");
			p2.print("");
			p3.print(0);
			
			p1.close();
			p2.close();
			p3.close();
		}
		catch (Exception e) {
			
		}
	}
	
	public static void main(String[] args) {
		// Use reset() at your own risk
		reset();
		try {
			// Set page selector
			PageSelector pageSelector = new PageSelector();
			pageSelector.setByJSONFile(SELECTOR_OPTION_FILE);
			
			// Create options for scraper
			ScraperOptions scraperOptions = new ScraperOptions();
			scraperOptions.setByJSONFile(SCRAPER_OPTION_FILE);
			scraperOptions.setPageSelector(pageSelector);
			
			// Real scraping process
			Scraper scraper = new Scraper(scraperOptions);
			scraper.scrape();
		} catch (Exception e) {
			e.printStackTrace(System.out);
		}
	}
}
