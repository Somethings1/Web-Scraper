package article;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Vector;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import edu.stanford.nlp.ie.AbstractSequenceClassifier;
import edu.stanford.nlp.ie.crf.CRFClassifier;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.util.Triple;
import gui.Helper;

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
	public Vector<Entity> entities;
	public int ownID;
	private static final String COUNTER_FILE = "config" + File.separator + "total.info";

	private String serializedClassifier = "classifiers/english.muc.7class.distsim.crf.ser.gz";
	private AbstractSequenceClassifier<CoreLabel> classifier;

	public Article() {
		content = new Vector<String>();
		authors = new Vector<String>();
		hashtag = new Vector<String>();
		category = new Vector<String>();
		entities = new Vector<Entity>();
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(link);
		builder.append(webName);
		builder.append(type);
		builder.append(summary);
		builder.append(title);
		builder.append(content);
		builder.append(publishDate);
		builder.append(authors);
		builder.append(hashtag);
		builder.append(category);
		return builder.toString();
	}

	public static void updateTotalArticle() {
		try {
			File file = new File(COUNTER_FILE);
			PrintWriter writer = new PrintWriter(file);
			writer.print(id - 1);
			writer.close();
		} catch (Exception e) {
			e.getStackTrace();
		}
	}

	public static int getTotalArticle() {
		int total = 0;
		try {
			File file = new File(COUNTER_FILE);
			Scanner reader = new Scanner(file);
			String s = reader.nextLine();
			total = Integer.parseInt(s);
			reader.close();
		} catch (Exception e) {
			e.getStackTrace();
		}
		return total;
	}

	private final Vector<Entity> findEntitiesInParagraph(String s) {
		Vector<Entity> res = new Vector<Entity>();

		List<Triple<String, Integer, Integer>> triples = classifier.classifyToCharacterOffsets(s);

		for (int i = 0; i < triples.size(); i++) {
			Triple<String, Integer, Integer> trip = triples.get(i);
			String content = s.substring(trip.second(), trip.third());

			res.add(new Entity(content, trip.first()));
		}

		return res;
	}

	@SuppressWarnings("unchecked")
	public synchronized void saveToJSON() {
		try {
			classifier = CRFClassifier.getClassifier(serializedClassifier);
		} catch (Exception e) {

		}

		JSONObject mainObject = new JSONObject();
		JSONArray hashtagJSON = new JSONArray();
		JSONArray categoryJSON = new JSONArray();
		JSONArray authorJSON = new JSONArray();
		JSONArray contentJSON = new JSONArray();
		JSONArray entitiesJSON = new JSONArray();

		for (String s : hashtag)
			hashtagJSON.add(s);
		for (String s : category)
			categoryJSON.add(s);
		for (String s : authors)
			authorJSON.add(s);
		for (String s : content)
			contentJSON.add(s);

		// Find all entities
		HashSet<Entity> entities = new HashSet<Entity>();
		for (int i = 0; i < content.size(); i++) {
			entities.addAll(findEntitiesInParagraph(content.elementAt(i)));
		}
		for (Entity entity : entities) {
			JSONObject entityJSON = new JSONObject();

			entityJSON.put("content", entity.content);
			entityJSON.put("type", entity.type);
			entitiesJSON.add(entityJSON);
		}

		mainObject.put("id", id);
		mainObject.put("link", link);
		mainObject.put("webName", webName);
		mainObject.put("type", type);
		mainObject.put("summary", summary);
		mainObject.put("title", title);
		mainObject.put("publishDate", publishDate);
		mainObject.put("hashtag", hashtagJSON);
		mainObject.put("category", categoryJSON);
		mainObject.put("authors", authorJSON);
		mainObject.put("content", contentJSON);
		mainObject.put("entity", entitiesJSON);

		try {
			PrintWriter file = new PrintWriter("JSONFile" + File.separator + id + ".json");
			file.write(mainObject.toJSONString());
			file.close();

			System.out.println("Completed, saved to: " + id++ + ".json");
			updateTotalArticle();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static Article readFromJSON(int id) throws Exception {
		Article article = new Article();

		JSONParser parser = new JSONParser();
		JSONObject main = new JSONObject();
		try {
			main = (JSONObject) parser.parse(new FileReader("JSONFile" + File.separator + id + ".json"));
		} catch (Exception e) {
			System.out.println("At article: " + id);
			throw e;
		}

		article.ownID = (int) (long) main.get("id");
		article.link = (String) main.get("link");
		article.webName = (String) main.get("webName");
		article.type = (String) main.get("type");
		article.summary = (String) main.get("summary");
		article.publishDate = (String) main.get("publishDate");
		article.title = Helper.modifyString((String) main.get("title"));
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
			article.content.add(Helper.modifyString((String) o));
		arr = (JSONArray) main.get("entity");
		for (Object o : arr) {
			JSONObject entityJSON = (JSONObject) o;
			Entity entity = new Entity((String) entityJSON.get("content"), (String) entityJSON.get("type"));
			article.entities.add(entity);
		}

		return article;
	}

	public static Vector<Article> loadAllArticle() throws Exception {
		Vector<Article> articleSet = new Vector<Article>();
		
		// Get number of articles
		int total = 0;
		Scanner reader = new Scanner(new File("config" + File.separator + "total.info"));
		total = Integer.parseInt(reader.nextLine());
		reader.close();

		// Read all the articles
		for (int i = 1; i <= total; i++) {
			try {
				articleSet.add(Article.readFromJSON(i));
			} catch (Exception e) {
				e.printStackTrace(System.out);
			}
		}
		return articleSet;
	}
}
