package xyz.karmishin.pharmacytracker.scrapers;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import xyz.karmishin.pharmacytracker.entities.Item;
import xyz.karmishin.pharmacytracker.entities.Location;

public class MaksavitLocationScraperService extends ScraperService<Location> {
    private static Logger logger = LogManager.getLogger();

    private final Item item;

    public MaksavitLocationScraperService(Item item) {
        this.item = item;
    }

    @Override
    public String getPharmacyChain() {
        return "maksavit";
    }

    private String determineStock(String stockAttribute) {
        switch (stockAttribute) {
            case "-1":
                return "Под заказ";
            case "1":
                return "1 шт.";
            case "2":
                return "2+ шт.";
            case "3":
                return "5+ шт.";
            default:
                return stockAttribute;
        }
    }

    @Override
    protected Task<ObservableList<Location>> createTask() {
        var task = new ScraperTask();

        task.setOnFailed(value -> {
            task.getException().printStackTrace();
            var alert = new Alert(AlertType.ERROR, task.getException().getMessage());
            alert.show();
        });

        return task;
    }

    private class ScraperTask extends Task<ObservableList<Location>> {
        @Override
        protected ObservableList<Location> call() throws Exception {
            Document document = Jsoup.connect(item.getUrl()).get();
            Elements elements = document.select(".pharmacy-item");

            for (Element element : elements) {
                if (isCancelled()) {
                    logger.debug("task cancelled");
                    break;
                }

                String locationStock = determineStock(element.attr("data-available"));
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
    }

}
