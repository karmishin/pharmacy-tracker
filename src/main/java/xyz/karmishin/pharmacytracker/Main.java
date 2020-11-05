package xyz.karmishin.pharmacytracker;

import java.io.IOException;

import javafx.application.Application;
import javafx.stage.Stage;
import xyz.karmishin.pharmacytracker.controllers.SearchPromptController;

public class Main extends Application {
	@Override
	public void start(Stage primaryStage) throws IOException {
		var sceneSwitcher = new SceneSwitcher("/fxml/searchprompt.fxml", new SearchPromptController(), primaryStage);
		sceneSwitcher.switchScene();
		
		primaryStage.setTitle("Pharmacy Tracker");
		primaryStage.setWidth(800);
		primaryStage.setHeight(500);
		primaryStage.show();
	}

	public static void main(String[] args) {
		launch(args);
	}
}