package gui;

import java.io.File;
import java.io.IOException;

import article.Article;
import article.ArticleSet;
import gui.controllers.ArticleViewController;
import gui.controllers.ScrapingPageController;
import gui.controllers.TrendPageController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class WindowOpener {
	public static void showMainPage (Stage primaryStage) throws IOException {
		Parent root = FXMLLoader.load(WindowOpener.class.getResource("pages/index.fxml"));
        primaryStage.setTitle("Blockchain news");
        primaryStage.setScene(new Scene(root, 993, 673));
        primaryStage.initStyle(StageStyle.UNDECORATED);
        primaryStage.show();
	}
	
	public static void showAboutPage () {
		try {
			FXMLLoader loader = new FXMLLoader(WindowOpener.class.getResource("pages/about_page.fxml"));

			Stage stage = new Stage();
			stage.setScene(new Scene(loader.load()));
			stage.initStyle(StageStyle.UNDECORATED);

			stage.show();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void showCrawlPage (ArticleSet set) {
		try {
			FXMLLoader loader = new FXMLLoader(WindowOpener.class.getResource("pages/crawl_page.fxml"));

			Stage stage = new Stage();
			Scene scene = new Scene(loader.load());
			scene.getStylesheets().add("gui" + File.separator + "pages" + File.separator + "crawl-page.css");
			stage.setScene(scene);
			
			stage.initStyle(StageStyle.UNDECORATED);
			
			ScrapingPageController controller = loader.getController();
			controller.initData(set);

			stage.show();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void showTrendPage (String query, ArticleSet articleSet) {
		try {
			FXMLLoader loader = new FXMLLoader(WindowOpener.class.getResource("pages/trend_page.fxml"));

			Stage stage = new Stage();
			stage.setScene(new Scene(loader.load()));
			stage.initStyle(StageStyle.UNDECORATED);
			
			TrendPageController controller = loader.getController();
			controller.initData(query, new ArticleSet(articleSet, true));

			stage.show();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void showArticleView(Article article, ArticleSet ...articleSet) {
		try {
			FXMLLoader loader = new FXMLLoader(WindowOpener.class.getResource("pages/article_view.fxml"));

			Stage stage = new Stage();
			stage.setTitle(article.title);
			stage.setScene(new Scene(loader.load()));
			stage.initStyle(StageStyle.UNDECORATED);

			ArticleViewController controller = loader.getController();
			controller.initData(article, articleSet);

			stage.show();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
