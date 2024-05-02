package article;

import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
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

/**
 * The soul of the project. Articles scraped from the internet will be contained
 * here
 */
public class Article {
	/**
	 * This id should not be accessed anywhere else in the code. If you want to get
	 * the ID of the article, use ownID instead
	 */
	public static int id;

	/**
	 * The original URL to the article
	 */
	public String link;

	/**
	 * The web name extracted from URL
	 */
	public String webName;

	/**
	 * One of the following: news, facebook post, tweet,...
	 */
	public String type;

	/**
	 * Brief content of the article
	 */
	public String summary;

	/**
	 * The headline
	 */
	public String title;

	/**
	 * Publish date in the form yyyy-mm-ddTHH-mm-ss
	 */
	public String publishDate;

	/**
	 * Each element is a paragraph of the article
	 */
	public Vector<String> content = new Vector<String>();

	/**
	 * Each element is the name of an author
	 */
	public Vector<String> authors = new Vector<String>();

	/**
	 * Each element is a hashtag
	 */
	public Vector<String> hashtag = new Vector<String>();

	/**
	 * Each element is a category
	 */
	public Vector<String> category = new Vector<String>();

	/**
	 * Each element is an object of type Entity
	 */
	public Vector<Entity> entities = new Vector<Entity>();

	/**
	 * The non-static id of the article
	 */
	public int ownID;

	private static final String COUNTER_FILE = "config" + File.separator + "total.info";
	private String serializedClassifier = "classifiers/english.muc.7class.distsim.crf.ser.gz";
	private AbstractSequenceClassifier<CoreLabel> classifier;

	/**
	 * Create a blank article
	 */
	public Article() {}

	/**
	 * Write the total number of articles to the total.info file
	 */
	public static void updateTotalArticle() throws Exception {
		File file = new File(COUNTER_FILE);
		PrintWriter writer = new PrintWriter(file);
		writer.print(id - 1);
		writer.close();
	}
	
	/**
	 * Set the counter to the one saved in file
	 * 
	 * @return <code>int</code>: the total number of articles saved in the database
	 */
	public static int getTotalArticle() throws Exception {
		int total = 0;
		File file = new File(COUNTER_FILE);
		Scanner reader = new Scanner(file);
		String s = reader.nextLine();
		total = Integer.parseInt(s);
		reader.close();
		return total;
	}

	/**
	 * Find all entities in a text
	 * 
	 * @param s The String to be analyzed
	 * @return a Vector of Entity contains in the given text
	 */
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

	/**
	 * Save current article to JSON file. Use the static id for file name
	 */
	@SuppressWarnings("unchecked")
	public synchronized void saveToJSON() throws Exception {
		classifier = CRFClassifier.getClassifier(serializedClassifier);

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

		PrintWriter file = new PrintWriter("JSONFile" + File.separator + id + ".json");
		file.write(mainObject.toJSONString());
		file.close();

		System.out.println("Completed, saved to: " + id++ + ".json");
		updateTotalArticle();
	}

	/**
	 * Read an article from JSON file with corresponding id
	 * 
	 * @param id the id of article to be read
	 * @return an Article object: the article read from JSON file
	 */
	public static Article readFromJSON(int id) throws Exception {
		Article article = new Article();

		JSONParser parser = new JSONParser();
		JSONObject main = new JSONObject();

		// DO NOT delete this try...catch under any circumstance
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

	/**
	 * Read all articles from database, write to a vector of article
	 * 
	 * @return A Vector of all articles have ever been saved
	 */
	public static Vector<Article> loadAllArticle() throws Exception {
		Vector<Article> articleSet = new Vector<Article>();

		// Get number of articles
		int total = 0;
		Scanner reader = new Scanner(new File("config" + File.separator + "total.info"));
		total = Integer.parseInt(reader.nextLine());
		reader.close();

		// Read all the articles
		for (int i = 1; i <= total; i++) {
			articleSet.add(Article.readFromJSON(i));
		}
		return articleSet;
	}
}
