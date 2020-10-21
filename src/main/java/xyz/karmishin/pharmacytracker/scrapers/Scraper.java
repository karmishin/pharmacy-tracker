package xyz.karmishin.pharmacytracker.scrapers;

import java.io.IOException;
import java.util.List;

import xyz.karmishin.pharmacytracker.entities.Item;

public interface Scraper {
	/**
	 * Select items on the search result page and add them to a list.
	 * @param query
	 * @return a list containing all found items
	 * @throws IOException
	 */
	public List<Item> findItems(String query) throws IOException;
	
	/**
	 * Select locations on the item page and add them to the {@link Item}'s location list.
	 * @param item
	 * @throws IOException
	 */
	public void findLocations(Item item) throws IOException;
}
