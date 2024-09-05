package article;

import java.util.HashSet;
import java.util.List;
import java.util.Vector;

import article.entity.Entity;
import edu.stanford.nlp.ie.AbstractSequenceClassifier;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.util.Triple;

public class ArticleUtility {
	
	public static final Vector<Entity> findEntitiesInParagraph(String s, AbstractSequenceClassifier<CoreLabel> classifier) {
		Vector<Entity> res = new Vector<Entity>();

		List<Triple<String, Integer, Integer>> triples = classifier.classifyToCharacterOffsets(s);

		for (int i = 0; i < triples.size(); i++) {
			Triple<String, Integer, Integer> trip = triples.get(i);
			String content = s.substring(trip.second(), trip.third());

			res.add(new Entity(content, trip.first()));
		}

		return res;
	}
	
	public static final HashSet<Entity> findAllEntities (Vector<String> content, AbstractSequenceClassifier<CoreLabel> classifier) {
		HashSet<Entity> entities = new HashSet<Entity>();
		for (int i = 0; i < content.size(); i++) {
			entities.addAll(findEntitiesInParagraph(content.elementAt(i), classifier));
		}
		return entities;
	}
	
	
}
