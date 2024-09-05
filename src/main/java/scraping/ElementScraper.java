package scraping;

import java.util.Vector;

import com.microsoft.playwright.Page;

public class ElementScraper {
	private Page page;
	private String instruction, type, selector;
	private boolean required;
	private Vector<String> content = new Vector<String>();
	
	public ElementScraper (Page page, String instruction) {
		this.page = page;
		this.instruction = instruction;
	}
	
	private void scrapeElementByCSSSelector(boolean keepHTMLFormat) {
		int total = page.locator(selector).count();

		for (int i = 0; i < total; i++) {
			if (keepHTMLFormat)
				content.add(page.locator(selector).nth(i).innerHTML());
			else
				content.add(page.locator(selector).nth(i).innerText());
		}
	}

	private void scrapeElementByJSONConfig() {
		String JSONConfig = page.locator("script[type=\"application/ld+json\"]").innerText();

		int start = JSONConfig.indexOf("\"" + selector + "\"");
		while (start != -1) {
			int attrStart = start + selector.length() + 4;

			content.add(JSONConfig.substring(attrStart, JSONConfig.indexOf("\"", attrStart)));

			start = JSONConfig.indexOf("\"" + selector + "\"", start + 1);
		}
	}

	private void scrapeElementByMetaProperty() {
		selector = "meta[property=\"" + selector + "\"]";
		int cnt = page.locator(selector).count();
		for (int i = 0; i < cnt; i++) {
			content.add(page.locator(selector).nth(i).getAttribute("content"));
		}
	}
	
	private void splitInstruction () {
		String[] tmp = instruction.split(";");
		this.type = tmp[0];
		this.selector = tmp[1];
		this.required = tmp[2].equals("1");
	}
	
	private boolean isMissingElement () {
		return this.content.size() == 0;
	}
	
	private void scrape () 
			throws Exception {
		switch (type) {
		case "1":
			scrapeElementByCSSSelector(false);
			break;
		case "2":
			scrapeElementByCSSSelector(true);
			break;
		case "3":
			scrapeElementByJSONConfig();
			break;
		case "4":
			scrapeElementByMetaProperty();
			break;
		default:
			throw new Exception("Invalid selector format");
		}
	}
	
	public Vector<String> getScrapedElements () 
			throws Exception {
		splitInstruction();
		scrape();

		if (isMissingElement()) {
			if (this.required)
				throw new Exception("Invalid page format");
			else 
				this.content.add("");
		}
		
		return this.content;
	}
}
