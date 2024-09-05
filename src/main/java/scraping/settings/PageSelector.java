package scraping.settings;

import java.io.File;
import org.json.simple.JSONObject;

import utility.JSONAdapter;

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
		JSONObject options = JSONAdapter.readJSONObjectFromFile(FILE);

		this.summary = (String) options.get("summary");
		this.title = (String) options.get("title");
		this.content = (String) options.get("content");
		this.publishDate = (String) options.get("publishDate");
		this.hashtag = (String) options.get("hashtag");
		this.authors = (String) options.get("authors");
		this.category = (String) options.get("category");
	}
	
	private JSONObject createJSONObjectFromSettings () {
		JSONObject options = new JSONObject();
		
		options.put("summary", this.summary);
		options.put("title", this.title);
		options.put("content", this.content);
		options.put("publishDate", this.publishDate);
		options.put("hashtag", this.hashtag);
		options.put("authors", this.authors);
		options.put("category", this.category);
		
		return options;
	}
	
	public void writeToJSONFile() throws Exception {
		JSONAdapter.writeJSONObjectToFile(createJSONObjectFromSettings(), FILE);
	}
}
