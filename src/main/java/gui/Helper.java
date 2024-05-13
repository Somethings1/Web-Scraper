package gui;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Scanner;
import java.util.Vector;

import org.apache.commons.text.StringEscapeUtils;

import article.Article;
import article.ArticleSet;
import article.Entity;
import gui.pages.ArticleViewController;
import gui.pages.CrawlPageController;
import gui.pages.TrendPageController;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Pair;

public class Helper {
	private static final int MAXIMUM_LINE_IN_TEXTAREA = 1000;
	private static final String FILLER_WORD_FILE_NAME = "material" + File.separator + "filler-words.info";
	
	/**
	 * Custom tooltip by given text
	 * @return javafx.scene.control.Tooltip corresponding to given text 
	*/
	public static Tooltip createTooltip (String s) {
		Tooltip tooltip = new Tooltip(s);
		tooltip.setAutoHide(false);
		tooltip.setFont(new Font("Montserrat Bold", 13));
		tooltip.setStyle("-fx-background-color: " + Color.WHITE + ";"
				+ "-fx-text-fill: " + Color.BACKGROUND_WEAK + ";"
				+ "-fx-padding: 5px 10px 5px 10px");
		
		return tooltip;
	}
	
	/**
	 * Beautify and escape HTML entities
	 * @param s the String to be modified
	 * @return modified string
	*/
	public static String modifyString (String s) {
		s = s.replaceAll("<[^>]*>", "");
		s = StringEscapeUtils.unescapeHtml4(s);
		s = s.replaceAll("\n", "");
		s = s.replaceAll("\t", "");
		s = s.replaceAll("&#x27;", "\'");
		s = s.trim();
		for (int i = 0; i < s.length() - 1; i++)
			if (s.charAt(i) == ' ' && s.charAt(i + 1) == ' ') {
				s = s.substring(0, i).concat(s.substring(i + 1));
				i--;
			}
				
		return s;
	}
	
	/**
	 * Separate object and normal text in a paragraph
	 * @param para paragraph to separate
	 * @param article the Article corresponding to the para to search for Entity
	 * @return LinkedList of Pair(text, object type)
	*/
	public static LinkedList<Pair<String, String>> objectSeparator (String para, Article article) {
		LinkedList<Pair<String, String>> subParas = new LinkedList<>();
		subParas.add(new Pair<String, String>(para, "none"));
		for (Entity entity: article.entities) {
			LinkedList<Pair<String, String>> newList = new LinkedList<>();
			for (int i = 0; i < subParas.size(); i++) {
				// if that segment is already an entity
				if (!subParas.get(i).getValue().equals("none")) {
					newList.add(subParas.get(i));
					continue;
				}
				
				int next = 0, prev = 0;
				String tmp = subParas.get(i).getKey();
				next = tmp.indexOf(entity.content, next);
				
				while(next >= 0) {
					if (next != 0) newList.add(new Pair<String, String>(tmp.substring(prev, next), "none"));
					newList.add(new Pair<String, String>(tmp.substring(next, next + entity.content.length()), entity.type));
					
					prev = next + entity.content.length();
					next = tmp.indexOf(entity.content, prev + 1);
				}
				
				newList.add(new Pair<String, String>(tmp.substring(prev), "none"));
			}
			subParas.clear();
			subParas.addAll(newList);
		}
		
		return subParas;
	}
	
	/**
	 * Read filler words in file FILLER_WORD_FILE_NAME
	*/
	private static HashSet<String> getFillerWords () {
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
	public static Vector<Pair<String, Integer>> getWordList (ArticleSet _articleSet) {
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

	/**
	 * Open article view window
	 * @param article article to be shown
	 * @param articleSet the set to search for related articles
	*/
	public static void showArticleView(Article article, ArticleSet ...articleSet) {
		try {
			FXMLLoader loader = new FXMLLoader(Helper.class.getResource("pages/article_view.fxml"));

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
	
	/**
	 * Open "About" window, which provides informations and instruction to use the app
	*/
	public static void showAboutPage () {
		try {
			FXMLLoader loader = new FXMLLoader(Helper.class.getResource("pages/about_page.fxml"));

			Stage stage = new Stage();
			stage.setScene(new Scene(loader.load()));
			stage.initStyle(StageStyle.UNDECORATED);

			stage.show();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Open "Crawl" windows, which allows user to scrape more articles from the internet.
	 * User can configure preferences of the scraper in a friendly way.
	*/
	public static void showCrawlPage (ArticleSet set) {
		try {
			FXMLLoader loader = new FXMLLoader(Helper.class.getResource("pages/crawl_page.fxml"));

			Stage stage = new Stage();
			Scene scene = new Scene(loader.load());
			scene.getStylesheets().add("gui" + File.separator + "pages" + File.separator + "crawl-page.css");
			stage.setScene(scene);
			
			stage.initStyle(StageStyle.UNDECORATED);
			
			CrawlPageController controller = loader.getController();
			controller.initData(set);

			stage.show();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Open a new window to represent the trend analyzed from the search result
	 * @param query the query input in search
	 * @param articleSet the article set to analyze and show result
	*/
	public static void showTrendPage (String query, ArticleSet articleSet) {
		try {
			FXMLLoader loader = new FXMLLoader(Helper.class.getResource("pages/trend_page.fxml"));

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
	
	/**
	 * Create a label with given text, with font Montserrat Regular
	 * @param s the content of the label
	 * @param size the size of Label in pixel
	 * @return the label with given text and size
	*/
	public static Label createNormalText (String s, int size) {
		Label res = new Label(s);
		
		res.setFont(new Font("Montserrat Regular", size));
		res.setTextFill(Paint.valueOf(Color.WHITE));
		
		return res;
	}
	
	/**
	 * Create a label with given text, with font Montserrat Bold and size of 32
	 * @param s the content of the label
	 * @return the label - title - with given text
	*/
	public static Label createTitle (String s) {
		Label res = new Label(s);
		
		res.setFont(new Font("Montserrat Bold", 32));
		res.setTextFill(Paint.valueOf(Color.WHITE));
		res.setAlignment(Pos.CENTER);
		res.setPadding(new Insets(30, 0, 0, 0));
		
		return res;
	}
	
	/**
	 * Create a normal text field
	 * @param width the width of text field
	 * @return a TextField with given width
	*/
	public static TextField createTextField (int width) {
		TextField field = new TextField();
		
		field.setStyle("-fx-text-fill: " + Color.WHITE + ";"
				+ "-fx-border-color: " + Color.LIGHT_GREY + ";"
				+ "-fx-border-radius: 10px");
		field.setBackground(null);
		field.setFont(new Font("Montserrat Regular", 15));
		field.setMinWidth(width);
		
		return field;
	}
	
	/**
	 * Create a text area with a height of 300 pixels.
	 * <b>IMPORTANT!</b> this text area auto delete the first line
	 * when it's more than MAXIMUM_LINE_IN_TEXTAREA line 
	 * @param width the width of the area
	 * @return a TextArea of size (300, width)
	*/
	public static TextArea createTextArea (int width) {
		TextArea field = new TextArea() {
			@Override
            public void replaceText(int start, int end, String text) {
                super.replaceText(start, end, text);
                while(getText().split("\n", -1).length > MAXIMUM_LINE_IN_TEXTAREA) {
                    int fle = getText().indexOf("\n");
                    super.replaceText(0, fle+1, "");
                }
                positionCaret(getText().length());
            }
		};
		field.setStyle("-fx-text-fill: white;"
					 + "-fx-background-color: transparent;"
					 + "-fx-control-inner-background: " + Color.BACKGROUND_MEDIUM + ";"
					 + "-fx-border-color: " + Color.WHITE + ";");
		field.setMaxWidth(width);
		field.setMinHeight(300);
		field.setFont(new Font("Montserrat", 15));
		field.widthProperty().addListener((o) -> {
			Node vp = field.lookup(".content");
			vp.setStyle("-fx-background-color: " + Color.BACKGROUND_MEDIUM + ";"
					  + "-fx-text-fill: white;");
		});
		return field;
	}
	
	/**
	 * Create a normal drop down menu
	 * @param s a Collection of String to be put to the menu
	 * @return a ComboBox with items the given String
	*/
	public static ComboBox createDropdown (String ...s) {
		ComboBox box = new ComboBox();
		for (String t: s) box.getItems().add(t);
		return box;
	}
	
	
}
