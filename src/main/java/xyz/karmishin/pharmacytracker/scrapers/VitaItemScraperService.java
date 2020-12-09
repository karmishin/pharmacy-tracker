package xyz.karmishin.pharmacytracker.scrapers;

import java.io.IOException;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Redirect;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.ObjectMapper;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import xyz.karmishin.pharmacytracker.entities.Item;

public class VitaItemScraperService extends ScraperService<Item> {
    private StringProperty name = new SimpleStringProperty();

    public VitaItemScraperService(String name) {
        this.name.set(name);
    }

    @Override
    public String getPharmacyChain() {
        return "vita";
    }

    @Override
    protected Task<ObservableList<Item>> createTask() {
        var task = new ItemScraperTask();

        task.setOnFailed(value -> {
            task.getException().printStackTrace();
            var alert = new Alert(AlertType.ERROR, task.getException().getMessage());
            alert.show();
        });

        return task;
    }

    private class ItemScraperTask extends Task<ObservableList<Item>> {
        final String query = name.get();

        @Override
        protected ObservableList<Item> call() throws Exception {
            var response = connect();
            parseResponse(response);

            updateProgress(100, 100);
            return partialResults.get();
        }

        private HttpResponse<String> connect() throws IOException, InterruptedException {
            var request = HttpRequest.newBuilder()
                    .header("User-Agent", "curl/7.71.1")
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(BodyPublishers.ofString("q=" + query))
                    .uri(URI.create("https://vitaexpress.ru/ajax/ajax-search.php")).build();

            CookieHandler.setDefault(new CookieManager());

            var httpClient = HttpClient.newBuilder()
                    .cookieHandler(CookieHandler.getDefault())
                    .followRedirects(Redirect.NORMAL).build();

            var response = httpClient.send(request, BodyHandlers.ofString());
            return response;
        }

        public void parseResponse(HttpResponse<String> response) throws JsonParseException, IOException {            
            var mapper = new ObjectMapper();
            var jsonNode = mapper.readTree(response.body());
            var resultsArray = jsonNode.get("GOODS");

            var elements = resultsArray.elements();
            elements.forEachRemaining(element -> {
                String title = element.get("NAME").asText();
                String price = element.get("PRICE").asText(); // TODO
                String stock = element.get("count").asText();
                String url = element.get("URL").asText();

                var item = new Item(title, price, stock, url);
                Platform.runLater(() -> {
                    partialResults.get().add(item);
                });
            });
        }
    }
}
