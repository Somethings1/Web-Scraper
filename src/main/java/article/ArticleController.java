package article;

import java.io.File;
import java.io.PrintWriter;
import java.util.Scanner;
import java.util.Vector;

import utility.JSONAdapter;

public class ArticleController {
	private static final String COUNTER_FILE_NAME = "config" + File.separator + "total.info";
	public static int id;
	
	public static void beginIDCount () throws Exception {
		id = getTotalArticle() + 1;
	}
	
	/**
	 * Write the total number of articles to the total.info file
	 */
	public static void updateTotalArticle() throws Exception {
		File file = new File(COUNTER_FILE_NAME);
		PrintWriter writer = new PrintWriter(file);
		writer.print(id - 1);
		writer.close();
	}
	
	/**
	 * Set the counter to the one saved in file
	 * 
	 * @return the total number of articles saved in the database
	 */
	public static int getTotalArticle() throws Exception {
		int total = 0;
		File file = new File(COUNTER_FILE_NAME);
		Scanner reader = new Scanner(file);
		String s = reader.nextLine();
		total = Integer.parseInt(s);
		reader.close();
		return total;
	}
	
	public static Vector<Article> loadAllArticle() throws Exception {
		Vector<Article> articleSet = new Vector<Article>();
		int total = getTotalArticle();

		// Read all the articles
		for (int i = 1; i <= total; i++) {
			articleSet.add(JSONAdapter.readArticleFromJSONFile(i));
		}
		return articleSet;
	}
}
