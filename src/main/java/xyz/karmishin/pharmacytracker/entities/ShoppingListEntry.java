package xyz.karmishin.pharmacytracker.entities;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "shopping_list")
public class ShoppingListEntry {
    @DatabaseField (generatedId = true)
    private int id;
    @DatabaseField
    private String title, address, coordinate;
    @DatabaseField
    private double price;

    public ShoppingListEntry() {
        // ORMLite no-arg constructor
    }

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
