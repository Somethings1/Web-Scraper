package scraping;

import java.io.File;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import java.io.FileReader;
import java.io.PrintWriter;

/**
 * Configures selectors for a page. <br>
 * All selector should be in the format {type};{query};{required}
 * Where {type} = {1, 2, 3, 4} meaning "use css selector, keep text only", "use css selector, keep HTML", "use application/json+ld key", "use meta tag" respectively
 * {query}
 */
public class PageSelector {
	private static final String FILE = "config" + File.separator + "selector.json";
	
	private String summary;
	private String title;
	private String content;
	private String publishDate;
	private String hashtag;
	private String authors;
	private String category;

	public String getSummary() {
		return summary;
	}

	public void setSummary(String summary) {
		this.summary = summary;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getPublishDate() {
		return publishDate;
	}

	public void setPublishDate(String publishDate) {
		this.publishDate = publishDate;
	}

	public String getHashtag() {
		return hashtag;
	}

	public void setHashtag(String hashtag) {
		this.hashtag = hashtag;
	}

	public String getAuthors() {
		return authors;
	}

	public void setAuthors(String authors) {
		this.authors = authors;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public void setByJSONFile() throws Exception {
		JSONParser parser = new JSONParser();
		Object obj = parser.parse(new FileReader(FILE));
		JSONObject options = (JSONObject) obj;

		setSummary((String) options.get("summary"));
		setTitle((String) options.get("title"));
		setContent((String) options.get("content"));
		setPublishDate((String) options.get("publishDate"));
		setHashtag((String) options.get("hashtag"));
		setAuthors((String) options.get("authors"));
		setCategory((String) options.get("category"));

	}
	
	public void writeToJSONFile() throws Exception {
		JSONObject main = new JSONObject();
		
		main.put("summary", this.summary);
		main.put("title", this.title);
		main.put("content", this.content);
		main.put("publishDate", this.publishDate);
		main.put("hashtag", this.hashtag);
		main.put("authors", this.authors);
		main.put("category", this.category);
		
		PrintWriter file = new PrintWriter(FILE);
		file.write(main.toJSONString());
		file.close();
	}
}
