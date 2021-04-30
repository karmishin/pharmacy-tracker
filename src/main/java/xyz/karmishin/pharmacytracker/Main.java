package xyz.karmishin.pharmacytracker;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

import javafx.application.Application;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableListBase;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import xyz.karmishin.pharmacytracker.controllers.SearchPromptController;
import xyz.karmishin.pharmacytracker.entities.ShoppingListEntry;

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
	}

	public static void main(String[] args) {
		launch(args);
	}
}
