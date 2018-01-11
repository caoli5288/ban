# Ban
Simple ban-manager for bukkit-based minecraft server with optional command-only mode and query cache. Fetch player by name(not uuid) with case ignored.

## Usage
Time support unit suffix like 1m, 1h and 1d. Non-suffix time parsing to minutes.
* /ban \<player> [time] [message]
* /unban \<player>
* /banip \<player> [time|?unlimited] [message]
* /unbanip \<ip>
* /mute \<player> [time] [message]
* /unmute \<player>
