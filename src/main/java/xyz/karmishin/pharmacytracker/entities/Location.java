package xyz.karmishin.pharmacytracker.entities;

public class Location {
	private String address, stock;
	private double price;
	
	public Location(String address, String price, String stock) {
		this.address = address;
		this.price = Double.parseDouble(price);
		this.stock = stock;
	}

	@Override
	public String toString() {
		return String.format(" * %s -- %s руб. -- %s шт. \n", address, price, stock);
	}

	public double getPrice() {
		return price;
	}	
}
