package me.johnlhoward.www.celerity;

import com.velocitypowered.api.proxy.InboundConnection;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;


import net.kyori.adventure.text.Component;
import org.slf4j.Logger;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Director {
    private final Logger logger;
    private final Mapper mapper;
    private final Map<UUID, InetSocketAddress> playerServerMap = new HashMap<>();
    public Director(Mapper mapper, Logger logger) {
        this.mapper = mapper;
        this.logger = logger;
    }

    public Runnable lookup(UUID uuid, InboundConnection connection) {
        return () -> {
            try {
                // Look up if user set preference for server in database
                // Look up region of connection.getRemoteAddress() in database
                // If miss hit api to get location of ip address and store it in database
                // Find which configured server is closer to user

                mapper.searchServer(uuid, connection.getRemoteAddress().getAddress());
                logger.debug("Pre Login!");

            } catch (Exception e) {
                logger.error("Error in redirecting player: ", e);
            }
        };
    }

    public Runnable redirect(Player player) {
        return () -> {
            try {
                // Look up if user set preference for server in database
                // Look up region of connection.getRemoteAddress() in database
                // If miss hit api to get location of ip address and store it in database

                // Find which configured server is closer to user
                // Send transfer packet to the user

                InetSocketAddress address = mapper.getServer(player.getUniqueId());

                if(address != null) {
                    player.transferToHost(address);
                    logger.debug("Wrote transfer packet to player");
                    player.disconnect(Component.text("Sent to other server"));
                }
                else{
                    logger.debug("Was not able to find a server for player: " + player);
                }

            } catch (Exception e) {
                logger.error("Error in redirecting player: ", e);
            }
        };
    }
}
