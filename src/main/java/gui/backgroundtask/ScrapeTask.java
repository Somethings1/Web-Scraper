package gui.backgroundtask;

import java.util.Vector;

import article.Article;
import article.ArticleSet;
import javafx.concurrent.Task;
import scraping.PageSelector;
import scraping.Scraper;
import scraping.ScraperOptions;

public class ScrapeTask extends Task<Integer> {
	private ArticleSet articleSet;
	private Scraper scraper;

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
			scraper = new Scraper(scraperOptions, articleSet);

			// Add visited links
			Vector<String> visited = new Vector<String>();
			for (Article article : articleSet.content()) {
				visited.add(article.link);
			}
			scraper.addToVisitedList(visited);
		} catch (Exception e) {
			updateMessage("Error");
		}
	}

	@Override
	protected Integer call() throws Exception {
		scraper.scrape();
		return 1;
	}

}
