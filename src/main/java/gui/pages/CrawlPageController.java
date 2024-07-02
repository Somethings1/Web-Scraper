package gui.pages;

import java.io.File;
import java.io.PrintWriter;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import article.ArticleSet;
import gui.backgroundtask.ScrapeTask;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import scraping.ScraperOptions;
import scraping.PageSelector;

public class CrawlPageController {
	private final String[] SELECTOR_TYPE = {
		"CSS (keep text only)",
	    "CSS (keep HTML)",
	    "Application/ld+json",
	    "<meta> tag"
	};
	private final String[] LOAD_METHOD = {
		"scroll",
		"click",
		"none"
	};
	private final int FIELD_WIDTH1 = 400;
	private final int FIELD_WIDTH2 = 650;

	private double xOffset = 0;
	private double yOffset = 0;
	private ScraperOptions scraperOptions = new ScraperOptions();
	private PageSelector pageSelector = new PageSelector();
	private TextArea proxyField; 
	private ComboBox summaryType = createDropdown(SELECTOR_TYPE);
	private ComboBox titleType = createDropdown(SELECTOR_TYPE);
	private ComboBox contentType = createDropdown(SELECTOR_TYPE);
	private ComboBox publishDateType = createDropdown(SELECTOR_TYPE);
	private ComboBox hashtagType = createDropdown(SELECTOR_TYPE);
	private ComboBox authorsType = createDropdown(SELECTOR_TYPE);
	private ComboBox categoryType = createDropdown(SELECTOR_TYPE);
	private TextField summarySelector = createTextField(FIELD_WIDTH1);
	private TextField titleSelector = createTextField(FIELD_WIDTH1);
	private TextField publishDateSelector = createTextField(FIELD_WIDTH1);
	private TextField hashtagSelector = createTextField(FIELD_WIDTH1);
	private TextField authorsSelector = createTextField(FIELD_WIDTH1);
	private TextField contentSelector = createTextField(FIELD_WIDTH1);
	private TextField categorySelector = createTextField(FIELD_WIDTH1);
	private CheckBox summaryRequirement = new CheckBox();
	private CheckBox titleRequirement = new CheckBox();
	private CheckBox publishDateRequirement = new CheckBox();
	private CheckBox hashtagRequirement = new CheckBox();
	private CheckBox authorsRequirement = new CheckBox();
	private CheckBox contentRequirement = new CheckBox();
	private CheckBox categoryRequirement = new CheckBox();
	private TextField startLink = createTextField(FIELD_WIDTH2);
	private TextField loadLinkButtonText = createTextField(FIELD_WIDTH2);
	private TextField validLinkPrefix = createTextField(FIELD_WIDTH2);
	private TextField maxLinkCount = createTextField(FIELD_WIDTH2);
	private TextField thread = createTextField(FIELD_WIDTH2);
	private ComboBox loadLinkMethod = createDropdown(LOAD_METHOD);
	private TextArea console = createTextArea(870);
	private final int MAXIMUM_LINE_IN_TEXTAREA = 1000;
	
	public static TextArea _console;
	private ArticleSet articleSet;
	

	@FXML
	HBox title_bar;
	@FXML
	Label label_title;
	@FXML
	ScrollPane scroll_pane;
	@FXML
	Pane exit_btn;
	@FXML
	VBox item_container;
	
	public void hoverExitButton() {
		exit_btn.setBackground(new Background(new BackgroundFill(Paint.valueOf(Color.DARK_RED), null, null)));
	}

	public void unhoverExitButton() {
		exit_btn.setBackground(null);
	}

	public void clickExitButton() {
		exit_btn.getScene().getWindow().hide();
	}
	
	private Label createNormalText (String s, int size) {
		Label res = new Label(s);
		
		res.setFont(new Font("Montserrat Regular", size));
		res.setTextFill(Paint.valueOf(Color.WHITE));
		
		return res;
	}
	
	private Label createTitle (String s) {
		Label res = new Label(s);
		
		res.setFont(new Font("Montserrat Bold", 32));
		res.setTextFill(Paint.valueOf(Color.WHITE));
		res.setAlignment(Pos.CENTER);
		res.setPadding(new Insets(30, 0, 0, 0));
		
		return res;
	}
	private TextField createTextField (int width) {
		TextField field = new TextField();
		
		field.setStyle("-fx-text-fill: " + Color.WHITE + ";"
				+ "-fx-border-color: " + Color.LIGHT_GREY + ";"
				+ "-fx-border-radius: 10px");
		field.setBackground(null);
		field.setFont(new Font("Montserrat Regular", 15));
		field.setMinWidth(width);
		
		return field;
	}
	
	private TextArea createTextArea (int width) {
		TextArea field = new TextArea() {
			@Override
            public void replaceText(int start, int end, String text) {
                super.replaceText(start, end, text);
                while(getText().split("\n", -1).length > MAXIMUM_LINE_IN_TEXTAREA) {
                    int fle = getText().indexOf("\n");
                    super.replaceText(0, fle+1, "");
                }
                positionCaret(getText().length());
            }
		};
		field.setStyle("-fx-text-fill: white;"
					 + "-fx-background-color: transparent;"
					 + "-fx-control-inner-background: " + Color.BACKGROUND_MEDIUM + ";"
					 + "-fx-border-color: " + Color.WHITE + ";");
		field.setMaxWidth(width);
		field.setMinHeight(300);
		field.setFont(new Font("Montserrat", 15));
		field.widthProperty().addListener((o) -> {
			Node vp = field.lookup(".content");
			vp.setStyle("-fx-background-color: " + Color.BACKGROUND_MEDIUM + ";"
					  + "-fx-text-fill: white;");
		});
		return field;
	}
	
	private ComboBox createDropdown (String ...s) {
		ComboBox box = new ComboBox();
		for (String t: s) box.getItems().add(t);
		return box;
	}
	
	private void createComponents () {
		Label title1 = createTitle("Page Selector");
		Label subtitle1 = createNormalText("Provide way to select the right element on page", 18);
		GridPane selectorPane = createSelectorPane();
		
		
		Label title2 = createTitle("Scraper Options");
		Label subtitle2 = createNormalText("Change the settings of scraper", 18);
		GridPane optionsPane = createScraperOptionsPane();
		
		
		Label title3 = createTitle("Proxy List");
		Label subtitle3 = createNormalText("The number of proxy should double the number of thread\n"
												+ "Proxy should be given in the form address:port:username:password", 18);
		proxyField = createTextArea(870);
		
		Label scrapeButton = new Label("Scrape!");
		scrapeButton.setStyle("-fx-padding: 10 20 10 20;"
				+ "-fx-text-fill: white;"
				+ "-fx-font-family: Montserrat;"
				+ "-fx-font-weight: bold;"
				+ "-fx-font-size: 19px;"
				+ "-fx-border-color: " + Color.LIGHT_BLUE + ";"
				+ "-fx-border-radius: 15px;"
				+ "-fx-border-width: 2px;"
				+ "-fx-cursor: hand");
		scrapeButton.setOnMouseEntered((event) -> {
			scrapeButton.setBackground(
					new Background(
							new BackgroundFill(
									Paint.valueOf(Color.LIGHT_BLUE),
									new CornerRadii(15),
									null
							)
					)
			);
		});
		scrapeButton.setOnMouseExited((event) -> {
			scrapeButton.setBackground(null);
		});
		scrapeButton.setOnMouseClicked((event) -> {
			startScraper();
		});
		
		item_container.getChildren().addAll(title1, subtitle1, selectorPane, title2, subtitle2, optionsPane, title3, subtitle3, proxyField, scrapeButton);
		VBox.setMargin(proxyField, new Insets(20));
		VBox.setMargin(scrapeButton, new Insets(30, 0, 0, 400));
		updateFromFile();
	}
	
	private void startScraper () {
		try {
			// Update the user settings
			writeToFile();
			
			ScrapeTask scraper = new ScrapeTask(articleSet);
			scraper.setOnFailed(event -> {
				// Show error message;
			});
			
			ExecutorService executorService = Executors.newFixedThreadPool(20);
			
			// Open console 
			_console = console;
			_console.setEditable(false);
			item_container.getChildren().add(_console);	
			VBox.setMargin(_console, new Insets(20));
			
			executorService.execute(scraper);
			executorService.shutdown();
		} catch (Exception e) {
			// Show error message
		}
	}
	
	private GridPane createSelectorPane () {
		GridPane pane = new GridPane();
		pane.setVgap(20);
		pane.setHgap(30);
		pane.setPadding(new Insets(20));
		
		pane.add(createNormalText("Option", 15), 1, 0);
		pane.add(createNormalText("Selector", 15), 2, 0);
		pane.add(createNormalText("Required", 15), 3, 0);
		
		pane.add(createNormalText("Summary: ", 15), 0, 1);
		pane.add(summaryType, 1, 1);
		pane.add(summarySelector, 2, 1);
		pane.add(summaryRequirement, 3, 1);
		GridPane.setHalignment(summaryRequirement, HPos.CENTER);
		
		pane.add(createNormalText("Title: ", 15), 0, 2);
		pane.add(titleType, 1, 2);
		pane.add(titleSelector, 2, 2);
		pane.add(titleRequirement, 3, 2);
		GridPane.setHalignment(titleRequirement, HPos.CENTER);
		
		pane.add(createNormalText("Paragraph: ", 15), 0, 3);
		pane.add(contentType, 1, 3);
		pane.add(contentSelector, 2, 3);
		pane.add(contentRequirement, 3, 3);
		GridPane.setHalignment(contentRequirement, HPos.CENTER);
		
		pane.add(createNormalText("Publish date: ", 15), 0, 4);
		pane.add(publishDateType, 1, 4);
		pane.add(publishDateSelector, 2, 4);
		pane.add(publishDateRequirement, 3, 4);
		GridPane.setHalignment(publishDateRequirement, HPos.CENTER);
		
		pane.add(createNormalText("Hashtag: ", 15), 0, 5);
		pane.add(hashtagType, 1, 5);
		pane.add(hashtagSelector, 2, 5);
		pane.add(hashtagRequirement, 3, 5);
		GridPane.setHalignment(hashtagRequirement, HPos.CENTER);
		
		pane.add(createNormalText("Authors: ", 15), 0, 6);
		pane.add(authorsType, 1, 6);
		pane.add(authorsSelector, 2, 6);
		pane.add(authorsRequirement, 3, 6);
		GridPane.setHalignment(authorsRequirement, HPos.CENTER);
		
		pane.add(createNormalText("Category: ", 15), 0, 7);
		pane.add(categoryType, 1, 7);
		pane.add(categorySelector, 2, 7);
		pane.add(categoryRequirement, 3, 7);
		GridPane.setHalignment(categoryRequirement, HPos.CENTER);
		
		return pane;
	}
	
	private GridPane createScraperOptionsPane () {
		GridPane pane = new GridPane();
		pane.setVgap(20);
		pane.setHgap(30);
		pane.setAlignment(Pos.CENTER);
		pane.setPadding(new Insets(50, 20, 50, 20));
		
		pane.add(createNormalText("Start link: ", 15), 0, 0);
		pane.add(startLink, 1, 0);
		
		pane.add(createNormalText("Load link method: ", 15), 0, 1);
		pane.add(loadLinkMethod, 1, 1);
		
		pane.add(createNormalText("Load link button text: ", 15), 0, 2);
		pane.add(loadLinkButtonText, 1, 2);
		
		pane.add(createNormalText("Max number of link: ", 15), 0, 3);
		pane.add(maxLinkCount, 1, 3);
		
		pane.add(createNormalText("Number of thread: ", 15), 0, 4);
		pane.add(thread, 1, 4);
		
		pane.add(createNormalText("Valid link prefix: ", 15), 0, 5);
		pane.add(validLinkPrefix, 1, 5);
		
		return pane;
	}
	
	private void setSelector (ComboBox type, TextField selector, CheckBox required, String origin) {
		String s[] = origin.split(";");
		type.setValue(SELECTOR_TYPE[Integer.parseInt(s[0]) - 1]);
		selector.setText(s[1]);
		required.fire();
	}
	
	private String getSelector (ComboBox type, TextField selector, CheckBox required) {
		String res = "";
		for (int i = 0; i < SELECTOR_TYPE.length; i++) {
			if (type.getValue().equals(SELECTOR_TYPE[i])) {
				res += String.valueOf(i + 1);
				break;
			}
		}
		res += ";".concat(selector.getText());
		res = res.concat(";".concat(required.isSelected() ? "1" : "0"));
		return res;
	}
	
	private void updateFromFile () {
		try {
			pageSelector.setByJSONFile();
			scraperOptions.setByJSONFile();
			File file = new File("config" + File.separator + "prx.txt");
			Scanner reader = new Scanner(file);

			while (reader.hasNextLine()) {
				String prx = reader.nextLine();
				proxyField.setText(proxyField.getText() + prx + "\n");
			}

			reader.close();
			
			setSelector(summaryType, summarySelector, summaryRequirement, pageSelector.getSummary());
			setSelector(titleType, titleSelector, titleRequirement, pageSelector.getTitle());
			setSelector(contentType, contentSelector, contentRequirement, pageSelector.getContent());
			setSelector(publishDateType, publishDateSelector, publishDateRequirement, pageSelector.getPublishDate());
			setSelector(hashtagType, hashtagSelector, hashtagRequirement, pageSelector.getHashtag());
			setSelector(authorsType, authorsSelector, authorsRequirement, pageSelector.getAuthors());
			setSelector(categoryType, categorySelector, categoryRequirement, pageSelector.getCategory());
			
			startLink.setText(scraperOptions.getStartLink());
			loadLinkButtonText.setText(scraperOptions.getLoadLinkButtonText());
			validLinkPrefix.setText(scraperOptions.getValidLinkPrefix());
			maxLinkCount.setText(String.valueOf(scraperOptions.getMaxLinkCount()));
			thread.setText(String.valueOf(scraperOptions.getThread()));
			loadLinkMethod.setValue(scraperOptions.getLoadLinkMethod());
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void writeToFile() {
		try {
			pageSelector.setSummary(getSelector(summaryType, summarySelector, summaryRequirement));
			pageSelector.setTitle(getSelector(titleType, titleSelector, titleRequirement));
			pageSelector.setContent(getSelector(contentType, contentSelector, contentRequirement));
			pageSelector.setPublishDate(getSelector(publishDateType, publishDateSelector, publishDateRequirement));
			pageSelector.setHashtag(getSelector(hashtagType, hashtagSelector, hashtagRequirement));
			pageSelector.setAuthors(getSelector(authorsType, authorsSelector, authorsRequirement));
			pageSelector.setCategory(getSelector(categoryType, categorySelector, categoryRequirement));
			
			scraperOptions.setLoadLinkButtonText(loadLinkButtonText.getText());
			scraperOptions.setLoadLinkMethod((String)loadLinkMethod.getValue());
			scraperOptions.setMaxLinkCount(Integer.parseInt(maxLinkCount.getText()));
			scraperOptions.setStartLink(startLink.getText());
			scraperOptions.setThread(Integer.parseInt(thread.getText()));
			scraperOptions.setValidLinkPrefix(validLinkPrefix.getText());
			
			pageSelector.writeToJSONFile();
			scraperOptions.writeToJSONFile();
			
			PrintWriter file = new PrintWriter("config" + File.separator + "prx.txt");
			file.append(proxyField.getText());
			file.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void initData (ArticleSet set) {
		this.articleSet = set;
		initStyle();
		createComponents();
	}
	
	public void initStyle () {
		scroll_pane.widthProperty().addListener((o) -> {
			Node vp = scroll_pane.lookup(".viewport");
			vp.setStyle("-fx-background-color: transparent");
		});
		scroll_pane.setStyle("-fx-background-color: transparent");
		scroll_pane.setFitToWidth(true);
		title_bar.setOnMousePressed(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				xOffset = event.getSceneX();
				yOffset = event.getSceneY();
			}
		});
		title_bar.setOnMouseDragged(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				((Stage) (title_bar.getScene().getWindow())).setX(event.getScreenX() - xOffset);
				((Stage) (title_bar.getScene().getWindow())).setY(event.getScreenY() - yOffset);
			}
		});
	}
	
	public void initialize () {
		
	}
}
