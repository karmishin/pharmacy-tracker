package xyz.karmishin.pharmacytracker.entities;

public class ShoppingListEntry {
    private String title, address, coordinate;
    private double price;

    public ShoppingListEntry(Item item, Location location, String coordinate) {
        title = item.getTitle();
        address = location.getAddress();
        price = location.getPrice();
        this.coordinate = coordinate;
    }

    public String getTitle() {
        return title;
    }

    public String getAddress() {
        return address;
    }

    public String getCoordinate() {
        return coordinate;
    }

    public double getPrice() {
        return price;
    }
}
