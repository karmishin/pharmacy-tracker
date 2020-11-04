package xyz.karmishin.pharmacytracker.scrapers;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import xyz.karmishin.pharmacytracker.entities.Item;
import xyz.karmishin.pharmacytracker.entities.Location;

public class MaksavitLocationScraperService extends Service<ObservableList<Location>> {
	private static Logger logger = LogManager.getLogger();


    private final Item item; 
    private ReadOnlyObjectWrapper<ObservableList<Location>> partialResults = new ReadOnlyObjectWrapper<>(this,
            "partialResults", FXCollections.observableArrayList());

    public MaksavitLocationScraperService(Item item) {
        this.item = item;
    }

    public ObservableList<Location> getPartialResults() {
        return partialResults.get();
    }

    public ReadOnlyObjectProperty<ObservableList<Location>> partialResultsProperty() {
        return partialResults.getReadOnlyProperty();
    }

    @Override
    protected Task<ObservableList<Location>> createTask() {
        return new Task<ObservableList<Location>>() {
            @Override
            protected ObservableList<Location> call() throws Exception {
                Document document = Jsoup.connect(item.getUrl()).get();
                Elements elements = document.select(".pharmacy-item");

                for (Element element : elements) {
                    if (isCancelled()) {
                        logger.debug("task cancelled");
                        break;
                    }

                    String locationStock = element.attr("data-available");
                    if (locationStock.contentEquals("-1")) continue;
                    String locationAddress = element.attr("data-address");
                    String locationPrice = element.attr("data-price");

                    var location = new Location(locationAddress, locationPrice, locationStock);
                    Platform.runLater(() -> {
                        partialResults.get().add(location);
                        logger.debug("added " + locationAddress);
                    });
                }

                updateProgress(100, 100);
                return partialResults.get();
            }

        };
    }
    
    
}
