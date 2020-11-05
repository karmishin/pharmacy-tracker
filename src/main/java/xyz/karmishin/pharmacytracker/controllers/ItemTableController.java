package xyz.karmishin.pharmacytracker.controllers;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import xyz.karmishin.pharmacytracker.SceneSwitcher;
import xyz.karmishin.pharmacytracker.entities.Item;
import xyz.karmishin.pharmacytracker.scrapers.MaksavitItemScraperService;

public class ItemTableController implements Initializable {
	private MaksavitItemScraperService service;

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

	public ItemTableController(String name) {
		service = new MaksavitItemScraperService(name);
		service.start();
	}
	
	public ItemTableController(MaksavitItemScraperService service) {
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
		title.setCellValueFactory(new PropertyValueFactory<Item, String>("title"));
		price.setCellValueFactory(new PropertyValueFactory<Item, String>("price"));
		stock.setCellValueFactory(new PropertyValueFactory<Item, String>("stock"));

		tableView.setRowFactory(value -> {
			TableRow<Item> row = new TableRow<>();

			row.setOnMouseClicked(event -> {
				if (event.getClickCount() == 2 && (!row.isEmpty())) {
					var item = row.getItem();
					var locationTableController = new LocationTableController(item, service);
					var sceneSwitcher = new SceneSwitcher("/fxml/locationtable.fxml", locationTableController, event);
					sceneSwitcher.switchScene();
				}
			});

			return row;
		});
	}

	@FXML
	protected void handleBackButtonAction(ActionEvent event) {
		if (service.isRunning()) {
			service.cancel();
		}

		var searchPromptController = new SearchPromptController();
		var sceneSwitcher = new SceneSwitcher("/fxml/searchprompt.fxml", searchPromptController, event);
		sceneSwitcher.switchScene();
	}
}
