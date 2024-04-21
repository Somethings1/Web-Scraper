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
import gui.pages.TrendPageController;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Pair;

public class Helper {
	
	/**
	 * Custom tooltip by given text
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
	 * Return LinkedList<Pair<String, String>> </br>
	 * of Pair(text, object type)
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
	
	private static HashSet<String> getFillerWords () {
		HashSet<String> result = new HashSet<String>();
		
		try {
			File file = new File("material" + File.separator + "filler-words.info");
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
	 * Support for trend finding</br>
	 * @return a list of word (or word combination) and its frequency</br>
	 * update filler word list in material/filler-words.info
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
	 * Open article view window</br>
	 * @param Article article: article to be shown
	 * @param ArticleSet articleSet: the set to search for related articles
	*/
	public static void showArticleView(Article article, ArticleSet articleSet) {
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
	 * Show about window
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
	 * Show trend window</br>
	 * @param ArticleSet articleSet: the article set to analyze and show result
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
}
