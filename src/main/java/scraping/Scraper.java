package scraping;

import java.util.Collection;
import java.util.HashSet;
import java.util.Vector;

import article.Article;
import article.ArticleSet;
import gui.pages.CrawlPageController;
import javafx.application.Platform;

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
