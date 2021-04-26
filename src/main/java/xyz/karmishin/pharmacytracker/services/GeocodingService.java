package xyz.karmishin.pharmacytracker.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.scene.control.Alert;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;

public class GeocodingService extends Service<String> {
    private StringProperty address = new SimpleStringProperty();

    public GeocodingService(String address) {
        this.address.set(address);
    }

    @Override
    protected Task<String> createTask() {
        var task = new GeocodingTask();

        task.setOnFailed(value -> {
            task.getException().printStackTrace();
            var alert = new Alert(Alert.AlertType.ERROR, task.getException().getMessage());
            alert.show();
        });

        return task;
    }

    private class GeocodingTask extends Task<String> {
        @Override
        protected String call() throws Exception {
            var fixedAddress = fixAddress();
            var request = HttpRequest.newBuilder()
                    .GET()
                    .uri(new URI("https://nominatim.openstreetmap.org/search?q=" + fixedAddress + "&format=json"))
                    .build();
            var httpClient = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.ALWAYS).build();
            var response = httpClient.send(request, BodyHandlers.ofString());

            return parseResponse(response);
        }

        private String fixAddress() {
            return address.get()
                    .concat(",Нижний Новгород") // TODO
                    .replace("д.", "")
                    .replaceAll(" ", "+")
                    .replaceAll("пр-т.", "проспект");
        }

        private String parseResponse(HttpResponse<String> response) throws JsonProcessingException {
            var mapper = new ObjectMapper();
            var jsonResults = mapper.readTree(response.body());
            var firstResult = jsonResults.get(0);

            var lat = firstResult.get("lat").asText();
            var lon = firstResult.get("lon").asText();
            return lat + "," + lon;
        }
    }
}
