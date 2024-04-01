package article;

import java.util.Vector;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import java.io.PrintWriter;
import java.io.IOException;
import java.io.File;
import java.util.Scanner;
import scraping.Scraper;

public class Article {
	public static int id;
	public String link;
	public String webName;
	public String type;
	public String summary;
	public String title;
	public Vector<String> content;
	public String publishDate;
	public Vector<String> authors;
	public Vector<String> hashtag;
	public Vector<String> category;	
	public static int count = 0;
	
	public static void updateTotalArticle () {
		try {
			File file = new File(System.getProperty("user.dir") + File.separator + "total.info");
			PrintWriter writer = new PrintWriter(file);
			writer.print(id - 1);
			writer.close();
		} catch (Exception e) {
			e.getStackTrace();
		}
	}
	
	public static int getTotalArticle () {
		int total = 0;
		try {
			File file = new File(System.getProperty("user.dir") + File.separator + "total.info");
			Scanner reader = new Scanner(file);
			String s = reader.nextLine();
			total = Integer.parseInt(s);
			reader.close();
		} catch (Exception e) {
			e.getStackTrace();
		}
		return total;
	}
	
	public Article () {
	}

	public void saveToJSON () {
		JSONObject mainObject = new JSONObject();
		mainObject.put("id", id);
		mainObject.put("link", link);
		mainObject.put("webName", webName);
		mainObject.put("type", type);
		mainObject.put("summary", summary);
		mainObject.put("title", title);
		mainObject.put("content", content);
		mainObject.put("publishDate", publishDate);
		
		JSONArray hashtagJSON = new JSONArray();
		JSONArray categoryJSON = new JSONArray();
		JSONArray authorJSON = new JSONArray();
		JSONArray contentJSON = new JSONArray();
		for (String s: hashtag) hashtagJSON.add(s);
		for (String s: category) categoryJSON.add(s);
		for (String s: authors) authorJSON.add(s);
		for (String s: content) contentJSON.add(s);
		
		mainObject.put("hashtag", hashtagJSON);
		mainObject.put("category", categoryJSON);
		mainObject.put("author", authorJSON);
		mainObject.put("content", contentJSON);
		
		try {
			PrintWriter file = new PrintWriter(System.getProperty("user.dir") + File.separator + "JSONFile" + File.separator + id + ".json");
			file.write(mainObject.toJSONString());
			System.out.println("Completed, saved to: " + id + ".json");
			id++;
			file.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static Article loadFromJSON (int id) {
		Article article = new Article();
		
		return article;
	}
}
