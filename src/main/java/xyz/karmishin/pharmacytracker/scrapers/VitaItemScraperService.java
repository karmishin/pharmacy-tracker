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
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;

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
        var task = new ScraperTask();

        task.setOnFailed(value -> {
            task.getException().printStackTrace();
            var alert = new Alert(AlertType.ERROR, task.getException().getMessage());
            alert.show();
        });

        return task;
    }

    private class ScraperTask extends Task<ObservableList<Item>> {
        final String query = name.get();

        @Override
        protected ObservableList<Item> call() throws Exception {
            var response = connect();
            parseResponse(response);

            updateProgress(100, 100);
            return partialResults.get();
        }

        private HttpResponse<String> connect() throws IOException, InterruptedException {
            var request = HttpRequest.newBuilder().header("User-Agent", "curl/7.71.1")
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(BodyPublishers.ofString("q=" + query))
                    .uri(URI.create("https://vitaexpress.ru/ajax/ajax-search.php")).build();

            CookieHandler.setDefault(new CookieManager());

            var httpClient = HttpClient.newBuilder().cookieHandler(CookieHandler.getDefault())
                    .followRedirects(Redirect.NORMAL).build();

            var response = httpClient.send(request, BodyHandlers.ofString());

            return response;
        }

        public void parseResponse(HttpResponse<String> response) throws JsonParseException, IOException,
                InterruptedException {
            var mapper = new ObjectMapper();
            var jsonNode = mapper.readTree(response.body());
            var resultsArray = jsonNode.get("GOODS");
            var elements = resultsArray.elements();

            var deobfuscator = new PriceDeobfuscator(jsonNode.get("SVG_SPRITE").asText());

            elements.forEachRemaining(element -> {
                String title = element.get("NAME").asText();
                String price = deobfuscator.getActualPrice(element.get("PRICE").asText());
                String stock = element.get("count").asText();
                String url = element.get("URL").asText();

                var item = new Item(title, price, stock, url);
                Platform.runLater(() -> {
                    partialResults.get().add(item);
                });
            });
        }
    }

    private class PriceDeobfuscator {
        private String svgSpriteId;

        private Map<String, String> actualSpriteMap = new HashMap<>();
        private Map<String, String> obfuscatedSpriteMap = new HashMap<>();
        private Map<String, String> deobfuscationMap = new HashMap<>();

        PriceDeobfuscator(String svgSpriteId) throws IOException, InterruptedException {
            this.svgSpriteId = svgSpriteId;

            fillActualSpriteMap();
            fillObfuscatedSpriteMap();
            constructDeobfuscationMap();
        }

        String getActualPrice(String extractedPrice) {
            String result = "";

            char[] digits = extractedPrice.toCharArray();
            for (int i = 0; i < digits.length; i++) {
                String digit = String.valueOf(digits[i]);
                result += deobfuscationMap.get(digit);
            }
            
            return result;
        }

        private void fillActualSpriteMap() {
            actualSpriteMap.put("M93 229c53,0 93,-41 93,-114 0,-74 -40,-115 -93,-115 -53,0 -93,41 -93,115 0,73 40,114 93,114zm0 -36c-30,0 -51,-25 -51,-78 0,-54 21,-79 51,-79 31,0 52,25 52,79 0,53 -21,78 -52,78z", "0");
            actualSpriteMap.put("0,0 0,21 28,21 28,135 53,135 53,0 ", "1");
            actualSpriteMap.put("M96 269l83 -80c44,-40 53,-68 53,-99 0,-55 -44,-90 -113,-90 -51,0 -95,19 -119,52l41 31c17,-22 43,-32 73,-32 39,0 59,16 59,45 0,18 -6,36 -36,65l-125 119 0 39 232 0 0 -50 -148 0z", "2");
            actualSpriteMap.put("M216 183l107 -128 0 -55 -302 0 0 69 202 0 -101 119 0 57 40 0c67,0 97,26 97,65 0,42 -35,67 -94,67 -50,0 -98,-17 -130,-43l-35 64c41,32 104,51 166,51 119,0 175,-65 175,-139 0,-67 -42,-117 -125,-127z", "3");
            actualSpriteMap.put("849,577 683,577 683,388 520,388 520,577 213,577 648,0 462,0 0,604 0,724 514,724 514,937 683,937 683,724 849,724 ", "4");
            actualSpriteMap.put("M1986 2153l-532 0 119 -1287 2392 0 0 -866 -3266 0 -286 3027 1343 0c1152,0 1494,334 1494,866 0,524 -437,850 -1176,850 -628,0 -1232,-214 -1637,-548l-437 802c508,406 1303,644 2090,644 1493,0 2200,-810 2200,-1788 0,-985 -659,-1700 -2304,-1700z", "5");
            actualSpriteMap.put("M568 497c-139,0 -259,43 -334,130l0 -1c0,-285 149,-431 384,-431 88,0 170,16 238,61l86 -176c-84,-53 -204,-80 -329,-80 -363,0 -613,236 -613,665 0,402 200,622 542,622 255,0 457,-157 457,-406 0,-234 -184,-384 -431,-384zm-39 608c-166,0 -259,-93 -259,-211 0,-124 104,-215 254,-215 152,0 250,82 250,213 0,130 -100,213 -245,213z", "6");
            actualSpriteMap.put("0,0 0,181 96,181 96,87 313,87 105,554 215,554 430,69 430,0 ", "7");
            actualSpriteMap.put("M247 184c31,-16 48,-43 48,-79 0,-65 -57,-105 -141,-105 -84,0 -141,40 -141,105 0,36 17,63 47,79 -38,18 -60,49 -60,91 0,70 60,113 154,113 94,0 155,-43 155,-113 0,-42 -23,-73 -62,-91zm-93 -129c43,0 72,19 72,53 0,33 -28,53 -72,53 -45,0 -71,-20 -71,-53 0,-34 28,-53 71,-53zm0 278c-52,0 -83,-22 -83,-61 0,-37 31,-60 83,-60 51,0 84,23 84,60 0,39 -33,61 -84,61z", "8");
            actualSpriteMap.put("M98 0c-55,0 -98,34 -98,87 0,51 40,83 93,83 30,0 55,-9 72,-28l0 0c0,61 -32,93 -83,93 -19,0 -37,-4 -51,-13l-19 37c18,12 44,18 71,18 78,0 132,-51 132,-143 0,-87 -43,-134 -117,-134zm4 131c-32,0 -54,-18 -54,-46 0,-28 22,-46 53,-46 36,0 56,20 56,46 0,26 -22,46 -55,46z", "9");
        }

        private void fillObfuscatedSpriteMap() throws IOException, InterruptedException {
            var document = Jsoup.connect("https://vitaexpress.ru/img/svg/num/num_v" + svgSpriteId + ".svg")
                    .ignoreContentType(true)
                    .get();

            var symbols = document.select("symbol");
            for (Element e : symbols) {
                String number = e.attr("id").subSequence(4, 5).toString();
                String coordinates = e.select("symbol > path").attr("d");
                if (coordinates.isBlank()) 
                    coordinates = e.select("symbol > polygon").attr("points");

                obfuscatedSpriteMap.put(coordinates, number);
            }
        }

        private void constructDeobfuscationMap() {
            obfuscatedSpriteMap.forEach((coordinates, fakeNumber) -> {
                String actualNumber = actualSpriteMap.get(coordinates);
                deobfuscationMap.put(fakeNumber, actualNumber);
            });
        }
    }
}
