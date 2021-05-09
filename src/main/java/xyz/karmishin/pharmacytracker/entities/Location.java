package xyz.karmishin.pharmacytracker.entities;

public class Location {
	private String address, stock;
	private double price;
	
	public Location(String address, String price, String stock) {
		this.address = address;
		this.price = Double.parseDouble(price);
		this.stock = stock;
	}

	public double getPrice() {
		return price;
	}

	public String getAddress() {
		return address;
	}

	public String getStock() {
		return stock;
	}
}
