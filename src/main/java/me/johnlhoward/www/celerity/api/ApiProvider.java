package me.johnlhoward.www.celerity.api;

import me.johnlhoward.www.celerity.objects.Location;
import org.slf4j.Logger;

public abstract class ApiProvider {
    protected String apiKey;
    protected Logger logger;

    public ApiProvider(String apiKey, Logger logger) {
        this.apiKey = apiKey;
        this.logger = logger;
    }

    public abstract Location getLocation(String ipAddress) throws Exception;

    // Optionally, method to validate the API key
    public boolean validateApiKey() {
        return apiKey != null && !apiKey.isEmpty();
    }
}
