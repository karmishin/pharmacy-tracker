package xyz.karmishin.pharmacytracker;

import java.io.IOException;
import java.util.Locale;
import java.util.ResourceBundle;

import javafx.event.Event;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public class SceneSwitcher {
    private final String path;
    private final Initializable controller;
    private final Stage primaryStage;

    public SceneSwitcher(String path, Initializable controller, Event event) {
        this.path = path;
        this.controller = controller;
        this.primaryStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
    }

    public SceneSwitcher(String path, Initializable controller, Stage primaryStage) {
        this.path = path;
        this.controller = controller;
        this.primaryStage = primaryStage;
    }

    public void switchScene() {
        var loader = new FXMLLoader();
        loader.setLocation(getClass().getResource(path));
        loader.setController(controller);

        var locale = new Locale("ru", "RU");    // TODO
        var resourceBundle = ResourceBundle.getBundle("/strings/strings", locale);
        loader.setResources(resourceBundle);

        try {
            Pane pane = loader.load();
            var scene = new Scene(pane);
            primaryStage.setScene(scene);

            /* a fix for GNOME-based desktop environments */
            primaryStage.setWidth(primaryStage.getWidth());
		    primaryStage.setHeight(primaryStage.getHeight());
        } catch (IOException e) {
            e.printStackTrace();
            var alert = new Alert(AlertType.ERROR, e.getMessage());
            alert.show();
        }
    }
}
