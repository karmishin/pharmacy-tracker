package xyz.karmishin.pharmacytracker.controllers;

import javafx.beans.property.SimpleListProperty;
import javafx.collections.ListChangeListener;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import xyz.karmishin.pharmacytracker.entities.ShoppingListEntry;

import java.net.URL;
import java.util.ResourceBundle;

public class ShoppingListController implements Initializable {
    private SimpleListProperty<ShoppingListEntry> shoppingListProperty;

    @FXML
    private TableView<ShoppingListEntry> tableView;
    @FXML
    private TableColumn<ShoppingListEntry, String> title;
    @FXML
    private TableColumn<ShoppingListEntry, Double> price;
    @FXML
    private TableColumn<ShoppingListEntry, String> address;
    @FXML
    private WebView webView;

    public ShoppingListController(SimpleListProperty<ShoppingListEntry> shoppingListProperty) {
        this.shoppingListProperty = shoppingListProperty;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        tableView.itemsProperty().bind(shoppingListProperty);

        // dynamically resizable columns
        title.prefWidthProperty().bind(tableView.widthProperty().divide(2));
        price.prefWidthProperty().bind(tableView.widthProperty().divide(5));
        address.prefWidthProperty().bind(tableView.widthProperty().divide(3));

        // set the column factories
        title.setCellValueFactory(new PropertyValueFactory<>("title"));
        price.setCellValueFactory(new PropertyValueFactory<>("price"));
        address.setCellValueFactory(new PropertyValueFactory<>("address"));

        String mapPageUrl = getClass().getResource("/map/map.html").toExternalForm();
        WebEngine webEngine = webView.getEngine();
        webEngine.load(mapPageUrl);

        webEngine.getLoadWorker().stateProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == Worker.State.SUCCEEDED) {
                shoppingListProperty.get().forEach(shoppingListEntry -> {
                    webEngine.executeScript("L.marker([" + shoppingListEntry.getCoordinate() +
                            "]).addTo(map).bindPopup('" + shoppingListEntry.getAddress() + "');");
                });
            }
        });

        shoppingListProperty.addListener((ListChangeListener<ShoppingListEntry>) c -> {
            c.next();
            c.getAddedSubList().forEach(shoppingListEntry -> {
                webEngine.executeScript("L.marker([" + shoppingListEntry.getCoordinate() +
                        "]).addTo(map).bindPopup('" + shoppingListEntry.getAddress() + "');");
            });
        });
    }
}
