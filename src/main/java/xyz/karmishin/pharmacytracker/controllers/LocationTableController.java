package xyz.karmishin.pharmacytracker.controllers;

import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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
import xyz.karmishin.pharmacytracker.entities.ShoppingListEntry;
import xyz.karmishin.pharmacytracker.scrapers.ScraperNotFoundException;
import xyz.karmishin.pharmacytracker.scrapers.ScraperService;
import xyz.karmishin.pharmacytracker.scrapers.ScraperServiceFactory;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

public class LocationTableController implements Initializable {
	private ScraperService<Location> locationService;
	private ScraperService<Item> itemService;
	private Item item;
	private SimpleListProperty<ShoppingListEntry> shoppingListProperty;
	private ItemTableController itemTableController;

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
	private TextField filterField;

	public LocationTableController(Item item, ScraperService<Item> itemService, SimpleListProperty<ShoppingListEntry> shoppingListProperty, ItemTableController itemTableController) {
		this.itemTableController = itemTableController;
		try {
			locationService = ScraperServiceFactory.makeLocationScraperService(item, itemService.getPharmacyChain());
			locationService.start();

			this.item = item;
			this.itemService = itemService;
			this.shoppingListProperty = shoppingListProperty;
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
		address.setCellValueFactory(new PropertyValueFactory<>("address"));
		price.setCellValueFactory(new PropertyValueFactory<>("price"));
		stock.setCellValueFactory(new PropertyValueFactory<>("stock"));

		tableView.setRowFactory(value -> {
			TableRow<Location> row = new TableRow<>();

			row.setOnMouseClicked(event -> {
				if (event.getClickCount() == 2 && (!row.isEmpty())) {
					var stage = new Stage();
					stage.show();

					var loader = new FXMLLoader();
					loader.setLocation(getClass().getResource("/fxml/locationview.fxml"));

					var pharmacyLocation = row.getItem();
					var locationViewController = new LocationViewController(pharmacyLocation, locationService, item, shoppingListProperty);
					loader.setController(locationViewController);

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
			});

			return row;
		});
	}

	@FXML
	protected void onTextChanged() {
		if (filterField.getText().isEmpty()) {
			tableView.itemsProperty().bind(locationService.partialResultsProperty());
			return;
		}
		tableView.itemsProperty().unbind();
		tableView.setItems(filterList(locationService.getPartialResults(), filterField.getText()));
	}

	private ObservableList<Location> filterList(List<Location> locationList, String searchText) {
		var filteredList = new ArrayList<Location>();
		for (Location location : locationList) {
			if (location.getAddress().toLowerCase().contains(searchText.toLowerCase())) {
				filteredList.add(location);
			}
		}
		return FXCollections.observableList(filteredList);
	}

	@FXML
	protected void handleBackButtonAction(ActionEvent event) {
		if (locationService.isRunning()) {
			locationService.cancel();
		}

		var sceneSwitcher = new SceneSwitcher("/fxml/itemtable.fxml", itemTableController, event);
		sceneSwitcher.switchScene();
	}
}
