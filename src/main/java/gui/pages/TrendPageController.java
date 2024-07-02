package gui.pages;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Vector;

import article.Article;
import article.ArticleSet;
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
import javafx.stage.Stage;
import javafx.util.Pair;

public class TrendPageController {
	private double xOffset = 0;
	private double yOffset = 0;
	String query;
	private static final String FILLER_WORD_FILE_NAME = "material" + File.separator + "filler-words.info";

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
	
	/**
	 * Read filler words in file FILLER_WORD_FILE_NAME
	*/
	private HashSet<String> getFillerWords () {
		HashSet<String> result = new HashSet<String>();
		
		try {
			File file = new File(FILLER_WORD_FILE_NAME);
			Scanner scanner = new Scanner(file);
			
			String[] words = scanner.nextLine().split(",");
			for (String word: words) result.add(word);
			
			scanner.close();
		}
		catch (IOException e) {
			
		}
		
		return result;
	}
	
	/**
	 * Support for trend finding<br>update filler word list in material/filler-words.info
	 * @param _articleSet the ArticleSet to search for words in it
	 * @return a list of word (or word combination) and its frequency
	*/
	private Vector<Pair<String, Integer>> getWordList (ArticleSet _articleSet) {
		HashMap<String, Integer> words = new HashMap<String, Integer>();
		Vector<Article> articleSet = _articleSet.content();
		for (Article article: articleSet) {
			for (String para: article.content) {
				String wordsInPara[] = para.split(" ");
				Vector<String> realWordList = new Vector<String>();
				for (String word: wordsInPara) {
					boolean isRealWord = true;
					for (int i = 0; i < word.length(); i++) {
						if (word.charAt(i) == '\'') {
							word = word.substring(0, i);
							break;
						}
						if (word.charAt(i) == '\"') {
							word = String.join("", word.split("\""));
							continue;
						}
						if (word.charAt(i) == '.' || word.charAt(i) == ',' || word.charAt(i) == '?' || word.charAt(i) == '!') {
							word = word.substring(0, word.length() - 1);
							continue;
						}
						if (word.charAt(i) < 'A' || (word.charAt(i) > 'Z' && word.charAt(i) < 'a') || word.charAt(i) > 'z') {
							isRealWord = false;
							break;
						}
					}
					if (isRealWord && !word.isBlank()) realWordList.add(word.toLowerCase());
				}
				for (int i = 0; i < realWordList.size(); i++) {
					String s = realWordList.elementAt(i);
				
					if (words.containsKey(s)) words.replace(s, words.get(s) + 1);
					else words.put(s, 1);
				}
			}
		}
		
		HashSet<String> fillerWords = getFillerWords();
		for (String word: fillerWords) {
			words.remove(word);
		}
		
		Vector<Pair<String, Integer>> wordList = new Vector<>();
		for (HashMap.Entry<String, Integer> entry: words.entrySet()) {
			wordList.add(new Pair<String, Integer>(entry.getKey(), entry.getValue()));
		}
		wordList.sort((a, b) -> b.getValue() - a.getValue());
		
		return wordList;
	}
	
	public void initData (String query, ArticleSet articleSet) {
		query_label.setText(query);
		initStyle();
		Platform.runLater(new Runnable () {
			@Override
			public void run () {
				showGraph(getWordList(articleSet));
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
