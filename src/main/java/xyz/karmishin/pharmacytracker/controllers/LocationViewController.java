package xyz.karmishin.pharmacytracker.controllers;

import javafx.beans.property.SimpleListProperty;
import javafx.concurrent.Worker;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import xyz.karmishin.pharmacytracker.Persistence;
import xyz.karmishin.pharmacytracker.entities.Item;
import xyz.karmishin.pharmacytracker.entities.Location;
import xyz.karmishin.pharmacytracker.entities.ShoppingListEntry;
import xyz.karmishin.pharmacytracker.scrapers.ScraperService;
import xyz.karmishin.pharmacytracker.services.GeocodingService;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class LocationViewController implements Initializable {
	private GeocodingService geocodingService;
	private Location pharmacyLocation;
	private Item item;
	private SimpleListProperty<ShoppingListEntry> shoppingListProperty;

	@FXML
	private Label titleLabel;
	@FXML
	private Label priceLabel;
	@FXML
	private Label addressLabel;
	@FXML
	private WebView webView;

	public LocationViewController(Location pharmacyLocation, ScraperService<Location> locationService, Item item, SimpleListProperty<ShoppingListEntry> shoppingListProperty) {
		geocodingService = new GeocodingService(pharmacyLocation.getAddress());

		this.pharmacyLocation = pharmacyLocation;
		this.item = item;
		this.shoppingListProperty = shoppingListProperty;
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		titleLabel.setText(item.getTitle());
		priceLabel.setText(String.valueOf(pharmacyLocation.getPrice()));
		addressLabel.setText(pharmacyLocation.getAddress());

		String mapPageUrl = getClass().getResource("/map/map.html").toExternalForm();
		WebEngine webEngine = webView.getEngine();
		webEngine.load(mapPageUrl);

		webEngine.getLoadWorker().stateProperty().addListener((observable, oldValue, newValue) -> {
			if (newValue == Worker.State.SUCCEEDED) {
				geocodingService.start();
			}
		});

		geocodingService.setOnSucceeded((event -> {
			webEngine.executeScript("L.marker([" + geocodingService.getValue() +
					"]).addTo(map).bindPopup('" + pharmacyLocation.getAddress() + "');");
		}));
	}

	@FXML
	protected void handleAddToListButtonAction(ActionEvent event) {
		var entry = new ShoppingListEntry(item, pharmacyLocation, geocodingService.getValue());
		shoppingListProperty.get().add(entry);
		try {
			var persistence = new Persistence();
			persistence.createShoppingListDao().create(entry);
			persistence.close();
		} catch (SQLException | IOException e) {
			new Alert(Alert.AlertType.ERROR, e.getMessage()).show();
		}
	}
}
