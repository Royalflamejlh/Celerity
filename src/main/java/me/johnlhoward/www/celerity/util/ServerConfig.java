package me.johnlhoward.www.celerity.util;

public record ServerConfig(String ip, int port, double latitude, double longitude) {
    @Override
    public String toString() {
        return "ServerConfig{" +
                "ip='" + ip + '\'' +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                '}';
    }
}

