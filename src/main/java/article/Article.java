package article;
import java.util.Vector;

import article.entity.Entity;
import edu.stanford.nlp.ie.AbstractSequenceClassifier;
import edu.stanford.nlp.ie.crf.CRFClassifier;
import edu.stanford.nlp.ling.CoreLabel;
import utility.JSONAdapter;

/**
 * The soul of the project. Articles scraped from the internet will be contained
 * here
 */
public class Article {

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

	
	private String serializedClassifier = "classifiers/english.muc.7class.distsim.crf.ser.gz";
	private static AbstractSequenceClassifier<CoreLabel> classifier;

	public Article() {
		try {
			if (classifier == null)
				classifier = CRFClassifier.getClassifier(serializedClassifier);
		}
		catch (Exception e) {
			
		}
	}

	private void updateID () {
		this.ownID = ArticleController.id++;
	}
	
	public synchronized void save () throws Exception {
		updateID();
		entities.addAll(ArticleUtility.findAllEntities(content, classifier));
		JSONAdapter.saveArticleToJSONFile(this);
		ArticleController.updateTotalArticle();
	}
}
