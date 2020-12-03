package xyz.karmishin.pharmacytracker.controllers;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import xyz.karmishin.pharmacytracker.SceneSwitcher;

public class SearchPromptController implements Initializable {
	@FXML
	private TextField queryField;
	@FXML
	private ToggleGroup group;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		
	}

	@FXML
	protected void handleSearchButtonAction(ActionEvent event) {
		String query = queryField.getText();
		String pharmacyChain = ((RadioButton) group.getSelectedToggle()).getId();

		var itemTableController = new ItemTableController(query, pharmacyChain);
		var sceneSwitcher = new SceneSwitcher("/fxml/itemtable.fxml", itemTableController, event);
		sceneSwitcher.switchScene();
	}
}
