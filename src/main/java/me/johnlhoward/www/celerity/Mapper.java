package me.johnlhoward.www.celerity;

import me.johnlhoward.www.celerity.api.ApiLocator;
import me.johnlhoward.www.celerity.db.Database;
import me.johnlhoward.www.celerity.db.DatabaseManager;
import me.johnlhoward.www.celerity.objects.Location;
import me.johnlhoward.www.celerity.util.ServerConfig;
import org.slf4j.Logger;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import static me.johnlhoward.www.celerity.Celerity.config;

public class Mapper {
    private final Logger logger;
    private ApiLocator apiLocator = null;
    private DatabaseManager databaseManager = null;
    private final ConcurrentHashMap<UUID, CompletableFuture<InetSocketAddress>> serverSearchFutures = new ConcurrentHashMap<>();
    public Mapper(Logger logger){
        this.logger = logger;
        if(config.isDbCacheEnabled()){
            databaseManager = new DatabaseManager(logger);
        }
        if(config.isApiEnabled()){
            apiLocator = new ApiLocator(logger);
        }
    }

    /**
     * What an algorithm this is... I hope it works!
     */
    private static double haversine(Location loc1, Location loc2) {
        double lat1 = loc1.getLatitude();
        double lon1 = loc1.getLatitude();
        double lat2 = loc2.getLatitude();
        double lon2 = loc2.getLatitude();
        final int R = 6371; // Radius of the Earth in kilometers
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }


    /**
     * Calculates the nearest server to a location
     * @param location The location from where to calculate from
     * @return The server nearest to provided location
     */
    private InetSocketAddress getNearestServer(Location location) {
        logger.debug("Getting nearest server to location: " + location);

        InetSocketAddress nearestServer = null;
        double shortestDistance = Double.MAX_VALUE;

        for (String server : config.getServers()) {
            ServerConfig serverConfig = config.getServerConfig(server);
            if (serverConfig == null) {
                logger.warn("The server configuration for " + server + " is invalid");
                continue;
            }

            Location serverLocation = new Location(serverConfig.latitude(), serverConfig.longitude());
            double distance = haversine(serverLocation, location);

            logger.debug("Distance to " + server + " is " + distance + " km");
            if (distance < shortestDistance) {
                shortestDistance = distance;
                nearestServer = new InetSocketAddress(serverConfig.ip(), serverConfig.port()); // Assuming default port is 25565
            }
        }

        if (nearestServer != null) {
            logger.debug("Nearest server is " + nearestServer);
        } else {
            logger.warn("No servers found or all configurations are invalid");
        }

        return nearestServer;
    }

    /**
     * Starts the search for lowest latency server
     * @param uuid The uuid of the player
     * @param address The ip address of the player
     */
    public void searchServer(UUID uuid, InetAddress address) {
        CompletableFuture<InetSocketAddress> future = new CompletableFuture<>();
        serverSearchFutures.put(uuid, future);

        Location location = null;
        if (config.isDbCacheEnabled()) {
            if (config.isCacheIpEnabled()) {
                location = databaseManager.getLocation(address);
            }
            if (location == null && config.isCacheUsernameEnabled()) {
                location = databaseManager.getLocation(uuid);
            }
        }
        if (location == null && config.isApiEnabled()){
            location = apiLocator.getLocation(address.getHostAddress());
            if (location !=null && config.isDbCacheEnabled()) {
                databaseManager.save(uuid, address, location);
            }
        }

        if (location != null) {
            logger.debug("Found location: " + location);
            InetSocketAddress nearestServer = getNearestServer(location);
            future.complete(nearestServer);
        } else {
            future.complete(null);
        }
    }

    /**
     * Returns the result of the searchServer call or waits for
     * it to finish.
     * @param uuid The uuid of the player
     * @return Returns the address object for the transfer packer
     * or returns null if not to transfer the player
     */
    public InetSocketAddress getServer(UUID uuid){
        try {
            CompletableFuture<InetSocketAddress> future = serverSearchFutures.get(uuid);
            if (future != null) {
                return future.get(10, TimeUnit.SECONDS);
            } else {
                return null;
            }
        } catch (Exception e) {
            logger.error("Error while waiting for server search to complete", e);
            return null;
        }
    }


}
