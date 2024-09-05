package gui.controllers;

import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import article.Article;
import article.ArticleSet;
import gui.Color;
import gui.WindowOpener;
import gui.backgroundtask.SearchTask;
import javafx.application.Platform;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;

/**
 * Main controller of the app
 */
public class MainPageController extends Controller {
	@FXML
	protected StackPane page_container;
	@FXML
	protected VBox item_container;
	@FXML
	protected TextField pagination;
	@FXML
	protected Label total_page;
	@FXML
	protected TextField search_bar;
	@FXML
	protected Pane info_btn;
	@FXML
	protected Pane trend_btn;
	@FXML
	protected Pane crawl_btn;
	@FXML
	protected Label prev_page_btn;
	@FXML
	protected Label next_page_btn;
	
	private int numberOfPage;
	private ArticleSet articleSet;
	
	public Tooltip createTooltip (String s) {
		Tooltip tooltip = new Tooltip(s);
		tooltip.setAutoHide(false);
		tooltip.setFont(new Font("Montserrat Bold", 13));
		tooltip.setStyle("-fx-background-color: " + Color.WHITE + ";"
				+ "-fx-text-fill: " + Color.BACKGROUND_WEAK + ";"
				+ "-fx-padding: 5px 10px 5px 10px");
		
		return tooltip;
	}
	
	private HBox createItemOutline (boolean lastItem) {
		HBox item = new HBox();
		item.setPadding(new Insets(12, 10, 12, 10));
		item.setPrefWidth(800);
		item.setCursor(Cursor.HAND);
		if (!lastItem) {
			item.setBorder(new Border(new BorderStroke(Paint.valueOf(Color.DARK_GREY), BorderStrokeStyle.SOLID,
					CornerRadii.EMPTY, new BorderWidths(0, 0, 0.5, 0))));
		}
		return item;
	}
	
	private void addHoverEffectOnItem (HBox item) {
		item.setOnMouseEntered(new EventHandler<MouseEvent>() {
			public void handle(MouseEvent event) {
				item.setBackground(
						new Background(new BackgroundFill(Paint.valueOf(Color.BACKGROUND_HOVER), null, null)));
			}
		});
		item.setOnMouseExited(new EventHandler<MouseEvent>() {
			public void handle(MouseEvent event) {
				item.setBackground(null);
			}
		});
	}
	
	private void addTooltipOnItem (HBox item, Article article) {
		Tooltip.install(item, createTooltip("Author: ".concat(String.join(", ", article.authors))
				.concat("\nPublish date time: ".concat(article.publishDate))));
	}
	
	private Label createIDLabel (int ID) {
		Label IDLabel = new Label(Integer.toString(ID));
		IDLabel.setTextFill(Paint.valueOf(Color.WHITE));
		IDLabel.setFont(new Font("Montserrat Regular", 14));
		IDLabel.setPrefWidth(70);
		IDLabel.setAlignment(Pos.CENTER_RIGHT);
		IDLabel.setPadding(new Insets(0, 20, 0, 0));
		return IDLabel;
	}
	
	private Label createTitleLabel (String title) {
		Label titleLabel = new Label(title);
		
		titleLabel.setTextFill(Paint.valueOf(Color.WHITE));
		titleLabel.setFont(new Font("Montserrat Bold", 15));
		titleLabel.setPrefWidth(800);
		
		return titleLabel;
	}
	
	private void addContentOnItem (HBox item, Article article) {
		Label IDLabel = createIDLabel(article.ownID);
		Label titleLabel = createTitleLabel(article.title);
		item.getChildren().addAll(IDLabel, titleLabel);
	}
	
	public HBox createItem(Article article, boolean isLastItem) {
		HBox item = createItemOutline(isLastItem);
		
		addHoverEffectOnItem(item);
		addTooltipOnItem (item, article);
		addContentOnItem(item, article);
		
		return item;
	}
	
	private void validatePageNumber () {
		try {
			Integer.parseInt(pagination.getText());
		} catch (NumberFormatException e) {
			pagination.setText("1");
		}
	}
	
	private void clearItemContainer () {
		item_container.getChildren().clear();
		item_container.setAlignment(Pos.CENTER_LEFT);
		item_container.setPadding(new Insets(0, 30, 0, 25));
	}
	
	private void sortSetByID (Vector<Article> set) {
		set.sort((Article a, Article b) -> a.ownID - b.ownID);
	}
	
	public void showArticleSet() {
		validatePageNumber();

		if (articleSet.size() == 0) {
			displaySearchError("No result found");
			return;
		}

		numberOfPage = (articleSet.size() + 9) / 10;
		total_page.setText("/    " + Integer.toString(numberOfPage));
		int currentPage = Integer.parseInt(pagination.getText());
		Vector<Article> currentSet = articleSet.content();
		
		sortSetByID(currentSet);
		clearItemContainer();
		
		for (int i = (currentPage - 1) * 10; i < currentPage * 10 && i < currentSet.size(); i++) {
			HBox item = createItem(currentSet.elementAt(i),
					(i == currentPage * 10 - 1 || i == currentSet.size() - 1 ? true : false));
			int t = i;
			item.setOnMouseClicked(new EventHandler<MouseEvent>() {
				public void handle(MouseEvent event) {
					WindowOpener.showArticleView(currentSet.elementAt(t), articleSet);
				}
			});
			item_container.getChildren().add(item);
		}
	}
	private void setEventHandlers() {
		search_bar.setOnKeyPressed(new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent ke) {
				if (ke.getCode().equals(KeyCode.ENTER)) {
					search(search_bar.getText());
				}
			}
		});
		setWindowDraggable();
	}
	
	
	
	private void searchEventRunning () {
		search_bar.setDisable(true);
		item_container.getChildren().clear();
		pagination.setFocusTraversable(false);

		Label loadingText = new Label("Searching...");
		loadingText.setFont(new Font("Montserrat Bold", 18));
		loadingText.setTextFill(Paint.valueOf(Color.WHITE));
		item_container.setAlignment(Pos.CENTER);
		item_container.getChildren().add(loadingText);
	}
	
	private void searchEventSucceeded () {
		search_bar.setDisable(false);
		showArticleSet();
	}
	
	private void searchEventFailed (WorkerStateEvent event) {
		search_bar.setDisable(false);
		displaySearchError(event.getSource().getMessage());
	}
	
	private SearchTask createSearchTask (String query) {
		SearchTask searcher = new SearchTask(this.articleSet, query);
		
		searcher.setOnRunning((e) -> {
			searchEventRunning();
		});
		searcher.setOnSucceeded((e) -> {
			searchEventSucceeded();
		});
		searcher.setOnFailed((e) -> {
			searchEventFailed(e);
		});
		
		return searcher;
	}
	
	private void performTask (SearchTask task) {
		ExecutorService executorService = Executors.newFixedThreadPool(1);
		executorService.execute(task);
		executorService.shutdown();
	}
	
	private void returnToFirstPage() {
		pagination.setText("1");
	}
	
	private void resetArticleSet () {
		articleSet.reset();
	}
	
	private void search(String query) {
		returnToFirstPage();
		resetArticleSet();
		
		if (query.isBlank()) {
			showArticleSet();
			return;
		}

		try {
			SearchTask searcher = createSearchTask(query);
			performTask(searcher);
			
		} catch (Exception e) {
			displaySearchError(e.getMessage());
		}
	}

	private void displaySearchError(String message) {
		Label loadingText = new Label(message);
		loadingText.setFont(new Font("Montserrat Bold", 18));
		loadingText.setTextFill(Paint.valueOf(Color.WHITE));
		item_container.setAlignment(Pos.CENTER);
		item_container.getChildren().clear();
		item_container.getChildren().add(loadingText);
	}
	
	public void showNextPage () {
		int currentPage = Integer.parseInt(pagination.getText());
		if (currentPage == numberOfPage)
			return;
		pagination.setText("" + (currentPage + 1));
		showArticleSet();
	}
	
	public void showPrevPage () {
		int currentPage = Integer.parseInt(pagination.getText());
		if (currentPage == 1)
			return;
		pagination.setText("" + (currentPage - 1));
		showArticleSet();
	}

	public void quit() {
		Platform.exit();
		System.exit(0);
	}

	public void hoverInfoButton() {
		info_btn.setBackground(new Background(new BackgroundFill(Paint.valueOf(Color.DARK_GREY), null, null)));
	}

	public void unhoverInfoButton() {
		info_btn.setBackground(null);
	}

	public void hoverTrendButton() {
		trend_btn.setBackground(
				new Background(new BackgroundFill(Paint.valueOf(Color.LIGHT_BLUE), new CornerRadii(100), null)));
	}

	public void unhoverTrendButton() {
		trend_btn.setBackground(
				new Background(new BackgroundFill(Paint.valueOf(Color.DARK_GREY), new CornerRadii(100), null)));
	}
	public void hoverCrawlButton() {
		crawl_btn.setBackground(
				new Background(new BackgroundFill(Paint.valueOf(Color.LIGHT_BLUE), new CornerRadii(100), null)));
	}

	public void unhoverCrawlButton() {
		crawl_btn.setBackground(
				new Background(new BackgroundFill(Paint.valueOf(Color.DARK_GREY), new CornerRadii(100), null)));
	}

	public void hoverPrevPage() {
		prev_page_btn.setTextFill(Paint.valueOf(Color.WHITE));
	}

	public void unhoverPrevPage() {
		prev_page_btn.setTextFill(Paint.valueOf(Color.LIGHT_GREY));
	}

	public void hoverNextPage() {
		next_page_btn.setTextFill(Paint.valueOf(Color.WHITE));
	}

	public void unhoverNextPage() {
		next_page_btn.setTextFill(Paint.valueOf(Color.LIGHT_GREY));
	}

	public void clickExitButton() {
		Platform.exit();
		System.exit(0);
	}

	public void clickNextPage() {
		showNextPage();
	}

	public void clickPrevPage() {
		showPrevPage();
	}

	public void clickTrendButton() {
		String query = search_bar.getText();
		WindowOpener.showTrendPage(query.isBlank() ? "whole set" : query, this.articleSet);
	}

	public void clickInfoButton() {
		WindowOpener.showAboutPage();
	}
	
	public void clickCrawlButton() {
		WindowOpener.showCrawlPage(this.articleSet);
	}

	public void initialize() {
		articleSet = new ArticleSet();
		showArticleSet();
		Platform.runLater(() -> item_container.requestFocus());
		setEventHandlers();
	}
}