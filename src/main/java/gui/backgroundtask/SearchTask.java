package gui.backgroundtask;

import java.util.Vector;

import article.ArticleSet;
import javafx.concurrent.Task;

public class SearchTask extends Task<ArticleSet> {
	private ArticleSet articleSet;
	private String query;

	public SearchTask(ArticleSet articleSet, String query) {
		this.articleSet = articleSet;
		this.query = query;
	}

	@Override
	protected ArticleSet call() throws Exception {
		updateMessage("processing");

		Vector<String> queries = new Vector<String>();
		for (String s : query.split("&"))
			queries.add(s);

		try {
			// Else
			for (String singleQuery : queries) {
				if (singleQuery.split("=").length == 1) {
					articleSet.applyGeneralFilter(singleQuery.split(","));
					continue;
				}

				String type = singleQuery.split("=")[0];
				String[] filters = singleQuery.split("=")[1].split(",");

				switch (type.trim()) {
				case "site":
					articleSet.filterByWebName(filters);
					break;
				case "author":
					articleSet.filterByAuthor(filters);
					break;
				case "type":
					articleSet.filterByType(filters);
					break;
				case "dateRange":
					articleSet.filterByDateRange(filters[0] + "T00:00:00", filters[1] + "T23:59:59");
					break;
				case "tag":
					articleSet.filterByCategory(filters);
					break;
				case "hashtag":
					articleSet.filterByHashtag(filters);
					break;
				default:
					throw new Exception("Invalid search query, please try again!");
				}
			}
		} catch (Exception e) {
			updateMessage(e.getMessage());
			throw new Exception("");
		}

		updateMessage("done");
		return this.articleSet;
	}
}
