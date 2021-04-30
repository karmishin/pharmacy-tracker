package xyz.karmishin.pharmacytracker.controllers;

import javafx.beans.property.SimpleListProperty;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import xyz.karmishin.pharmacytracker.SceneSwitcher;
import xyz.karmishin.pharmacytracker.entities.ShoppingListEntry;

import java.io.IOException;
import java.net.URL;
import java.util.Locale;
import java.util.ResourceBundle;

public class SearchPromptController implements Initializable {
	private SimpleListProperty<ShoppingListEntry> shoppingListProperty;
	@FXML
	private TextField queryField;
	@FXML
	private ToggleGroup group;

	public SearchPromptController(SimpleListProperty<ShoppingListEntry> shoppingListProperty) {
		this.shoppingListProperty = shoppingListProperty;
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		queryField.setOnKeyPressed(event -> {
			if (event.getCode() == KeyCode.ENTER)
				search(event);
		});
	}

	@FXML
	protected void handleSearchButtonAction(ActionEvent event) {
		search(event);
	}

	private void search(Event event) {
		String query = queryField.getText();
		String pharmacyChain = ((RadioButton) group.getSelectedToggle()).getId();

		var itemTableController = new ItemTableController(query, pharmacyChain, shoppingListProperty, this);
		var sceneSwitcher = new SceneSwitcher("/fxml/itemtable.fxml", itemTableController, event);
		sceneSwitcher.switchScene();
	}

	@FXML
	protected void handleListButtonAction(ActionEvent event) {
		var stage = new Stage();
		stage.setTitle("Shopping List - Pharmacy Tracker");
		stage.show();

		var loader = new FXMLLoader();
		loader.setLocation(getClass().getResource("/fxml/shoppinglist.fxml"));

		var shoppingListController = new ShoppingListController(shoppingListProperty);
		loader.setController(shoppingListController);

		var locale = new Locale("ru", "RU");    // TODO
		var resourceBundle = ResourceBundle.getBundle("/strings/strings", locale);
		loader.setResources(resourceBundle);

		try {
			Pane pane = loader.load();
			stage.setScene(new Scene(pane));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
