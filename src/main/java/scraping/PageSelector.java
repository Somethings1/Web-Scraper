package scraping;
import java.io.File;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import java.io.FileReader;

/**
 * Configures selectors for a page\nusing CSS selector
 * <ul>Parameters:<li>summary</li><li>title</li><li>content</li><li>publishDate</li><li>hashtag</li><li>authors</li><li>category</li></ul>
*/
public class PageSelector {
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
	
	public void setByJSONFile (String fileName) throws Exception {
		try {			
			JSONParser parser = new JSONParser();
			Object obj = parser.parse(new FileReader(System.getProperty("user.dir") + File.separator + fileName));
			JSONObject options = (JSONObject)obj;
			
			setSummary((String)options.get("summary"));
			setTitle((String)options.get("title"));
			setContent((String)options.get("content"));
			setPublishDate((String)options.get("publishDate"));
			setHashtag((String)options.get("hashtag"));
			setAuthors((String)options.get("authors"));
			setCategory((String)options.get("category"));
			
		} catch (Exception e) {
			throw e;
		}
	}
}
