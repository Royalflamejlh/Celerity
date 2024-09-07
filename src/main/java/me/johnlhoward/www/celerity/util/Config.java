package me.johnlhoward.www.celerity.util;

import com.moandjiezana.toml.Toml;
import org.slf4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class Config {
    private static Toml tomlConfig;
    private final Logger logger;
    private final File configFile;

    public Config(Path dataDirectory, Logger logger) {
        this.logger = logger;
        configFile = new File(dataDirectory.toFile(), "config.toml");
        loadConfigFile();
        if(tomlConfig.getBoolean("settings.debug")) enableDebugMode();
    }

    public void loadConfigFile() {
        if (!configFile.exists()) {
            createConfigFileFromResources();
        }
        try (FileInputStream fis = new FileInputStream(configFile)) {
            tomlConfig = new Toml().read(fis);
            logger.info("Loaded config.toml successfully.");
        } catch (IOException e) {
            logger.error("Error loading config.toml.", e);
            throw new RuntimeException(e);
        }
    }

    private void createConfigFileFromResources() {
        try (InputStream in = getClass().getClassLoader().getResourceAsStream("config.toml")) {
            if (in == null) {
                logger.error("Default config.toml not found in resources.");
                return;
            }

            // Ensure the directory exists
            if (configFile.getParentFile() != null) {
                configFile.getParentFile().mkdirs();
            }

            // Copy the file from resources to the data directory
            Files.copy(in, configFile.toPath());
            logger.debug("config.toml copied from resources to the data directory.");
        } catch (IOException e) {
            logger.error("Failed to copy config.toml from resources.", e);
        }
    }

    private void enableDebugMode(){
        logger.info("Enabling debug mode");
        try {
            Class<?> levelClass = Class.forName("org.apache.logging.log4j.Level");
            Method setLevel = Class.forName("org.apache.logging.log4j.core.config.Configurator").getMethod("setLevel", String.class, levelClass);
            setLevel.invoke(null, "celerity", levelClass.getField("DEBUG").get(null));
        } catch (ReflectiveOperationException e) {
            logger.warn("while changing log level", e);
        }
        logger.debug(":)");
        logger.info("If you don't see the smiley face then the plugin failed to enable debug logging!");
    }

    public String getApiProvider() {
        return tomlConfig.getString("settings.api.provider");
    }

    public String getApiKey() {
        return tomlConfig.getString("settings.api.key");
    }

    public boolean isApiEnabled() {
        return tomlConfig.getBoolean("settings.api.enabled", false);
    }

    // No transfer domains
    public List<String> getNoTransferDomains() {
        return tomlConfig.getList("settings.no_transfer_domains");
    }

    // DB Cache settings
    public boolean isDbCacheEnabled() {
        return tomlConfig.getBoolean("settings.db_cache.enabled", true);
    }

    public boolean isCacheIpEnabled() {
        return tomlConfig.getBoolean("settings.db_cache.cache_ip", true);
    }

    public boolean isCacheUsernameEnabled() {
        return tomlConfig.getBoolean("settings.db_cache.cache_username", true);
    }

    public int getCacheExpireTime() {
        return tomlConfig.getLong("settings.db_cache.cache_expire", 0L).intValue();
    }

    // Database settings
    public String getDbAddress() {
        return tomlConfig.getString("db.address");
    }

    public String getDbDatabaseName() {
        return tomlConfig.getString("db.database");
    }

    public String getDbTablePrefix() {
        return tomlConfig.getString("db.table_prefix");
    }

    public String getDbUsername() {
        return tomlConfig.getString("db.username");
    }

    public String getDbPassword() {
        return tomlConfig.getString("db.password");
    }
    public Set<String> getServers(){
        return getServerMap().keySet();
    }
    public ServerConfig getServerConfig(String server){
        return getServerMap().get(server);
    }
    public  Map<String, ServerConfig> getServerMap() {
        Map<String, ServerConfig> serverConfigs = new HashMap<>();
        if (tomlConfig.containsTable("servers")) {
            Toml servers = tomlConfig.getTable("servers");
            for (Map.Entry<String, Object> serverEntry : servers.toMap().entrySet()) {
                String serverKey = serverEntry.getKey();
                if (servers.containsTable(serverKey)) {
                    Toml server = servers.getTable(serverKey);
                    String ip = server.getString("ip");
                    int port = server.getLong("port").intValue();
                    double latitude = server.getDouble("latitude");
                    double longitude = server.getDouble("longitude");
                    serverConfigs.put(serverKey, new ServerConfig(ip, port, latitude, longitude));
                }
            }
        }
        return serverConfigs;
    }

}
