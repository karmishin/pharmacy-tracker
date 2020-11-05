package xyz.karmishin.pharmacytracker.controllers;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextField;
import xyz.karmishin.pharmacytracker.SceneSwitcher;

public class SearchPromptController implements Initializable {
	@FXML
	private TextField queryField;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		
	}

	@FXML
	protected void handleSearchButtonAction(ActionEvent event) {
		var itemTableController = new ItemTableController(queryField.getText());
		var sceneSwitcher = new SceneSwitcher("/fxml/itemtable.fxml", itemTableController, event);
		sceneSwitcher.switchScene();
	}
}
