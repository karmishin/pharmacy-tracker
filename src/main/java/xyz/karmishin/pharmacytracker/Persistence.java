package xyz.karmishin.pharmacytracker;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.table.TableUtils;
import xyz.karmishin.pharmacytracker.entities.ShoppingListEntry;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

public class Persistence {
    private String home = System.getProperty("user.home");
    private File storage, databaseFile;
    private JdbcConnectionSource connectionSource;

    public Persistence() throws IOException, SQLException {
        storage = new File(home, ".pharmacy-tracker");
        if (!storage.exists()) {
            storage.mkdirs();
        }

        databaseFile = new File(storage, "database.db");
        if (!databaseFile.exists()) {
            databaseFile.createNewFile();
        }

        connectionSource = new JdbcConnectionSource(getDatabaseUrl());
        TableUtils.createTableIfNotExists(connectionSource, ShoppingListEntry.class);
    }

    public Dao<ShoppingListEntry, String> createShoppingListDao() throws SQLException {
        return DaoManager.createDao(connectionSource, ShoppingListEntry.class);
    }

    public void close() throws IOException {
        connectionSource.close();
    }

    private String getDatabaseUrl() {
        return "jdbc:sqlite:" + databaseFile.getAbsolutePath();
    }
}
