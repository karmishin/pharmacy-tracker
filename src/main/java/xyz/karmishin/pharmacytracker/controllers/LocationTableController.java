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
import xyz.karmishin.pharmacytracker.ScreenController;
import xyz.karmishin.pharmacytracker.entities.Item;
import xyz.karmishin.pharmacytracker.entities.Location;
import xyz.karmishin.pharmacytracker.scrapers.MaksavitLocationScraperService;

public class LocationTableController implements Initializable {
	private MaksavitLocationScraperService service;

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

	public LocationTableController(Item item) {
		service = new MaksavitLocationScraperService(item);
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		service.start();

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
		
		var screenController = ScreenController.getInstance();
		screenController.activate("itemTable");
	}
}
