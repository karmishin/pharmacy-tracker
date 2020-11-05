package xyz.karmishin.pharmacytracker.controllers;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import xyz.karmishin.pharmacytracker.SceneSwitcher;
import xyz.karmishin.pharmacytracker.entities.Item;
import xyz.karmishin.pharmacytracker.entities.Location;
import xyz.karmishin.pharmacytracker.scrapers.MaksavitItemScraperService;
import xyz.karmishin.pharmacytracker.scrapers.MaksavitLocationScraperService;

public class LocationTableController implements Initializable {
	private MaksavitLocationScraperService service;
	private MaksavitItemScraperService itemService;

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

	public LocationTableController(Item item, MaksavitItemScraperService itemService) {
		service = new MaksavitLocationScraperService(item);
		service.start();

		this.itemService = itemService;
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		tableView.itemsProperty().bind(service.partialResultsProperty());
		progressBar.progressProperty().bind(service.progressProperty());

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
		if (service.isRunning()) {
			service.cancel();
		}

		var itemTableController = new ItemTableController(itemService);
		var sceneSwitcher = new SceneSwitcher("/fxml/itemtable.fxml", itemTableController, event);
		sceneSwitcher.switchScene();
	}
}
