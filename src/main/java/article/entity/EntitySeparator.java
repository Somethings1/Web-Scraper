package article.entity;

import java.util.LinkedList;

import article.Article;
import javafx.util.Pair;

public class EntitySeparator {
	private static LinkedList<Pair<String, String>> result = new LinkedList(), tempList = new LinkedList();

	private static void resetList(LinkedList<Pair<String, String>> to) {
		to.clear();
	}

	private static void addPairToResult(String a, String b) {
		result.add(new Pair<String, String>(a, b));
	}

	private static Pair<String, String> makePair (String a, String b) {
		return new Pair<String, String>(a, b);
	}
	
	private static boolean isSegmentAnEntity (Pair<String, String> segment) {
		return !segment.getValue().equals("none");
	}

	private static LinkedList<Pair<String, String>> separateSegmentByEntity(String segment, Entity entity) {
		LinkedList<Pair<String, String>> result = new LinkedList<>();
		
		int nextOccur = segment.indexOf(entity.content);
		int prevEnd = 0;
		
		while (nextOccur >= 0) {
			result.add(makePair(segment.substring(prevEnd, nextOccur), "none"));
			result.add(makePair(segment.substring(nextOccur, nextOccur + entity.content.length()), entity.type));
			
			prevEnd = nextOccur + entity.content.length();
			nextOccur = segment.indexOf(entity.content, prevEnd);
		}
		
		if (prevEnd != segment.length()) {
			result.add(makePair(segment.substring(prevEnd), "none"));
		}
		
		return result;
	}
	
	private static void putSeparatedSegmentsToTempList (Entity entity) {
		for (int i = 0; i < result.size(); i++) {
			Pair<String, String> currentSegment = result.get(i);
			
			if (isSegmentAnEntity(currentSegment)) {
				tempList.add(currentSegment);
				continue;
			}
			LinkedList<Pair<String, String>> separatedSegments = separateSegmentByEntity(currentSegment.getKey(), entity);
			tempList.addAll(separatedSegments);
		}
	}

	private static void separateResultByEntity(Entity entity) {
		resetList(tempList);
		putSeparatedSegmentsToTempList(entity);
		resetList(result);
		result.addAll(tempList);
	}

	public static final LinkedList<Pair<String, String>> separateEntities(String para, Article article) {
		resetList(result);
		addPairToResult(para, "none");

		for (Entity entity : article.entities) {
			separateResultByEntity(entity);
		}

		return result;
	}

}
