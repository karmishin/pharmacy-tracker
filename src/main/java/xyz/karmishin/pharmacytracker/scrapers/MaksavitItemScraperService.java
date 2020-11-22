package xyz.karmishin.pharmacytracker.scrapers;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import xyz.karmishin.pharmacytracker.entities.Item;

public class MaksavitItemScraperService extends Service<ObservableList<Item>> {
	private static Logger logger = LogManager.getLogger();

	private StringProperty name = new SimpleStringProperty();
	private ReadOnlyObjectWrapper<ObservableList<Item>> partialResults = new ReadOnlyObjectWrapper<>(this,
			"partialResults", FXCollections.observableArrayList());

	public MaksavitItemScraperService(String name) {
		this.name.set(name);
	}

	public ObservableList<Item> getPartialResults() {
		return partialResults.get();
	}

	public ReadOnlyObjectProperty<ObservableList<Item>> partialResultsProperty() {
		return partialResults.getReadOnlyProperty();
	}

	@Override
	protected Task<ObservableList<Item>> createTask() {
		final String url = "https://maksavit.ru/catalog/?q=" + name.get();

		return new Task<ObservableList<Item>>() {
			@Override
			protected ObservableList<Item> call() throws IOException {
				logger.debug("connecting to " + url);
				Document firstPage = Jsoup.connect(url).get();
				processPage(firstPage);

				/* Try to find a pagination element. */
				Elements pagination = firstPage.select(".nums > a");

				/* Handle multiple pages. */
				if (!pagination.isEmpty()) {
					/* Determine the number of pages by the last element in the pagination. */
					int numberOfPages = Integer.parseInt(pagination.last().text());

					/* Start with the second page, as we've already processed the first one. */
					for (int i = 2; i <= numberOfPages; i++) {
						if (isCancelled()) {
							logger.debug("task cancelled");
							break;
						}

						String nextPageUrl = url + "&PAGEN_1=" + i;
						logger.debug("connecting to " + nextPageUrl);
						Document nextPage = Jsoup.connect(nextPageUrl).get();

						logger.debug("processing page " + i + "/" + numberOfPages);
						processPage(nextPage);

						updateProgress(i, numberOfPages);
					}
				}

				updateProgress(100, 100);
				logger.debug("task complete");
				return partialResults.get();
			}

			private void processPage(Document document) {
				Elements elements = document.select(".catalog_item");

				for (Element element : elements) {
					if (isCancelled())
						break;

					String itemPrice = element.select(".price").text();
					if (itemPrice.isBlank())
						continue;
					String itemName = element.select(".item-title").text();
					String itemStock = element.select(".item-stock > .value").text();
					String itemUrl = "https://maksavit.ru" + element.select(".item-title > a").attr("href");

					var item = new Item(itemName, itemPrice, itemStock, itemUrl);

					Platform.runLater(() -> {
						partialResults.get().add(item);
						logger.debug("added " + itemName);
					});
				}
			}
		};
	}

}