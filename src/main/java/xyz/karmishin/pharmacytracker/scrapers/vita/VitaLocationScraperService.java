package xyz.karmishin.pharmacytracker.scrapers.vita;

import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.scene.control.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import xyz.karmishin.pharmacytracker.entities.Item;
import xyz.karmishin.pharmacytracker.entities.Location;
import xyz.karmishin.pharmacytracker.scrapers.ScraperService;

public class VitaLocationScraperService extends ScraperService<Location> {
    private final Item item;
    private WebDriver webDriver;

    public VitaLocationScraperService(Item item) {
        this.item = item;
    }

    @Override
    public String getPharmacyChain() {
        return "vita";
    }

    @Override
    protected Task<ObservableList<Location>> createTask() {
        var task = new ScraperTask();

        task.setOnFailed(value -> {
            task.getException().printStackTrace();
            var alert = new Alert(Alert.AlertType.ERROR, task.getException().getMessage());
            alert.show();
        });

        return task;
    }

    private class ScraperTask extends Task<ObservableList<Location>> {
        @Override
        protected ObservableList<Location> call() {
            scrape();

            updateProgress(100, 100);
            return partialResults.get();
        }

        private void confirmCity() {
            var confirmButton = webDriver.findElements(By.className("help-city__links")).get(0).findElement(By.tagName("a"));
            confirmButton.click();
        }

        private void scrape() {
            ChromeOptions options = new ChromeOptions();
            webDriver = new ChromeDriver(options);

            webDriver.get(item.getUrl());
            confirmCity();
            webDriver.findElement(By.className("activity-pharms"))
                    .findElement(By.tagName("a"))
                    .click();
            new WebDriverWait(webDriver, 5)
                    .until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.className("cart__modal-apt-item")));

            webDriver.findElements(By.className("cart__modal-apt-item")).forEach(element -> {
                if (!element.isDisplayed()) {
                    var js = (JavascriptExecutor) webDriver;
                    js.executeScript("document.getElementsByClassName(\"ps-container\").item(0).scrollBy(0, 520);");
                }
                var address = element.findElement(By.className("cart__modal-apt-address")).getText();
                var price = item.getPrice();
                var stock = "В наличии";

                var location = new Location(address, price, stock);
                partialResults.get().add(location);
            });

            webDriver.quit();
        }
    }
}
