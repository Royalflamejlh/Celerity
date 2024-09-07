package me.johnlhoward.www.celerity.db;

import me.johnlhoward.www.celerity.objects.Location;
import org.slf4j.Logger;

import java.net.InetAddress;
import java.sql.SQLException;
import java.util.UUID;

import static me.johnlhoward.www.celerity.Celerity.config;

public class DatabaseManager {
    private final Database database;
    private Logger logger;
    public DatabaseManager(Logger logger){
        this.logger = logger;

        DatabaseConfig dbConfig = new DatabaseConfig();

        dbConfig.username = config.getDbUsername();
        dbConfig.password = config.getDbPassword();
        dbConfig.prefix = config.getDbTablePrefix();
        dbConfig.address = config.getDbAddress();
        dbConfig.database = config.getDbDatabaseName();

        database = new Database(dbConfig);
        try {
            database.connect();
        } catch (SQLException e) {
            logger.warn("Failed to connect to database. Please check your configuration.");
            logger.warn(e.getLocalizedMessage());
        }
    }
    public Location getLocation(InetAddress address) {
        return database.getCachedLocation(address);
    }
    public Location getLocation(UUID uuid) {
        return database.getCachedLocation(uuid);
    }
    public void save(UUID uuid, InetAddress address, Location location) {
        database.cacheLocationData(uuid, address, location);
    }
}
