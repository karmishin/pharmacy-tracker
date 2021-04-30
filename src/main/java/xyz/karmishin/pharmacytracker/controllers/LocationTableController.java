package xyz.karmishin.pharmacytracker.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import xyz.karmishin.pharmacytracker.SceneSwitcher;
import xyz.karmishin.pharmacytracker.entities.Item;
import xyz.karmishin.pharmacytracker.entities.Location;
import xyz.karmishin.pharmacytracker.scrapers.ScraperNotFoundException;
import xyz.karmishin.pharmacytracker.scrapers.ScraperService;
import xyz.karmishin.pharmacytracker.scrapers.ScraperServiceFactory;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class LocationTableController implements Initializable {
	private ScraperService<Location> locationService;
	private ScraperService<Item> itemService;
	private Item item;

	@FXML
	private Label titleLabel;
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
	@FXML
	private Tab mapTab;

	public LocationTableController(Item item, ScraperService<Item> itemService) {
		try {
			locationService = ScraperServiceFactory.makeLocationScraperService(item, itemService.getPharmacyChain());
			locationService.start();

			this.item = item;
			this.itemService = itemService;
		} catch (ScraperNotFoundException e) {
			var alert = new Alert(AlertType.ERROR, e.getMessage());
			alert.show();
		}
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		titleLabel.setText(item.getTitle());

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

		tableView.setRowFactory(value -> {
			TableRow<Location> row = new TableRow<>();

			row.setOnMouseClicked(event -> {
				if (event.getClickCount() == 2 && (!row.isEmpty())) {
					var stage = new Stage();
					stage.show();

					var pharmacyLocation = row.getItem();
					var locationViewController = new LocationViewController(pharmacyLocation, locationService, item);

					var loader = new FXMLLoader();
					loader.setLocation(getClass().getResource("/fxml/locationview.fxml"));
					loader.setController(locationViewController);
					try {
						Pane pane = loader.load();
						stage.setScene(new Scene(pane));
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			});

			return row;
		});
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
