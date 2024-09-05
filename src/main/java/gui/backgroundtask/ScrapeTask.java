package gui.backgroundtask;

import java.util.Vector;

import article.Article;
import article.ArticleSet;
import javafx.concurrent.Task;
import scraping.ScrapingController;
import scraping.settings.PageSelector;
import scraping.settings.ScraperOptions;

public class ScrapeTask extends Task<Integer> {
	private ArticleSet articleSet;
	private ScrapingController scrapingController;

	public ScrapeTask(ArticleSet set) {
		this.articleSet = set;

		try {
			// Set page selector
			PageSelector pageSelector = new PageSelector();
			pageSelector.setByJSONFile();

			// Create options for scraper
			ScraperOptions scraperOptions = new ScraperOptions();
			scraperOptions.setByJSONFile();
			scraperOptions.setPageSelector(pageSelector);

			// Create scraper
			scrapingController = new ScrapingController(scraperOptions, articleSet);

			// Add visited links
			Vector<String> visited = new Vector<String>();
			for (Article article : articleSet.content()) {
				visited.add(article.link);
			}
			scrapingController.addToVisitedList(visited);
		} catch (Exception e) {
			updateMessage("Error");
		}
	}

	@Override
	protected Integer call() throws Exception {
		scrapingController.scrape();
		return 1;
	}

}
