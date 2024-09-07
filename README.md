# Celerity - [ALPHA]

# USE AT YOUR OWN RISK (NOT IN PRODUCTION)

## About
The goal of this plugin is to be able to redirect user to different proxies depending on their location.

Imagine you have a two network setup:

Server A (east.myserver.com) in NY and Server B (west.myserver.com) in LA

This plugin allows for all players to type in myserver.com and connect to whichever server is closer WITHOUT
proxying their connection.

## Road Map

This is a work in progress but here is a basic roadmap for what needs to be done:
- Set up the cache expiry system.
- Allow clients with old versions to connect through.
- Add more API options
- Allow IP history to be stored locally. SQLite?

## Installing
This is just a drop in Velocity plugin. It will then make a config.toml like such:
```
[settings]

# List of domains where no transfer should occur if the user connects using these
no_transfer_domains = ["east.myserver.com", "est.myserver.com"]
debug = false   # Enable to get debug messages. You know the drill

[settings.api]
enabled = true             # Disabling this will stop use from using the api.
provider = "ipgeolocation" # Right now only ipgeolocation (https://ipgeolocation.io/) is supported, go there to make an api key
key = "somekeyyouget"      # If no key is needed, set to None

[settings.db_cache]
enabled = true           # Whether to use the database caching feature
cache_ip = true          # Save IPs in the database
cache_username = true    # Save usernames in the database (may decrease accuracy)
cache_expire = 730       # Hours until a cached entry expires (0 disables expiration)

[db] ## Database Configuration for Cache
address = "120.0.0.1:12345"
database = "fundatabase"
table_prefix = "cel_"
username = "username"
password = "password"

[servers] ## Server Configuration section

[servers.easternserver]
ip = "east.myserver.net"
port = 25565
latitude = 40.7128   # Example: Latitude of New York City
longitude = -74.0060 # Example: Longitude of New York City

[servers.westernserver]
ip = "123.45.68.79"
port = 42069
latitude = 34.0522    # Example: Latitude of Los Angeles
longitude = -118.2437 # Example: Longitude of Los Angeles
```

## Building
You should be able to build this plugin with Maven, using Java 21.