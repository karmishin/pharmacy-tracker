package xyz.karmishin.pharmacytracker.scrapers;

import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;

public abstract class ScraperService<T> extends Service<ObservableList<T>> {
    public abstract String getPharmacyChain();

    protected ReadOnlyObjectWrapper<ObservableList<T>> partialResults = new ReadOnlyObjectWrapper<>(this,
            "partialResults", FXCollections.observableArrayList());

    public ObservableList<T> getPartialResults() {
        return partialResults.get();
    }

    public ReadOnlyObjectProperty<ObservableList<T>> partialResultsProperty() {
        return partialResults.getReadOnlyProperty();
    }
}