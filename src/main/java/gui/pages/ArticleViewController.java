package gui.pages;

import java.util.LinkedList;
import java.util.Vector;

import article.Article;
import article.ArticleSet;
import gui.Color;
import gui.Helper;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.util.Pair;

public class ArticleViewController extends Application {

	private Article article;
	private ArticleSet articleSet;
	private final int CONTAINER_WIDTH = 735;
	private final int RELATED_WIDTH = 245;
	private boolean showObject = false;
	private double xOffset = 0;
	private double yOffset = 0;

	@FXML
	HBox title_bar;
	@FXML
	Label label_title;
	@FXML
	Label label_author;
	@FXML
	Label label_site;
	@FXML
	Label label_date;
	@FXML
	TextFlow content_container;
	@FXML
	TextFlow tag_container;
	@FXML
	VBox related_container;
	@FXML
	VBox main;
	@FXML
	ScrollPane scroll_pane;
	@FXML
	Pane exit_btn;
	@FXML
	Pane navigate_btn;
	@FXML
	Pane object_toggle;
	@FXML
	ImageView object_icon;

	private void initStyle() {
		label_title.setWrapText(true);
		label_title.setTextFill(Paint.valueOf(Color.WHITE));
		label_title.prefWidthProperty().bind(main.widthProperty());

		label_author.setTextFill(Paint.valueOf(Color.WHITE));
		label_site.setTextFill(Paint.valueOf(Color.WHITE));
		label_date.setTextFill(Paint.valueOf(Color.WHITE));
		
		scroll_pane.widthProperty().addListener((o) -> {
			Node vp = scroll_pane.lookup(".viewport");
			vp.setStyle("-fx-background-color: transparent");
			vp = scroll_pane.lookup(":vertical");
			vp.setStyle("-fx-background-color: transparent");
		});
		scroll_pane.setStyle("-fx-background-color: transparent");
		scroll_pane.setFitToWidth(true);
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

		content_container.setPrefWidth(CONTAINER_WIDTH);
		content_container.setPadding(new Insets(0, 25, 0, 30));

		related_container.setPadding(new Insets(15));
	}

	private void showTag() {
		tag_container.getChildren().clear();
		tag_container.setPadding(new Insets(5, 0, 5, 30));
		Label category = new Label(article.category.elementAt(0));
		category.setStyle("-fx-background-color: " + Color.BLUE + ";" + "-fx-background-radius: 5px;"
				+ "-fx-font-family: Montserrat;" + "-fx-text-fill: " + Color.WHITE);
		category.setPadding(new Insets(5, 10, 5, 10));
		category.setTooltip(new Tooltip("Category"));
		tag_container.getChildren().add(category);

		for (String hashtag : article.hashtag) {
			Label space = new Label("    ");
			tag_container.getChildren().add(space);

			Label tag = new Label(hashtag.startsWith("#") ? hashtag : "#".concat(hashtag));
			tag.setStyle("-fx-background-color: " + Color.WHITE + ";" + "-fx-background-radius: 5px;"
					+ "-fx-font-family: Montserrat;" + "-fx-text-fill: " + Color.DARK_GREY);
			tag.setPadding(new Insets(5, 10, 5, 10));
			tag.setTooltip(new Tooltip("Hashtag"));

			tag_container.getChildren().add(tag);
		}

	}

	private void showArticle() {
		label_title.setText("#" + article.ownID + ": " + article.title);
		label_author.setText(String.join(", ", article.authors));
		label_site.setText(article.webName);
		label_date.setText(article.publishDate);
		content_container.getChildren().clear();

		showTag();
		showContent();
		showSimilar();
	}

	private void showSimilar() {
		Vector<Article> current = this.articleSet.content();
		related_container.getChildren().clear();
		for (int i = 0; i < 11 && i < current.size(); i++) {
			Article currentArticle = current.elementAt(i);
			if (currentArticle.ownID == article.ownID)
				continue;

			Label title = new Label("#" + currentArticle.ownID + ": " + currentArticle.title + "\n");
			title.setTextFill(Paint.valueOf(Color.DARK_GREY));
			title.setWrapText(true);
			title.setMaxWidth(RELATED_WIDTH - 30);
			title.setMaxHeight(Label.USE_COMPUTED_SIZE);
			title.setFont(new Font("Montserrat Bold", 15));
			title.setCursor(Cursor.HAND);
			VBox.setMargin(title, new Insets(0, 0, 20, 0));

			title.setOnMouseEntered(new EventHandler<MouseEvent>() {
				public void handle(MouseEvent e) {
					title.setTextFill(Paint.valueOf(Color.LIGHT_GREY));
				}
			});

			title.setOnMouseExited(new EventHandler<MouseEvent>() {
				public void handle(MouseEvent e) {
					title.setTextFill(Paint.valueOf(Color.DARK_GREY));
				}
			});

			title.setOnMouseClicked(new EventHandler<MouseEvent>() {
				public void handle(MouseEvent e) {
					articleSet.reset();
					initData(currentArticle, articleSet);
				}
			});

			related_container.getChildren().add(title);
		}
	}

	private void showContent() {
		for (String s : article.content) {
			content_container.getChildren().add(new Text("\n"));
			LinkedList<Pair<String, String>> subParas = Helper.objectSeparator(s, article);

			for (int i = 0; i < subParas.size(); i++) {
				String content = subParas.get(i).getKey();
				String type = subParas.get(i).getValue();
				Text text = new Text(content);

				text.setFont(new Font("Montserrat Regular", 15));
				text.setFill(Paint.valueOf(Color.WHITE));
				if (!type.equals("none") && showObject) {
					text.setUnderline(true);
					text.setFill(Paint.valueOf(Color.LIGHT_BLUE));
					text.setFont(new Font("Montserrat Bold", 15));

					Tooltip tooltip = Helper.createTooltip(type);
					tooltip.setShowDelay(Duration.ZERO);

					Tooltip.install(text, tooltip);
				}
				content_container.getChildren().add(text);

			}
			content_container.getChildren().add(new Text("\n"));
		}
	}

	public void hoverExitButton() {
		exit_btn.setBackground(new Background(new BackgroundFill(Paint.valueOf(Color.DARK_RED), null, null)));
	}

	public void unhoverExitButton() {
		exit_btn.setBackground(null);
	}

	public void clickExitButton() {
		exit_btn.getScene().getWindow().hide();
	}

	public void hoverNavigateButton() {
		navigate_btn.setBackground(new Background(new BackgroundFill(Paint.valueOf(Color.DARK_GREY), null, null)));
	}

	public void unhoverNavigateButton() {
		navigate_btn.setBackground(null);
	}

	public void hoverObjectButton() {
		object_toggle.setBackground(new Background(new BackgroundFill(Paint.valueOf(Color.DARK_GREY), null, null)));
	}

	public void unhoverObjectButton() {
		object_toggle.setBackground(null);
	}

	public void clickNavigateButton() {
		getHostServices().showDocument(article.link);
	}

	public void toggleEntity() {
		if (showObject) {
			object_icon.setImage(new Image("file:material/object-off.png"));
			showObject = false;
		} else {
			object_icon.setImage(new Image("file:material/object-on.png"));
			showObject = true;
		}
		showArticle();
	}

	public void initData(Article article, ArticleSet set) {
		this.article = article;
		this.articleSet = new ArticleSet(set, true);
		this.articleSet.filterBySimilarEntity(article);
		showArticle();
		initStyle();
	}
	
	@Override
	public void start(Stage arg0) throws Exception {
		
	}
}
