package xyz.karmishin.pharmacytracker;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public class Main extends Application {
	private static Logger logger = LogManager.getLogger(Main.class);
	
	@Override
	public void start(Stage primaryStage) throws IOException {
		ScreenController screenController = ScreenController.getInstance();
		
		Pane searchPrompt = FXMLLoader.load(getClass().getResource("/fxml/searchprompt.fxml"));
		screenController.addScreen("searchPrompt", searchPrompt);
		screenController.activate("searchPrompt");
		
		primaryStage.setScene(screenController.getMainScene());
		primaryStage.setTitle("Pharmacy Tracker");
		primaryStage.setWidth(800);
		primaryStage.setHeight(500);
		primaryStage.show();
	}
	
	public static void main(String[] args) {
		launch(args);
	}
}