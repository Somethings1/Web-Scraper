package utility;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import article.Article;
import article.entity.Entity;

public class JSONAdapter {
	public static JSONObject readJSONObjectFromFile (String filePath) 
			throws ParseException, IOException {
		JSONParser parser = new JSONParser();
		Object obj = parser.parse(new FileReader(filePath));
		return (JSONObject) obj;
	}
	
	public static void writeJSONObjectToFile (JSONObject object, String filePath) 
			throws FileNotFoundException {
		PrintWriter file = new PrintWriter(filePath);
		file.write(object.toJSONString());
		file.close();
	}
	
	public static void saveArticleToJSONFile (Article article) 
			throws FileNotFoundException {
		JSONObject mainObject = new JSONObject();
		JSONArray hashtagJSON = new JSONArray();
		JSONArray categoryJSON = new JSONArray();
		JSONArray authorJSON = new JSONArray();
		JSONArray contentJSON = new JSONArray();
		JSONArray entitiesJSON = new JSONArray();

		for (String s : article.hashtag)
			hashtagJSON.add(s);
		for (String s : article.category)
			categoryJSON.add(s);
		for (String s : article.authors)
			authorJSON.add(s);
		for (String s : article.content)
			contentJSON.add(s);

		for (Entity entity : article.entities) {
			JSONObject entityJSON = new JSONObject();

			entityJSON.put("content", entity.content);
			entityJSON.put("type", entity.type);
			entitiesJSON.add(entityJSON);
		}

		mainObject.put("id", article.ownID);
		mainObject.put("link", article.link);
		mainObject.put("webName", article.webName);
		mainObject.put("type", article.type);
		mainObject.put("summary", article.summary);
		mainObject.put("title", article.title);
		mainObject.put("publishDate", article.publishDate);
		mainObject.put("hashtag", hashtagJSON);
		mainObject.put("category", categoryJSON);
		mainObject.put("authors", authorJSON);
		mainObject.put("content", contentJSON);
		mainObject.put("entity", entitiesJSON);
		
		writeJSONObjectToFile(mainObject, "JSONFile" + File.separator + article.ownID + ".json");
	}
	
	public static Article readArticleFromJSONFile (int id) throws Exception {
		Article article = new Article();
		JSONObject main = readJSONObjectFromFile("JSONFile" + File.separator + id + ".json");

		article.ownID = (int) (long) main.get("id");
		article.link = (String) main.get("link");
		article.webName = (String) main.get("webName");
		article.type = (String) main.get("type");
		article.summary = (String) main.get("summary");
		article.publishDate = (String) main.get("publishDate");
		article.title = (String) main.get("title");
		JSONArray arr = (JSONArray) main.get("hashtag");
		for (Object o : arr)
			article.hashtag.add((String) o);
		arr = (JSONArray) main.get("category");
		for (Object o : arr)
			article.category.add((String) o);
		arr = (JSONArray) main.get("authors");
		for (Object o : arr)
			article.authors.add((String) o);
		arr = (JSONArray) main.get("content");
		for (Object o : arr)
			article.content.add((String) o);
		arr = (JSONArray) main.get("entity");
		for (Object o : arr) {
			JSONObject entityJSON = (JSONObject) o;
			Entity entity = new Entity((String) entityJSON.get("content"), (String) entityJSON.get("type"));
			article.entities.add(entity);
		}

		return article;
	}
}
