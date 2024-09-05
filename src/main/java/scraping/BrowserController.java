package scraping;

import java.io.File;
import java.util.Scanner;
import java.util.Vector;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.options.Proxy;

public class BrowserController {
	private Browser browser;
	private int proxyCount = 0;
	private Vector<String> proxy = new Vector<String>();
	private static final String PROXY_FILE = "config" + File.separator + "prx.txt";
	private final int DEFAULT_WAIT_TIME = 60000;
	
	public BrowserController () {
		loadProxyList();
		Playwright playwright = Playwright.create();
		BrowserType.LaunchOptions launchOptions = new BrowserType.LaunchOptions();
		//launchOptions.setProxy("per-context");
		this.browser = playwright.chromium().launch(launchOptions);
	}
	
	private void loadProxyList() {
		try {
			File file = new File(PROXY_FILE);
			Scanner reader = new Scanner(file);

			while (reader.hasNextLine()) {
				String prx = reader.nextLine();
				if (prx.isBlank())
					continue;
				proxy.add(prx);
			}

			reader.close();
		} catch (Exception e) {
			Debugger.yell(e.getMessage());
		}
	}
	
	private BrowserContext createBrowserContext() {
		BrowserContext context;
		if (proxy.size() > 0) {
			String[] t = proxy.elementAt(proxyCount % proxy.size()).split(":");
			Proxy prox = new Proxy(t[0] + ":" + t[1]);
			prox.setUsername(t[2]).setPassword(t[3]);
			proxyCount++;
			context = browser.newContext(new Browser.NewContextOptions().setJavaScriptEnabled(false).setProxy(prox));
			Debugger.yell("Proxy: " + t[0] + ":" + t[1]);
		} else {
			context = browser.newContext(new Browser.NewContextOptions().setJavaScriptEnabled(false));
		}
		return context;
	}
	
	private void ignoreTimedOutException (Page page) {
		page.setDefaultTimeout(DEFAULT_WAIT_TIME); 
	}
	
	private void blockAllImagesAndCSS (Page page) {
		page.route("**/*.{png,jpg,jpeg,css,ico,svg}", route -> route.abort());
	}
	
	private void navigatePageToLink (Page page, String link)
		throws PlaywrightException {
		long navigateTimeStart = System.currentTimeMillis();
		page.navigate(link); // might throw Exception
		long navigateTimeEnd = System.currentTimeMillis();
		Debugger.yell("Navigated in " + (navigateTimeEnd - navigateTimeStart));
	}
	
	public Page openPageAt (String link) {
		BrowserContext context = createBrowserContext();
		Page page = context.newPage();
		
		ignoreTimedOutException(page);
		blockAllImagesAndCSS(page);
		navigatePageToLink(page, link);
		
		return page;
	}
	
	public void close () {
		this.browser.close();
	}
}
