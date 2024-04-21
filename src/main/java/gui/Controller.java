package gui;

import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import article.Article;
import article.ArticleSet;
import gui.backgroundtask.SearchTask;
import javafx.application.Platform;
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
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.stage.Stage;

public class Controller {
	@FXML private Pane exit_btn;
	@FXML private StackPane page_container;
	@FXML private VBox item_container;
	@FXML private TextField pagination;
	@FXML private Label total_page;
	@FXML private TextField search_bar;
	@FXML private Pane info_btn;
	@FXML private Pane trend_btn;
	@FXML private HBox title_bar;
	@FXML private Label prev_page_btn;
	@FXML private Label next_page_btn;
	private int numberOfPage;
	private ArticleSet articleSet;
	private double xOffset = 0;
	private double yOffset = 0;
	
	public void quit() {
		Platform.exit();
		System.exit(0);
	}
	
	public void hoverExitButton() {
		exit_btn.setBackground(new Background(new BackgroundFill(Paint.valueOf(Color.DARK_RED), null, null)));
	}

	public void unhoverExitButton() {
		exit_btn.setBackground(null);
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
	
	public void hoverPrevPage () {
		prev_page_btn.setTextFill(Paint.valueOf(Color.WHITE));
	}
	
	public void unhoverPrevPage () {
		prev_page_btn.setTextFill(Paint.valueOf(Color.LIGHT_GREY));
	}
	
	public void hoverNextPage () {
		next_page_btn.setTextFill(Paint.valueOf(Color.WHITE));
	}
	
	public void unhoverNextPage () {
		next_page_btn.setTextFill(Paint.valueOf(Color.LIGHT_GREY));
	}
	
	public void clickExitButton() {
		Platform.exit();
		System.exit(0);
	}

	public void clickNextPage() {
		int currentPage = Integer.parseInt(pagination.getText());
		if (currentPage == numberOfPage)
			return;
		pagination.setText("" + (currentPage + 1));
		showArticleSet();
	}

	public void clickPrevPage() {
		int currentPage = Integer.parseInt(pagination.getText());
		if (currentPage == 1)
			return;
		pagination.setText("" + (currentPage - 1));
		showArticleSet();
	}
	public void clickTrendButton() {
		String query = search_bar.getText();
		Helper.showTrendPage(query.isBlank() ? "whole set" : query, articleSet);
	}
	public void clickInfoButton() {
		Helper.showAboutPage();
	}
	
	public HBox createItem(Article article, boolean lastItem) {
		// Create the container
		HBox item = new HBox();
		item.setPadding(new Insets(12, 10, 12, 10));
		item.setPrefWidth(800);
		item.setCursor(Cursor.HAND);
		if (!lastItem) {
			item.setBorder(new Border(new BorderStroke(Paint.valueOf(Color.DARK_GREY), BorderStrokeStyle.SOLID,
					CornerRadii.EMPTY, new BorderWidths(0, 0, 0.5, 0))));
		}

		// Add events
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
		

		// add tooltip
		Tooltip.install(item, Helper.createTooltip("Author: ".concat(String.join(", ", article.authors))
				.concat("\nPublish date time: ".concat(article.publishDate))));

		// Create the labels
		Label ID = new Label();
		Label label = new Label();
		ID.setText(Integer.toString(article.ownID));
		label.setText(article.title);

		// Set style for items
		label.setTextFill(Paint.valueOf(Color.WHITE));
		label.setFont(new Font("Montserrat Bold", 15));
		label.setPrefWidth(800);
		ID.setTextFill(Paint.valueOf(Color.WHITE));
		ID.setFont(new Font("Montserrat Regular", 14));
		ID.setPrefWidth(70);
		ID.setAlignment(Pos.CENTER_RIGHT);
		ID.setPadding(new Insets(0, 20, 0, 0));

		item.getChildren().add(ID);
		item.getChildren().add(label);
		return item;
	}
	private void displaySearchError(String message) {
		Label loadingText = new Label(message);
		loadingText.setFont(new Font("Montserrat Bold", 18));
		loadingText.setTextFill(Paint.valueOf(Color.WHITE));
		item_container.setAlignment(Pos.CENTER);
		item_container.getChildren().add(loadingText);
	}

	public void showArticleSet() {
		try {
			Integer.parseInt(pagination.getText());
		} catch (NumberFormatException e) {
			pagination.setText("1");
		}

		if (articleSet.size() == 0) {
			displaySearchError("No result found");
			return;
		}

		numberOfPage = (articleSet.size() + 9) / 10;
		total_page.setText("/    " + Integer.toString(numberOfPage));
		int page = Integer.parseInt(pagination.getText());
		Vector<Article> currentSet = articleSet.content();
		currentSet.sort((Article a, Article b) -> a.ownID - b.ownID);

		item_container.getChildren().clear();
		item_container.setAlignment(Pos.CENTER_LEFT);
		item_container.setPadding(new Insets(0, 30, 0, 25));

		for (int i = (page - 1) * 10; i < page * 10 && i < currentSet.size(); i++) {
			HBox item = createItem(currentSet.elementAt(i), (i == page * 10 - 1 || i == currentSet.size() - 1 ? true : false));
			int t = i;
			item.setOnMouseClicked(new EventHandler<MouseEvent>() {
				public void handle(MouseEvent event) {
					Helper.showArticleView(currentSet.elementAt(t), articleSet);
				}
			});
			item_container.getChildren().add(item);
		}
	}

	public void search(String query) {
		// Return to page 1
		pagination.setText("1");
		articleSet.reset();
		if (query.length() == 0) {
			articleSet.reset();
			showArticleSet();
			return;
		}

		try {
			SearchTask searcher = new SearchTask(this.articleSet, query);
			searcher.setOnRunning((succeesesEvent) -> {
				search_bar.setDisable(true);
				item_container.getChildren().clear();
				pagination.setFocusTraversable(false);

				Label loadingText = new Label("Searching...");
				loadingText.setFont(new Font("Montserrat Bold", 18));
				loadingText.setTextFill(Paint.valueOf(Color.WHITE));
				item_container.setAlignment(Pos.CENTER);
				item_container.getChildren().add(loadingText);
			});
			searcher.setOnSucceeded((succeededEvent) -> {
				search_bar.setDisable(false);
				showArticleSet();
			});
			searcher.setOnFailed((event) -> {
				displaySearchError(event.getSource().getMessage());
			});
			ExecutorService executorService = Executors.newFixedThreadPool(1);
			executorService.execute(searcher);
			executorService.shutdown();
		} catch (Exception e) {
			displaySearchError(e.getMessage());
			return;
		}
	}	
	private void setEventHandlers () {
		search_bar.setOnKeyPressed(new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent ke) {
				if (ke.getCode().equals(KeyCode.ENTER)) {
					search(search_bar.getText());
				}
			}
		});

		title_bar.setOnMousePressed(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				xOffset = event.getSceneX();
				yOffset = event.getSceneY();
			}
		});
		title_bar.setOnMouseDragged(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				((Stage) (title_bar.getScene().getWindow())).setX(event.getScreenX() - xOffset);
				((Stage) (title_bar.getScene().getWindow())).setY(event.getScreenY() - yOffset);
			}
		});
	}

	public void initialize() {
		articleSet = new ArticleSet();
		showArticleSet();
		Platform.runLater( () -> item_container.requestFocus() );
		setEventHandlers();	
	}
}