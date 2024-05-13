package gui;

import java.util.Vector;

import article.Article;
import article.ArticleSet;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import scraping.PageSelector;
import scraping.Scraper;
import scraping.ScraperOptions;

public class Main extends Application {		
	private static void callScraper ()  {
		try {
			// Get articleSet
			ArticleSet articleSet = new ArticleSet();
			
			// Set page selector
			PageSelector pageSelector = new PageSelector();
			pageSelector.setByJSONFile();

			// Create options for scraper
			ScraperOptions scraperOptions = new ScraperOptions();
			scraperOptions.setByJSONFile();
			scraperOptions.setPageSelector(pageSelector);

			// Create scraper
			Scraper scraper = new Scraper(scraperOptions, articleSet);

			// Add visited links
			Vector<String> visited = new Vector<String>();
			for (Article article : articleSet.content()) {
				visited.add(article.link);
			}
			scraper.addToVisitedList(visited);
			
			scraper.scrape();
		} catch (Exception e) {
			
		}
	}
	
	public static void main(String[] args) {
		//reset();
		launch(args);
		//callScraper();
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		Parent root = FXMLLoader.load(getClass().getResource("index.fxml"));
        primaryStage.setTitle("Blockchain news");
        primaryStage.setScene(new Scene(root, 993, 673));
        primaryStage.initStyle(StageStyle.UNDECORATED);
        primaryStage.show();
	}
}
