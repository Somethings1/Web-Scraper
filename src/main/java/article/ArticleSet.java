package article;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Vector;

import article.entity.Entity;
import utility.StringUtility;


/**
 * Set of all article ever scraped
*/
public class ArticleSet {
	private HashSet<Article> backupSet = new HashSet<Article>();
	private HashSet<Article> articleSet = new HashSet<Article>();
	private final int MINIMUM_ENTITY_COUNT_TO_MATCH = 10;
	
	/**
	 * Load all article from folder JSONFile to the set. This constructor should be used only once in the program because it's time-consuming<br>
	 * In case of creating a new, separate Article Set, use constructor <code>ArticleSet(ArticleSet, boolean)</code>
	*/
	public ArticleSet () {
		try {
			this.articleSet.addAll(ArticleController.loadAllArticle());
			this.backupSet.addAll(this.articleSet);
		}
		catch (Exception e) {
			e.printStackTrace(System.out);
		}
	}
	
	/**
	 * Clone another ArticleSet
	 * @param set the ArticleSet to clone from
	 * @param keepResult <code>boolean</code> to determine whether to keep the previous search result or reset the set back to original
	*/
	public ArticleSet (ArticleSet set, boolean keepResult) {
		if (keepResult)
			this.articleSet.addAll(set.content());
		else 
			this.articleSet.addAll(set.backupSet);
	}
	
	/**
	 * Clear all the search result and return to the original set (all articles)
	*/
	public void reset () {
		this.articleSet.addAll(this.backupSet);
	}
	
	/**
	 * Get the current size of the set
	 * @return single integer: current size of the set
	*/
	public int size () {
		return this.articleSet.size();
	}
	
	/**
	 * Get the content stored in the set
	 * @return Vector of Article of the content in the set
	*/	
	public Vector<Article> content () {
		Vector<Article> res = new Vector<Article>();
		res.addAll(articleSet);
		return res;
	}
	
	public void add (Article article) {
		this.articleSet.add(article);
	}
	
	private boolean matchContent (Article article, String content) {
		for (String s: article.content) 
			if (StringUtility.contains(s, content)) 
				return false;
		if (StringUtility.contains(article.title, content)
		 || StringUtility.contains(article.summary, content))	
			return false;
				
		if (StringUtility.contains(article.webName, content))
			return false;
				
		for (String s: article.authors) 
			if (StringUtility.contains(s, content)) 
				return false;
				
		if (StringUtility.contains(article.type, content))
			return false;
				
		for (String s: article.hashtag) 
			if (StringUtility.contains(s, content)) 
				return false;
				
		for (String s: article.category) 
			if (StringUtility.contains(s, content)) 
				return false;
		return true;
	}
	
	/**
	 * Remove all articles in the set which does not contain any of the query in any field
	 * @param query Collection of String to search for
	 * @return this set after filter.
	*/
	public ArticleSet applyGeneralFilter (String ...query) {
		articleSet.removeIf(article -> {
			for (String content: query)
				if (matchContent(article, content)) 
					return false;
			return true;
		});
		return this;
	}
	
	/**
	 * Remove all articles in the set which does not contain any of the query in content, title or summary 
	 * @param query Collection of String to search for
	 * @return this set after filter
	*/
	public ArticleSet filterByContent (String ...query) {
		articleSet.removeIf(article -> {
			for (String content: query) {				
				for (String s: article.content) 
					if (StringUtility.contains(s, content)) 
						return false;
				
				if (StringUtility.contains(article.title, content)
				 || StringUtility.contains(article.summary, content))	
					return false;
			}
			return true;
			
		});
		
		return this;
	}
	
	/**
	 * Remove all articles in the set which does not contain any of the query in web name 
	 * @param query Collection of String to search for
	 * @return this set after filter
	*/
	public ArticleSet filterByWebName (String ...query) {
		articleSet.removeIf(article -> {	
			for (String content: query)
				if (StringUtility.contains(article.webName, content))
					return false;		
			return true;
		});
		
		return this;
	}
	
	/**
	 * Remove all articles in the set which does not contain any of the query in type
	 * @param query Collection of String to search for
	 * @return this set after filter
	*/
	public ArticleSet filterByType (String ...query) {
		articleSet.removeIf(article -> {
			for (String content: query)
				if (StringUtility.contains(article.type, content))
					return false;
			return true;
		});
		
		return this;
	}
	
	/**
	 * Remove all articles in the set which has publish date earlier than startDate or later than endDate
	 * @param startDate String represent date in format yyyy-mm-dd
	 * @param endDate String represent date in format yyyy-mm-dd
	 * @return this set after filter
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
	 * Remove all articles in the set which was not written by any of the provided authors
	 * @param query Collection of String StringUtility.contains the authors' name
	 * @return this set after filter
	*/
	public ArticleSet filterByAuthor (String ...query) {
		articleSet.removeIf(article -> {
			for (String content: query) 
				for (String s: article.authors) 
					if (StringUtility.contains(s, content)) 
						return false;
			return true;
		});
		return this;
	}
	
	/**
	 * Remove all articles in the set which does not have any provided hashtag
	 * @param query Collection of String to search for
	 * @return this set after filter
	*/
	public ArticleSet filterByHashtag (String ...query) {
		articleSet.removeIf(article -> {
			for (String content: query) 
				for (String s: article.hashtag) 
					if (StringUtility.contains(s, content)) 
						return false;
			return true;
		});
		return this;
	}
	
	/**
	 * Remove all articles in the set which does not have any provided category
	 * @param query Collection of String to search for
	 * @return this set after filter
	*/
	public ArticleSet filterByCategory (String ...query) {
		articleSet.removeIf(article -> {
			for (String content: query) 
				for (String s: article.category) 
					if (StringUtility.contains(s, content)) 
						return false;
			return true;
		});
		return this;
	}
	
	/**
	 * Remove all articles in the set which does not have more than 10 similar entities to the provided Article
	 * @param source the Article to search for
	 * @return this set after filter
	*/
	public ArticleSet filterBySimilarEntity (Article source) {
		articleSet.removeIf(article -> {
			int count = 0;
			for (Entity entity: article.entities) {
				for (Entity toCompare: source.entities)
					if (entity.content.equals(toCompare.content))
						count++;
			}
			if (count < MINIMUM_ENTITY_COUNT_TO_MATCH) return false;
			return true;
		});
		return this;
	}
}
