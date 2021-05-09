package xyz.karmishin.pharmacytracker;

import javafx.application.Application;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.Platform;
import xyz.karmishin.pharmacytracker.controllers.SearchPromptController;
import xyz.karmishin.pharmacytracker.entities.ShoppingListEntry;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Objects;

public class Main extends Application {
    private static Logger logger = LogManager.getLogger();

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setWidth(800);
        primaryStage.setHeight(500);
        primaryStage.setTitle("Pharmacy Tracker");
        primaryStage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/logo.png"))));

        var shoppingListProperty = new SimpleListProperty<>(loadShoppingList());
        var controller = new SearchPromptController(shoppingListProperty);
        var sceneSwitcher = new SceneSwitcher("/fxml/searchprompt.fxml", controller, primaryStage);
        sceneSwitcher.switchScene();
        primaryStage.show();

        initWebDriver();
    }

    private void initWebDriver() {
        switch (Platform.getCurrent()) {
            case WINDOWS:
            case WIN8:
            case WIN8_1:
            case WIN10:
                System.setProperty("webdriver.chrome.driver", getClass().getResource("/bin/chromedriver.exe").getPath());
                break;
            case LINUX:
                var path = getClass().getResource("/bin/chromedriver").getPath();
                new File(path).setExecutable(true);
                System.setProperty("webdriver.chrome.driver", path);
                break;
        }
    }

    private ObservableList<ShoppingListEntry> loadShoppingList() {
        try {
            var persistence = new Persistence();
            var loadedList = persistence.createShoppingListDao().queryForAll();
            return FXCollections.observableArrayList(loadedList);
        } catch (IOException | SQLException e) {
            logger.error(e.getMessage());
            return FXCollections.observableArrayList();
        }
    }
}
