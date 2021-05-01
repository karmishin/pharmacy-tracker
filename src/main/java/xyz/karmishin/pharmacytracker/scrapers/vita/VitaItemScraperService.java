package xyz.karmishin.pharmacytracker.scrapers.vita;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;
import xyz.karmishin.pharmacytracker.entities.Item;
import xyz.karmishin.pharmacytracker.scrapers.ScraperService;

public class VitaItemScraperService extends ScraperService<Item> {
    private static Logger logger = LogManager.getLogger();
    private WebDriver webDriver;

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
        protected ObservableList<Item> call() {
            scrape();

            updateProgress(100, 100);
            return partialResults.get();
        }

        private void confirmCity() {
            var confirmButton = webDriver.findElements(By.className("help-city__links")).get(0).findElement(By.tagName("a"));
            confirmButton.click();
        }

        private void showMore() {
            var list = webDriver.findElements(By.className("btn-pager"));
            if (!list.isEmpty()) {
                closeSticky();
                JavascriptExecutor js = (JavascriptExecutor) webDriver;
                js.executeScript("document.getElementsByClassName(\"btn-pager\").item(0).click()");
                new WebDriverWait(webDriver, 2000);
            }
        }

        private void closeSticky() {
            var sticky = webDriver.findElements(By.className("info-sticky__close"));
            if (!sticky.isEmpty())
                sticky.get(0).click();
        }

        public void scrape() {
            ChromeOptions options = new ChromeOptions();
            webDriver = new ChromeDriver(options);
            webDriver.get("https://vitaexpress.ru/search/" + query + "/");

            confirmCity();
            showMore();

            var products = webDriver.findElements(By.className("product"));
            products.forEach(webElement -> {
                var link = webElement.findElement(By.className("product__title")).findElement(By.tagName("a"));

                var title = link.getText();
                var url = link.getAttribute("href");
                var price = webElement.findElement(By.className("priceSVG")).findElement(By.tagName("span")).getText();
                var stock = "В наличии";

                var item = new Item(title, price, stock, url);
                Platform.runLater(() -> {
                    partialResults.get().add(item);
                    logger.debug("added " + item.getTitle());
                });
            });

            webDriver.quit();
        }
    }
}
