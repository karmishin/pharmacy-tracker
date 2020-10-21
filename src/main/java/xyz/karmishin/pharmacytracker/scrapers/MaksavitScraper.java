package xyz.karmishin.pharmacytracker.scrapers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import xyz.karmishin.pharmacytracker.entities.Item;
import xyz.karmishin.pharmacytracker.entities.Location;

public class MaksavitScraper implements Scraper {
	private static final Logger logger = LogManager.getLogger();
	
	public List<Item> findItems(String query) throws IOException {
		String url = "https://maksavit.ru/catalog/?q=" + query;
		logger.debug("connecting to " + url);
		Document doc = Jsoup.connect(url).get();
		logger.debug("connection successful");
		List<Item> itemList = new ArrayList<>();
		
		for (Element result : doc.select(".catalog_item")) {
			String itemPrice = result.select(".price").text();
			if (itemPrice.isBlank()) continue;			
			String itemTitle = result.select(".item-title").text();
			String itemStock = result.select(".item-stock > .value").text();
			String itemUrl = "https://maksavit.ru" + result.select(".item-title > a").attr("href");
			
			Item item = new Item(itemTitle, itemPrice, itemStock, itemUrl);
			itemList.add(item);
			logger.debug("scraped item '" + itemTitle + "'");
		}
		
		return itemList;
	}

	public void findLocations(Item item) throws IOException {
		String url = item.getUrl();
		Document doc = Jsoup.connect(url).get();
		
		for (Element element : doc.select(".pharmacy-item")) {
			String locationStock = element.attr("data-available");
			if (locationStock.contentEquals("-1")) continue;
			String locationAddress = element.attr("data-address");
			String locationPrice = element.attr("data-price");
			
			item.addLocation(new Location(locationAddress, locationPrice, locationStock));
		}
		
	}
}
