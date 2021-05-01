package xyz.karmishin.pharmacytracker;

import javafx.application.Application;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.openqa.selenium.Platform;
import xyz.karmishin.pharmacytracker.controllers.SearchPromptController;
import xyz.karmishin.pharmacytracker.entities.ShoppingListEntry;

import java.util.Objects;

public class Main extends Application {
	@Override
	public void start(Stage primaryStage) {
		primaryStage.setWidth(800);
		primaryStage.setHeight(500);
		primaryStage.setTitle("Pharmacy Tracker");
		primaryStage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/logo.png"))));

		ObservableList<ShoppingListEntry> shoppingList = FXCollections.observableArrayList();
		var shoppingListProperty = new SimpleListProperty<>(shoppingList);
		var sceneSwitcher = new SceneSwitcher("/fxml/searchprompt.fxml", new SearchPromptController(shoppingListProperty), primaryStage);
		sceneSwitcher.switchScene();

		primaryStage.show();

		switch (Platform.getCurrent()) {
			case WINDOWS:
				System.setProperty("webdriver.chrome.driver", getClass().getResource("/bin/chromedriver.exe").getPath());
				break;
			case LINUX:
				System.setProperty("webdriver.chrome.driver", getClass().getResource("/bin/chromedriver").getPath());
				break;
		}
	}

	public static void main(String[] args) {
		launch(args);
	}
}
