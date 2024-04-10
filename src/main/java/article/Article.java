package article;

import java.util.Vector;
import java.util.HashSet;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import java.io.PrintWriter;
import java.io.IOException;
import java.io.File;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;
import edu.stanford.nlp.ie.AbstractSequenceClassifier;
import edu.stanford.nlp.ie.crf.*;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.util.Triple;

class Entity {
	public String type;
	public String content;
	
	public Entity (String type, String content) {
		this.type = type;
		this.content = content;
	}

	@Override
	public int hashCode() {
		return Objects.hash(content, type);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Entity other = (Entity) obj;
		return Objects.equals(content, other.content) && Objects.equals(type, other.type);
	}
}

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
	private static final String COUNTER_FILE = "config" + File.separator + "total.info";
	
	private String serializedClassifier = "classifiers/english.muc.7class.distsim.crf.ser.gz";
	private AbstractSequenceClassifier<CoreLabel> classifier;
	
	public static void updateTotalArticle () {
		try {
			File file = new File(COUNTER_FILE);
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
	
	private final Vector<Entity> findEntitiesInParagraph (String s) {
		Vector<Entity> res = new Vector<Entity>();
		
		List<Triple<String,Integer,Integer>> triples = classifier.classifyToCharacterOffsets(s);
		
        for (int i = 0; i < triples.size(); i++) {
        	Triple<String,Integer,Integer> trip = triples.get(i);
        	String content = s.substring(trip.second(), trip.third());
        			
        	res.add(new Entity(content, trip.first()));
        }
        
		return res;
	}

	@SuppressWarnings("unchecked")
	public synchronized void saveToJSON () {
		try {
			classifier = CRFClassifier.getClassifier(serializedClassifier);
		}
		catch (Exception e) {
			
		}
		
		JSONObject mainObject = new JSONObject();		
		JSONArray hashtagJSON = new JSONArray();
		JSONArray categoryJSON = new JSONArray();
		JSONArray authorJSON = new JSONArray();
		JSONArray contentJSON = new JSONArray();
		JSONArray entitiesJSON = new JSONArray();
		
		for (String s: hashtag) hashtagJSON.add(s);
		for (String s: category) categoryJSON.add(s);
		for (String s: authors) authorJSON.add(s);
		for (String s: content) contentJSON.add(s);
		
		// Find all entities
		HashSet<Entity> entities = new HashSet<Entity>();
		for (int i = 0; i < content.size(); i++) {
			entities.addAll(findEntitiesInParagraph(content.elementAt(i)));
		}
		for (Entity entity: entities) {
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
}
