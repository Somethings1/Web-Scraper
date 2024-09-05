package scraping;

import java.util.Collection;
import java.util.HashSet;
import java.util.Vector;

import article.Article;
import article.ArticleController;
import article.ArticleSet;
import scraping.settings.ScraperOptions;

/**
 * <b>Scraper</b><br>
 * Provide proxy list int <code>prx.txt</code>
 */
public class ScrapingController {

	protected final ScraperOptions scraperOptions;

	private HashSet<String> visitedURL = new HashSet<String>();

	private HashSet<String> newURL = new HashSet<String>();

	protected ArticleSet articleSet;

	public ScrapingController(ScraperOptions scraperOptions, ArticleSet articleSet) 
			throws Exception {
		ArticleController.beginIDCount();
		this.scraperOptions = scraperOptions;
		this.articleSet = articleSet;
	}

	protected synchronized void reopenThread(HashSet<String> _newURL) throws Exception {
		// If there's still a thread running, just add new URLs to the set
		Debugger.yell(String.valueOf(_newURL.size()));
		Debugger.yell(String.valueOf(SingleThreadScraper.threadCounter));
		newURL.addAll(_newURL);
		if (SingleThreadScraper.threadCounter != 0)
			return;

		Debugger.yell("All threads closed.");
		// Distribute next URLs to the threads evenly
		Vector<String> nextURL = new Vector<String>(newURL);

		Debugger.yell("New URL count: " + nextURL.size());
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
		Debugger.yell("Total link: " + nextURL.size());

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


	public void addToVisitedList(Collection<String> s) {
		this.visitedURL.addAll(s);
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
