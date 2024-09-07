package me.johnlhoward.www.celerity;

import com.google.inject.Inject;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import me.johnlhoward.www.celerity.util.Config;
import org.slf4j.Logger;
import java.nio.file.Path;


@Plugin(
        id = "celerity",
        name = "Celerity",
        version = "1.0-SNAPSHOT",
        description = "Send players to another proxy based on their ip",
        authors = {"John Howard"}
)
public class Celerity {
    private final ProxyServer server;
    private final Logger logger;
    private final Path dataDirectory;
    private Listener listener;
    private Director director;
    private Mapper mapper;
    public static Config config;

    @Inject
    public Celerity(ProxyServer server, Logger logger, @DataDirectory Path dataDirectory) {
        this.server = server;
        this.logger = logger;
        this.dataDirectory = dataDirectory;
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        logger.info("Initializing Celerity");
        logger.warn("Celerity is an ALPHA plugin and should not be used in production!");

        config = new Config(dataDirectory, logger);

        logger.info("Loaded Config using API Provider: " + config.getApiProvider());

        this.mapper = new Mapper(logger);
        this.director = new Director(mapper, logger);
        this.listener = new Listener(director, logger);


        logger.debug("Registering Listener");
        server.getEventManager().register(this, listener);

    }
}
