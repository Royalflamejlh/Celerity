package me.johnlhoward.www.celerity.db;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import me.johnlhoward.www.celerity.objects.Location;

import java.net.InetAddress;
import java.sql.*;
import java.util.UUID;

public class Database {

    private final HikariDataSource dataSource;
    private final String tablePrefix;

    public Database(DatabaseConfig dbConfig) {
        String url = "jdbc:mariadb://" + dbConfig.address + "/" + dbConfig.database;
        this.tablePrefix = dbConfig.prefix;

        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setDriverClassName("org.mariadb.jdbc.Driver");
        hikariConfig.setJdbcUrl(url);
        hikariConfig.setUsername(dbConfig.username);
        hikariConfig.setPassword(dbConfig.password);

        this.dataSource = new HikariDataSource(hikariConfig);
    }

    public void connect() throws SQLException {
        try (Connection connection = dataSource.getConnection();
             Statement stmt = connection.createStatement()) {
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS " + tablePrefix + "location_cache (" +
                    "uuid VARCHAR(36), " +
                    "ipAddress VARCHAR(45), " +
                    "latitude DOUBLE, " +
                    "longitude DOUBLE, " +
                    "PRIMARY KEY (uuid, ipAddress))");
        }
    }

    public void cacheLocationData(UUID uuid, InetAddress address, Location location) {
        String query = "REPLACE INTO " + tablePrefix + "location_cache (uuid, ipAddress, latitude, longitude) VALUES (?, ?, ?, ?)";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, uuid.toString());
            pstmt.setString(2, address.getHostAddress());
            pstmt.setDouble(3, location.getLatitude());
            pstmt.setDouble(4, location.getLongitude());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Location getCachedLocation(UUID uuid) {
        String query = "SELECT latitude, longitude FROM " + tablePrefix + "location_cache WHERE uuid = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, uuid.toString());
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return new Location(rs.getDouble("latitude"), rs.getDouble("longitude"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Location getCachedLocation(InetAddress address) {
        String query = "SELECT latitude, longitude FROM " + tablePrefix + "location_cache WHERE ipAddress = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, address.getHostAddress());
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return new Location(rs.getDouble("latitude"), rs.getDouble("longitude"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void close() throws SQLException {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }
}
