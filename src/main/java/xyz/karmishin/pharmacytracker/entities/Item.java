package xyz.karmishin.pharmacytracker.entities;

import java.util.Comparator;
import java.util.List;

public class Item {
	private String title, price, stock, url;

	public Item(String title, String price, String stock, String url) {
		this.title = title;
		this.price = price;
		this.stock = stock;
		this.url = url;
	}

	@Override
	public String toString() {
		return String.format("%s -- %s -- %s", title, price, stock);
	}
	
	public String getTitle() {
		return title;
	}

	public String getStock() {
		return stock;
	}

	public String getPrice() {
		return price;
	}
	
	public String getUrl() {
		return url;
	}
}
