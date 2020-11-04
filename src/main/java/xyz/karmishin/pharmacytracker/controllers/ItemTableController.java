package xyz.karmishin.pharmacytracker.controllers;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.Pane;
import xyz.karmishin.pharmacytracker.ScreenController;
import xyz.karmishin.pharmacytracker.entities.Item;
import xyz.karmishin.pharmacytracker.scrapers.MaksavitItemScraperService;

public class ItemTableController implements Initializable {
	private MaksavitItemScraperService service;
	
	@FXML private Label label;
	@FXML private ProgressBar progressBar;
	@FXML private TableView<Item> tableView;
	@FXML private TableColumn<Item, String> title;
	@FXML private TableColumn<Item, String> price;
	@FXML private TableColumn<Item, String> stock;
	
	public ItemTableController(String name) {
		service = new MaksavitItemScraperService(name);
	}
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		service.start();
		
		tableView.itemsProperty().bind(service.partialResultsProperty());
		progressBar.progressProperty().bind(service.progressProperty());

		// dynamically resizable columns
		title.prefWidthProperty().bind(tableView.widthProperty().divide(2));
		price.prefWidthProperty().bind(tableView.widthProperty().divide(4));
		stock.prefWidthProperty().bind(tableView.widthProperty().divide(4));
		
		// set the column factories
		title.setCellValueFactory(new PropertyValueFactory<Item, String>("title"));
		price.setCellValueFactory(new PropertyValueFactory<Item, String>("price"));
		stock.setCellValueFactory(new PropertyValueFactory<Item, String>("stock"));

		tableView.setRowFactory(value -> {
			TableRow<Item> row = new TableRow<>();
			row.setOnMouseClicked(event -> {
				if (event.getClickCount() == 2 && (!row.isEmpty())) {
					var item = row.getItem();

					var loader = new FXMLLoader();
					var locationTableController = new LocationTableController(item);
					loader.setLocation(getClass().getResource("/fxml/locationtable.fxml"));
					loader.setController(locationTableController);
					try {
						Pane locationTable = loader.load();
						var screenController = ScreenController.getInstance();
						screenController.addScreen("locationTable", (Pane) locationTable);
						screenController.activate("locationTable");
					} catch (IOException e) {
						e.printStackTrace();
						var alert = new Alert(AlertType.ERROR, e.getMessage());
						alert.show();
					}	
				}
			});

			return row;
		});
	}
	
	@FXML protected void handleBackButtonAction(ActionEvent event) {
		if (service.isRunning()) {
			service.cancel();
		}
		var screenController = ScreenController.getInstance();
		screenController.activate("searchPrompt");
	}
}
