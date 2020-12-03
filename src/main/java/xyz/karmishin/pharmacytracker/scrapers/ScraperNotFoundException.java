package xyz.karmishin.pharmacytracker.scrapers;

public class ScraperNotFoundException extends Exception {
    ScraperNotFoundException(String requestedService) {
        super(requestedService);
    }
}
