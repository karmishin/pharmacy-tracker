package xyz.karmishin.pharmacytracker.controllers;

import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.cell.PropertyValueFactory;
import xyz.karmishin.pharmacytracker.SceneSwitcher;
import xyz.karmishin.pharmacytracker.entities.Item;
import xyz.karmishin.pharmacytracker.entities.ShoppingListEntry;
import xyz.karmishin.pharmacytracker.scrapers.ScraperNotFoundException;
import xyz.karmishin.pharmacytracker.scrapers.ScraperService;
import xyz.karmishin.pharmacytracker.scrapers.ScraperServiceFactory;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class ItemTableController implements Initializable {
	private ScraperService<Item> service;
	private SimpleListProperty<ShoppingListEntry> shoppingListProperty;
	private SearchPromptController searchPromptController;

	@FXML
	private Label label;
	@FXML
	private ProgressBar progressBar;
	@FXML
	private TableView<Item> tableView;
	@FXML
	private TableColumn<Item, String> title;
	@FXML
	private TableColumn<Item, String> price;
	@FXML
	private TableColumn<Item, String> stock;
	@FXML
	private TextField filterField;

	public ItemTableController(String name, String pharmacyChain, SimpleListProperty<ShoppingListEntry> shoppingListProperty, SearchPromptController searchPromptController) {
		this.shoppingListProperty = shoppingListProperty;
		this.searchPromptController = searchPromptController;

		try {
			service = ScraperServiceFactory.makeItemScraperService(name, pharmacyChain);
			service.start();
		} catch (ScraperNotFoundException e) {
			var alert = new Alert(AlertType.ERROR, e.getMessage());
			alert.show();
		}
	}

	public ItemTableController(ScraperService<Item> service) {
		this.service = service;
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		tableView.itemsProperty().bind(service.partialResultsProperty());
		progressBar.progressProperty().bind(service.progressProperty());

		// dynamically resizable columns
		title.prefWidthProperty().bind(tableView.widthProperty().divide(2));
		price.prefWidthProperty().bind(tableView.widthProperty().divide(4));
		stock.prefWidthProperty().bind(tableView.widthProperty().divide(4));

		// set the column factories
		title.setCellValueFactory(new PropertyValueFactory<>("title"));
		price.setCellValueFactory(new PropertyValueFactory<>("price"));
		stock.setCellValueFactory(new PropertyValueFactory<>("stock"));

		tableView.setRowFactory(value -> {
			TableRow<Item> row = new TableRow<>();

			row.setOnMouseClicked(event -> {
				if (event.getClickCount() == 2 && (!row.isEmpty())) {
					var item = row.getItem();
					var locationTableController = new LocationTableController(item, service, shoppingListProperty, this);
					var sceneSwitcher = new SceneSwitcher("/fxml/locationtable.fxml", locationTableController, event);
					sceneSwitcher.switchScene();
				}
			});

			return row;
		});
	}

	@FXML
	protected void onTextChanged() {
		if (filterField.getText().isEmpty()) {
			tableView.itemsProperty().bind(service.partialResultsProperty());
			return;
		}
		tableView.itemsProperty().unbind();
		tableView.setItems(filterList(service.getPartialResults(), filterField.getText()));
	}

	private ObservableList<Item> filterList(List<Item> itemList, String searchText) {
		var filteredList = new ArrayList<Item>();
		for (Item item : itemList) {
			if (item.getTitle().toLowerCase().contains(searchText.toLowerCase())) {
				filteredList.add(item);
			}
		}
		return FXCollections.observableList(filteredList);
	}

	@FXML
	protected void handleBackButtonAction(ActionEvent event) {
		if (service.isRunning()) {
			service.cancel();
		}

		var sceneSwitcher = new SceneSwitcher("/fxml/searchprompt.fxml", searchPromptController, event);
		sceneSwitcher.switchScene();
	}
}
