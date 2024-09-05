package scraping;

import gui.controllers.ScrapingPageController;
import javafx.application.Platform;

public class Debugger {
	private static final int MAX_CONSOLE_LENGTH = 100000;
	
	private static boolean maximumLengthReached () {
		return ScrapingPageController._console.getText().length() > MAX_CONSOLE_LENGTH;
	}
	
	public static void yell (String t) {
		String s = "(" + Thread.currentThread().getName() + ")" + t + "\n";
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				if (maximumLengthReached()) {
					ScrapingPageController._console.setText("");
				}
				ScrapingPageController._console.appendText(s);
			}
		});
	}
}
