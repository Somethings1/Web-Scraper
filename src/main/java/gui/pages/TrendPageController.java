package gui.pages;

import java.util.Vector;

import article.ArticleSet;
import gui.Color;
import gui.Helper;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import javafx.util.Pair;

public class TrendPageController {
	private double xOffset = 0;
	private double yOffset = 0;
	private ArticleSet articleSet;
	String query;

	@FXML
	HBox title_bar;
	@FXML
	Label label_title;
	@FXML
	ScrollPane scroll_pane;
	@FXML
	Pane exit_btn;
	@FXML
	Label query_label;
	@FXML
	VBox item_container;
	
	public void hoverExitButton() {
		exit_btn.setBackground(new Background(new BackgroundFill(Paint.valueOf(Color.DARK_RED), null, null)));
	}

	public void unhoverExitButton() {
		exit_btn.setBackground(null);
	}

	public void clickExitButton() {
		exit_btn.getScene().getWindow().hide();
	}
	
	public HBox createItem (String word, Integer thisValue, Integer maxValue) {
		HBox item = new HBox();
		item.setPadding(new Insets(10, 30, 10, 30));
		
		Label description = new Label(word);
		Pane graphic = new Pane();
		Label value = new Label(thisValue.toString());
		description.setFont(new Font("Montserrat Bold", 15));
		description.setPrefWidth(130);
		description.setAlignment(Pos.CENTER_RIGHT);
		description.setTextFill(Paint.valueOf(Color.WHITE));
		
		graphic.setBackground(new Background(new BackgroundFill(Paint.valueOf(Color.LIGHT_BLUE), null, null)));
		graphic.setPrefWidth(650 * thisValue / maxValue);
		
		value.setFont(new Font("Montserrat Bold", 15));
		value.setTextFill(Paint.valueOf(Color.WHITE));
		
		item.getChildren().addAll(description, graphic, value);
		HBox.setMargin(graphic, new Insets(0, 20, 0, 20));
		return item;
	}
	
	public void showGraph (Vector<Pair<String, Integer>> wordList) {
		int lastItem = Math.min(50, wordList.size());
		
		for (int i = 0; i < lastItem; i++) {
			HBox item = createItem(wordList.elementAt(i).getKey(),
								   wordList.elementAt(i).getValue(),
								   wordList.elementAt(0).getValue());
			item_container.getChildren().add(item);
		}
	}
	
	public void initData (String query, ArticleSet articleSet) {
		query_label.setText(query);
		initStyle();
		this.articleSet = articleSet;	
		Platform.runLater(new Runnable () {
			@Override
			public void run () {
				showGraph(Helper.getWordList(articleSet));
			}
		});
	}
	
	public void initStyle () {
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
	}
	
	public void initialize () {
		
	}
}
