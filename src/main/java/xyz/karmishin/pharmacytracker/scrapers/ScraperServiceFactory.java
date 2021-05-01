package xyz.karmishin.pharmacytracker.scrapers;

import xyz.karmishin.pharmacytracker.entities.Item;
import xyz.karmishin.pharmacytracker.entities.Location;
import xyz.karmishin.pharmacytracker.scrapers.maksavit.MaksavitItemScraperService;
import xyz.karmishin.pharmacytracker.scrapers.maksavit.MaksavitLocationScraperService;
import xyz.karmishin.pharmacytracker.scrapers.vita.VitaItemScraperService;
import xyz.karmishin.pharmacytracker.scrapers.vita.VitaLocationScraperService;

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
            case "vita":
                return new VitaLocationScraperService(item);
            default:
                throw new ScraperNotFoundException(pharmacyChain);
        }
    }
}
