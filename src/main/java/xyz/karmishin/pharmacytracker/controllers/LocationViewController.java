package xyz.karmishin.pharmacytracker.controllers;

import javafx.concurrent.Worker;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.text.Text;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import xyz.karmishin.pharmacytracker.entities.Item;
import xyz.karmishin.pharmacytracker.entities.Location;
import xyz.karmishin.pharmacytracker.scrapers.ScraperService;
import xyz.karmishin.pharmacytracker.services.GeocodingService;

import java.net.URL;
import java.util.ResourceBundle;

public class LocationViewController implements Initializable {
	private GeocodingService geocodingService;
	private ScraperService<Location> locationService;
	private Location pharmacyLocation;
	private Item item;

	@FXML
	private Text titleText;
	@FXML
	private Text priceText;
	@FXML
	private Text addressText;
	@FXML
	private WebView webView;

	public LocationViewController(Location pharmacyLocation, ScraperService<Location> locationService, Item item) {
		geocodingService = new GeocodingService(pharmacyLocation.getAddress());

		this.pharmacyLocation = pharmacyLocation;
		this.item = item;
		this.locationService = locationService;
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		titleText.setText(item.getTitle());
		priceText.setText(String.valueOf(pharmacyLocation.getPrice()));
		addressText.setText(pharmacyLocation.getAddress());

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
	protected void handleBackButtonAction(ActionEvent event) {
//		if (geocodingService.isRunning()) {
//			geocodingService.cancel();
//		}
//
//		var locationTableController = new LocationTableController(item, locationService);
//		var sceneSwitcher = new SceneSwitcher("/fxml/locationtable.fxml", locationTableController, event);
//		sceneSwitcher.switchScene();
	}
}
