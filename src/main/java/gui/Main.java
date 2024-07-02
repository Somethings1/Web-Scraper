package gui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class Main extends Application {		
	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		Parent root = FXMLLoader.load(getClass().getResource("pages/index.fxml"));
        primaryStage.setTitle("Blockchain news");
        primaryStage.setScene(new Scene(root, 993, 673));
        primaryStage.initStyle(StageStyle.UNDECORATED);
        primaryStage.show();
	}
}
