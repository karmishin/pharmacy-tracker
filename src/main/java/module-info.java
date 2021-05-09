module xyz.karmishin.pharmacytracker {
    exports xyz.karmishin.pharmacytracker;

    opens xyz.karmishin.pharmacytracker.entities to javafx.base, com.fasterxml.jackson.databind, ormlite.core;
    opens xyz.karmishin.pharmacytracker.controllers to javafx.fxml;

    requires java.net.http;
    requires transitive javafx.controls;
    requires transitive javafx.fxml;
    requires transitive javafx.web;
    requires transitive javafx.swing;
    requires org.jsoup;
    requires com.fasterxml.jackson.databind;
    requires org.apache.logging.log4j;
    requires selenium.chrome.driver;
    requires selenium.api;
    requires selenium.support;
    requires com.google.zxing;
    requires com.google.zxing.javase;
    requires ormlite.core;
    requires ormlite.jdbc;
    requires java.sql;
}