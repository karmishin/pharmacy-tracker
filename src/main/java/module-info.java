module xyz.karmishin.pharmacytracker {
    exports xyz.karmishin.pharmacytracker;

    opens xyz.karmishin.pharmacytracker.entities to javafx.base;
    opens xyz.karmishin.pharmacytracker.controllers to javafx.fxml;

    requires java.net.http;
    requires transitive javafx.controls;
    requires transitive javafx.fxml;
    requires org.jsoup;
    requires com.fasterxml.jackson.databind;
    requires org.apache.logging.log4j;
}