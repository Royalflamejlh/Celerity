package me.johnlhoward.www.celerity;

import com.google.inject.Inject;
import com.velocitypowered.api.event.EventTask;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PostLoginEvent;
import com.velocitypowered.api.event.connection.PreLoginEvent;
import com.velocitypowered.api.network.ProtocolVersion;
import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.text.Component;
import org.slf4j.Logger;

public final class Listener {

    private Director director;
    private Logger logger;

    public Listener(Director director, Logger logger) {
        this.director = director;
        this.logger = logger;
    }

    // TODO: Maybe should do on post login?

    @Subscribe
    public EventTask onPreLogin(PreLoginEvent event) {
        if(event.getConnection().getProtocolVersion().greaterThan(ProtocolVersion.MINECRAFT_1_20_3)){
            return EventTask.async(director.lookup(event.getUniqueId(), event.getConnection()));
        }
        event.setResult(PreLoginEvent.PreLoginComponentResult.denied(Component.text("TODO: Add configurable text to deny those with old versions")));

        return null;
    }

    @Subscribe
    public EventTask onPostLogin(PostLoginEvent event) {
        return EventTask.async(director.redirect(event.getPlayer()));
    }
}
