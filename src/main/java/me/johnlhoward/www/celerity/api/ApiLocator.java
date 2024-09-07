package me.johnlhoward.www.celerity.api;

import me.johnlhoward.www.celerity.objects.Location;
import org.slf4j.Logger;

import static me.johnlhoward.www.celerity.Celerity.config;

public class ApiLocator {
    private final ApiProvider apiProvider;
    private final Logger logger;
    public ApiLocator(Logger logger) {
        this.logger = logger;

        String providerName = config.getApiProvider();
        String apiKey = config.getApiKey();

        if ("ipgeolocation".equalsIgnoreCase(providerName)) {
            apiProvider = new IpGeolocationProvider(apiKey, logger);
            logger.debug("Loaded api provider: ipgeolocation");
        } else {
            logger.warn("Unknown API Provider: " + providerName);
            logger.info("Will try to use the default API Provider (ipgeolocation)");
            apiProvider = new IpGeolocationProvider(apiKey, logger);
            logger.debug("Loaded api provider: ipgeolocation");
        }

    }

    public Location getLocation(String ipAddress){
        try {
            return apiProvider.getLocation(ipAddress);
        } catch (Exception e) {
            logger.error("Error fetching location data for IP: " + ipAddress, e);
            return null;
        }
    }

}
