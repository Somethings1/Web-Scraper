package article;

import java.util.Vector;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import java.io.PrintWriter;
import java.io.IOException;
import java.io.File;
import java.util.Scanner;

public class Article {
	public static int id;
	public String link;
	public String webName;
	public String type;
	public String summary;
	public String title;
	public String content;
	public String publishDate;
	public String author;
	public Vector<String> hashtag;
	public Vector<String> category;	
	
	public static void updateTotalArticle () {
		try {
			File file = new File(System.getProperty("user.dir") + File.separator + "total.info");
			PrintWriter writer = new PrintWriter(file);
			writer.print(id);
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
		id++;
		JSONObject mainObject = new JSONObject();
		mainObject.put("id", id);
		mainObject.put("link", link);
		mainObject.put("webName", webName);
		mainObject.put("type", type);
		mainObject.put("summary", summary);
		mainObject.put("title", title);
		mainObject.put("content", content);
		mainObject.put("publishDate", publishDate);
		mainObject.put("author", author);
		
		JSONArray hashtagJSON = new JSONArray();
		JSONArray categoryJSON = new JSONArray();
		for (String s: hashtag) hashtagJSON.add(s);
		for (String s: category) categoryJSON.add(s);
		
		mainObject.put("hashtag", hashtagJSON);
		mainObject.put("category", categoryJSON);
		
		try {
			PrintWriter file = new PrintWriter(System.getProperty("user.dir") + File.separator + "JSONFile" + File.separator + id + ".json");
			file.write(mainObject.toJSONString());
			System.out.println("Done: " + id);
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
