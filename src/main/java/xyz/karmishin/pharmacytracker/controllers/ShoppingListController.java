package xyz.karmishin.pharmacytracker.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.qrcode.QRCodeWriter;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ListChangeListener;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.concurrent.Worker;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import xyz.karmishin.pharmacytracker.Persistence;
import xyz.karmishin.pharmacytracker.entities.ShoppingListEntry;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.SQLException;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.UUID;

public class ShoppingListController implements Initializable {
    private static Logger logger = LogManager.getLogger();
    private SimpleListProperty<ShoppingListEntry> shoppingListProperty;
    private ResourceBundle resourceBundle;
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
    @FXML
    private Button exportButton;

    public ShoppingListController(SimpleListProperty<ShoppingListEntry> shoppingListProperty) {
        this.shoppingListProperty = shoppingListProperty;
        resourceBundle = ResourceBundle.getBundle("strings/strings", new Locale("ru", "RU"));
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

        tableView.setRowFactory(value -> {
            TableRow<ShoppingListEntry> row = new TableRow<>();
            row.setOnMouseClicked(mouseEvent -> {
                var contextMenu = new ContextMenu();
                var removeButton = new MenuItem(resourceBundle.getString("list.remove"));
                removeButton.setOnAction(e -> {
                    removeEntry(row.getItem());
                });
                contextMenu.getItems().add(removeButton);
                row.contextMenuProperty().bind(
                        Bindings.when(row.emptyProperty())
                                .then((ContextMenu) null)
                                .otherwise(contextMenu));

            });
            return row;
        });

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

    @FXML
    protected void handleExportButtonAction() {
        var list = shoppingListProperty.get();
        var mapper = new ObjectMapper();
        try {
            logger.debug("Processing JSON...");
            var json = mapper.writeValueAsString(list);
            logger.debug("Generated JSON: " + json);

            var uploadService = new ShoppingListUploadService(json);
            uploadService.setOnSucceeded(event -> {
                logger.debug("Upload successful");
                showQrCode(uploadService.getValue().uri().toString());
                exportButton.setDisable(false);
            });
            uploadService.setOnRunning(event -> {
                exportButton.setDisable(true);
            });
            uploadService.setOnFailed(event -> {
                logger.error(uploadService.getException().getMessage());
                new Alert(Alert.AlertType.ERROR, uploadService.getException().getMessage());
                exportButton.setDisable(false);
            });

            uploadService.start();
            logger.debug("Upload service started");
        } catch (JsonProcessingException jsonProcessingException) {
            new Alert(Alert.AlertType.ERROR, jsonProcessingException.getMessage()).show();
        }
    }

    private void removeEntry(ShoppingListEntry entry) {
        try {
            var persistence = new Persistence();
            persistence.createShoppingListDao().delete(entry);
            persistence.close();
            shoppingListProperty.get().remove(entry);
        } catch (SQLException | IOException e) {
            new Alert(Alert.AlertType.ERROR, e.getMessage()).show();
        }
    }

    private void showQrCode(String content) {
        int width = 300;
        int height = 300;

        try {
            var qrCodeWriter = new QRCodeWriter();
            logger.debug("Generating QR code...");
            var bitMatrix = qrCodeWriter.encode(content, BarcodeFormat.QR_CODE, width, height);
            logger.debug("Creating buffered image...");
            var bufferedImage = MatrixToImageWriter.toBufferedImage(bitMatrix);
            logger.debug("Converting to JavaFX image...");
            var fxImage = SwingFXUtils.toFXImage(bufferedImage, null);

            logger.debug("QR code is ready. Creating the stage...");
            var borderPane = new BorderPane(new ImageView(fxImage));
            var scene = new Scene(borderPane);
            var stage = new Stage();
            stage.setScene(scene);
            stage.show();
            logger.debug("Stage is visible");
        } catch (WriterException e) {
            new Alert(Alert.AlertType.ERROR, e.getMessage()).show();
        }
    }

    private class ShoppingListUploadService extends Service<HttpResponse<String>> {
        private SimpleStringProperty jsonToSend = new SimpleStringProperty();

        public ShoppingListUploadService(String json) {
            jsonToSend.set(json);
        }

        @Override
        protected Task<HttpResponse<String>> createTask() {
            var task = new Task<HttpResponse<String>>() {
                @Override
                protected HttpResponse<String> call() throws Exception {
                    var uuid = UUID.randomUUID().toString();
                    logger.debug("Generated UUID: " + uuid);
                    var request = HttpRequest.newBuilder()
                            .uri(URI.create("https://api.cl1p.net/" + uuid))
                            .POST(HttpRequest.BodyPublishers.ofString(jsonToSend.get()))
                            .build();
                    var client = HttpClient.newHttpClient();
                    logger.debug("Sending request...");
                    var response = client.send(request, HttpResponse.BodyHandlers.ofString());
                    logger.debug("Request sent");
                    return response;
                }
            };

            return task;
        }
    }
}
