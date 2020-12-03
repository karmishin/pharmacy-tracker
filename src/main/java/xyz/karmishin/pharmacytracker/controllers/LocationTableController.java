package xyz.karmishin.pharmacytracker.controllers;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import xyz.karmishin.pharmacytracker.SceneSwitcher;
import xyz.karmishin.pharmacytracker.entities.Item;
import xyz.karmishin.pharmacytracker.entities.Location;
import xyz.karmishin.pharmacytracker.scrapers.ScraperNotFoundException;
import xyz.karmishin.pharmacytracker.scrapers.ScraperService;
import xyz.karmishin.pharmacytracker.scrapers.ScraperServiceFactory;

public class LocationTableController implements Initializable {
	private ScraperService<Location> locationService;
	private ScraperService<Item> itemService;

	@FXML
	private Label label;
	@FXML
	private ProgressBar progressBar;
	@FXML
	private TableView<Location> tableView;
	@FXML
	private TableColumn<Location, String> address;
	@FXML
	private TableColumn<Location, Double> price;
	@FXML
	private TableColumn<Location, String> stock;

	public LocationTableController(Item item, ScraperService<Item> itemService) {
		try {
			locationService = ScraperServiceFactory.makeLocationScraperService(item, itemService.getPharmacyChain());
			locationService.start();

			this.itemService = itemService;
		} catch (ScraperNotFoundException e) {
			var alert = new Alert(AlertType.ERROR, e.getMessage());
			alert.show();
		}
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		tableView.itemsProperty().bind(locationService.partialResultsProperty());
		progressBar.progressProperty().bind(locationService.progressProperty());

		// dynamically resizable columns
		address.prefWidthProperty().bind(tableView.widthProperty().divide(2));
		price.prefWidthProperty().bind(tableView.widthProperty().divide(4));
		stock.prefWidthProperty().bind(tableView.widthProperty().divide(4));

		// set the column factories
		address.setCellValueFactory(new PropertyValueFactory<Location, String>("address"));
		price.setCellValueFactory(new PropertyValueFactory<Location, Double>("price"));
		stock.setCellValueFactory(new PropertyValueFactory<Location, String>("stock"));
	}

	@FXML
	protected void handleBackButtonAction(ActionEvent event) {
		if (locationService.isRunning()) {
			locationService.cancel();
		}

		var itemTableController = new ItemTableController(itemService);
		var sceneSwitcher = new SceneSwitcher("/fxml/itemtable.fxml", itemTableController, event);
		sceneSwitcher.switchScene();
	}
}
