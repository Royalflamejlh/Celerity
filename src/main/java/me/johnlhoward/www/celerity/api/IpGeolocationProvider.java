package me.johnlhoward.www.celerity.api;

import java.io.IOException;
import java.net.URL;
import java.util.Scanner;

import me.johnlhoward.www.celerity.objects.Location;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;
import org.slf4j.Logger;

import javax.net.ssl.HttpsURLConnection;

public class IpGeolocationProvider extends ApiProvider {

    private static final String API_URL = "https://api.ipgeolocation.io/ipgeo?apiKey=%s&ip=%s";

    public IpGeolocationProvider(String apiKey, Logger logger) {
        super(apiKey, logger);
    }

    @Override
    public Location getLocation(String ipAddress) throws IOException {
        logger.debug("Starting to get location for IP: " + ipAddress);

        if (!validateApiKey()) {
            logger.warn("Invalid API Key provided.");
            throw new IllegalArgumentException("Invalid API Key");
        }

        String urlString = String.format(API_URL, apiKey, ipAddress);
        logger.debug("Constructed URL for API request: " + urlString);

        URL url = new URL(urlString);
        JSONObject jsonResponse = getJsonObject(url);

        double latitude = jsonResponse.getDouble("latitude");
        double longitude = jsonResponse.getDouble("longitude");

        logger.debug("Retrieved location: Latitude = " + latitude + ", Longitude = " + longitude);

        return new Location(latitude, longitude);
    }

    @NotNull
    private JSONObject getJsonObject(URL url) throws IOException {
        logger.debug("Opening connection to URL: " + url.toString());

        HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.connect();

        int responseCode = conn.getResponseCode();
        if (responseCode != 200) {
            logger.info("Failed to get location data. Response code: " + responseCode);
            throw new IOException("Failed to get location data from API: " + responseCode);
        }

        logger.debug("Successfully connected to the API. Reading response...");

        Scanner sc = new Scanner(url.openStream());
        StringBuilder response = new StringBuilder();
        while (sc.hasNext()) {
            response.append(sc.nextLine());
        }
        sc.close();

        logger.debug("API response successfully read.");

        return new JSONObject(response.toString());
    }

}
