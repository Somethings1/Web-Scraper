package article;
import java.io.File;
import java.util.Scanner;

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
	@Override
	public String toString() {
		return "summary=" + summary + "\ntitle=" + title + "\ncontent=" + content + "\npublishDate="
				+ publishDate + "\nhashtag=" + hashtag + "\nauthors=" + authors + "\ncategory=" + category;
	}
	
	public void setByString (String _options) throws Exception {
		String options[] = _options.split("\n");
		int cnt = 0;
		for (String option: options) {
			String attr = option.split("=")[0];
			String val = option.split("=")[1];
			
			switch(attr) {
			case "summary": 
				this.summary = val;
				cnt++;
				break;
			case "title":
				this.title = val;
				cnt++;
				break;
			case "content":
				this.content = val;
				cnt++;
				break;
			case "publishDate":
				this.publishDate = val;
				cnt++;
				break;
			case "hashtag":
				this.hashtag = val;
				cnt++;
				break;
			case "authors":
				this.authors = val;
				cnt++;
				break;
			case "category": 
				this.category = val;
				cnt++;
				break;
			default:
				throw new Exception("Invalid format for page selector");
			}
		}
		if (cnt < 7)
			throw new Exception("Invalid format for page selector");
	}
	
	public void setByFile (String fileName) throws Exception {
		try {
			File file = new File(System.getProperty("user.dir") + File.separator + fileName);
			Scanner reader = new Scanner(file);
			
			String res = reader.nextLine();
			
			while (reader.hasNextLine()) {
				res += "\n" + reader.nextLine();
			}
			
			setByString(res);
			
			reader.close();
		} catch (Exception e) {
			throw e;
		}
	}
}
