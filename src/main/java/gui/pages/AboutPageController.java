package gui.pages;

import gui.Color;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Paint;
import javafx.stage.Stage;

public class AboutPageController {
	private double xOffset = 0;
	private double yOffset = 0;

	@FXML
	HBox title_bar;
	@FXML
	Label label_title;
	@FXML
	ScrollPane scroll_pane;
	@FXML
	Pane exit_btn;
	
	public void hoverExitButton() {
		exit_btn.setBackground(new Background(new BackgroundFill(Paint.valueOf(Color.DARK_RED), null, null)));
	}

	public void unhoverExitButton() {
		exit_btn.setBackground(null);
	}

	public void clickExitButton() {
		exit_btn.getScene().getWindow().hide();
	}
	
	public void initialize () {
		scroll_pane.widthProperty().addListener((o) -> {
			Node vp = scroll_pane.lookup(".viewport");
			vp.setStyle("-fx-background-color: transparent");
			vp = scroll_pane.lookup(":vertical");
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
}
