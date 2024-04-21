package article;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Vector;

public class ArticleSet {
	private HashSet<Article> backupSet = new HashSet<Article>();
	private HashSet<Article> articleSet = new HashSet<Article>();
	
	/**
	 * Load all article from folder JSONFile
	*/
	public ArticleSet () {
		final long startTime = System.currentTimeMillis();
		try {
			this.articleSet.addAll(Article.loadAllArticle());
			this.backupSet.addAll(this.articleSet);
		}
		catch (Exception e) {
			e.printStackTrace(System.out);
		}
		final long endTime = System.currentTimeMillis();
		System.out.println("Search Engine started with size = " + this.articleSet.size() + ". Time: " + (endTime - startTime));
	}
	
	public ArticleSet (ArticleSet a, boolean keepResult) {
		if (keepResult)
			this.articleSet.addAll(a.content());
		else 
			this.articleSet.addAll(a.backupSet);
	}
	
	public void reset () {
		this.articleSet.addAll(this.backupSet);
	}
	
	private boolean contains (String a, String b) {
		if (a == null) return false;
		if (a.length() < b.length()) return false;
		a = a.toLowerCase().trim();
		b = b.toLowerCase().trim();
		return a.contains(b);
	}
	
	/**
	 * Return total number of matching articles
	*/
	public int size () {
		return this.articleSet.size();
	}
	
	/**
	 * return <code>Vector&lt;Article&gt;</code> of articles matches
	*/	
	public Vector<Article> content () {
		Vector<Article> res = new Vector<Article>();
		res.addAll(articleSet);
		return res;
	}
	
	/**
	 * Find in all fields
	*/
	public ArticleSet applyGeneralFilter (String ...query) {
		articleSet.removeIf(article -> {
			for (String content: query) {
				for (String s: article.content) 
					if (contains(s, content)) 
						return false;
				
				if (contains(article.title, content)
				 || contains(article.summary, content))	
					return false;
				
				if (contains(article.webName, content))
					return false;
				
				for (String s: article.authors) 
					if (contains(s, content)) 
						return false;
				
				if (contains(article.type, content))
					return false;
				
				for (String s: article.hashtag) 
					if (contains(s, content)) 
						return false;
				
				for (String s: article.category) 
					if (contains(s, content)) 
						return false;
			}
			return true;
		});
		return this;
	}
	
	/**
	 * Find articles with similar text appear in content, title or summary 
	*/
	public ArticleSet filterByContent (String ...query) {
		articleSet.removeIf(article -> {
			for (String content: query) {				
				for (String s: article.content) 
					if (contains(s, content)) 
						return false;
				
				if (contains(article.title, content)
				 || contains(article.summary, content))	
					return false;
			}
			return true;
			
		});
		
		return this;
	}
	
	/**
	 * Find articles in particular sites
	*/
	public ArticleSet filterByWebName (String ...query) {
		articleSet.removeIf(article -> {	
			for (String content: query)
				if (contains(article.webName, content))
					return false;		
			return true;
		});
		
		return this;
	}
	
	/**
	 * Find articles in particular types
	*/
	public ArticleSet filterByType (String ...query) {
		articleSet.removeIf(article -> {
			for (String content: query)
				if (contains(article.type, content))
					return false;
			return true;
		});
		
		return this;
	}
	
	/**
	 * Find articles in a specific date range</br>
	 * Provide <code>startDate</code> and <code>endDate</code> in format <code>YYYY-MM-DD</code>
	*/
	public ArticleSet filterByDateRange (String startDate, String endDate) throws Exception {
		articleSet.removeIf(article -> {

			LocalDateTime start = LocalDateTime.parse(startDate);
			LocalDateTime end = LocalDateTime.parse(endDate);
			LocalDateTime current = LocalDateTime.parse(article.publishDate);
				
				return current.isBefore(start) || current.isAfter(end);
			
		});
		
		return this;
	}
	
	/**
	 * Find articles written by authors
	*/
	public ArticleSet filterByAuthor (String ...query) {
		articleSet.removeIf(article -> {
			for (String content: query) 
				for (String s: article.authors) 
					if (contains(s, content)) 
						return false;
			return true;
		});
		return this;
	}
	
	/**
	 * Find articles with particular hashtags
	*/
	public ArticleSet filterByHashtag (String ...query) {
		articleSet.removeIf(article -> {
			for (String content: query) 
				for (String s: article.hashtag) 
					if (contains(s, content)) 
						return false;
			return true;
		});
		return this;
	}
	
	/**
	 * Find articles in particular categories
	*/
	public ArticleSet filterByCategory (String ...query) {
		articleSet.removeIf(article -> {
			for (String content: query) 
				for (String s: article.category) 
					if (contains(s, content)) 
						return false;
			return true;
		});
		return this;
	}
	
	/**
	 * Find related articles
	*/
	public ArticleSet filterBySimilarEntity (Article source) {
		articleSet.removeIf(article -> {
			int count = 0;
			for (Entity entity: article.entities) {
				for (Entity toCompare: source.entities)
					if (entity.content.equals(toCompare.content))
						count++;
			}
			if (count < 10) return false;
			return true;
		});
		return this;
	}
}
