package xyz.karmishin.pharmacytracker.controllers;

import java.io.IOException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import xyz.karmishin.pharmacytracker.ScreenController;

public class SearchPromptController {
	private static Logger logger = LogManager.getLogger(SearchPromptController.class);

	@FXML private TextField queryField;
	
	@FXML protected void handleSearchButtonAction(ActionEvent event) {
		try {
			var loader = new FXMLLoader();
			var itemTableController = new ItemTableController(queryField.getText());
			loader.setLocation(getClass().getResource("/fxml/itemtable.fxml"));
			loader.setController(itemTableController);
			
			Pane itemTable = loader.load();
			var screenController = ScreenController.getInstance();
			screenController.addScreen("itemTable", (Pane) itemTable);
			screenController.activate("itemTable");
		} catch (IOException e) {
			e.printStackTrace();
			var alert = new Alert(AlertType.ERROR, e.getMessage());
			alert.show();
		}
	}

}
