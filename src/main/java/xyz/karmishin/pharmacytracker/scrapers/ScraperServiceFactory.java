package xyz.karmishin.pharmacytracker.scrapers;

import xyz.karmishin.pharmacytracker.entities.Item;
import xyz.karmishin.pharmacytracker.entities.Location;

public class ScraperServiceFactory {
    public static ScraperService<Item> makeItemScraperService(String query, String pharmacyChain)
            throws ScraperNotFoundException {
        switch (pharmacyChain) {
            case "maksavit":
                return new MaksavitItemScraperService(query);
            case "vita":
                return new VitaItemScraperService(query);
            default:
                throw new ScraperNotFoundException(pharmacyChain);
        }
    }

    public static ScraperService<Location> makeLocationScraperService(Item item, String pharmacyChain)
            throws ScraperNotFoundException {
        switch (pharmacyChain) {
            case "maksavit":
                return new MaksavitLocationScraperService(item);
            default:
                throw new ScraperNotFoundException(pharmacyChain);
        }
    }
}
