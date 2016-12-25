package com.i5mc.ban;

import org.bukkit.event.Event;
import org.bukkit.event.EventException;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.plugin.EventExecutor;

import java.sql.Timestamp;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.bukkit.event.player.AsyncPlayerPreLoginEvent.Result.ALLOWED;
import static org.bukkit.event.player.AsyncPlayerPreLoginEvent.Result.KICK_BANNED;
import static org.bukkit.event.player.AsyncPlayerPreLoginEvent.Result.KICK_OTHER;

/**
 * Created on 16-12-25.
 */
public class BanListener implements EventExecutor {

    public static final Integer MAX_LIMIT = 10;
    public final BanPlugin plugin;
    public final Map<String, Banned> map;

    public BanListener(BanPlugin plugin) {
        this.plugin = plugin;
        map = new ConcurrentHashMap<>(0xFF);
    }

    @Override
    public void execute(Listener listener, Event event) throws EventException {
        AsyncPlayerPreLoginEvent login = (AsyncPlayerPreLoginEvent) event;
        if (login.getLoginResult() == ALLOWED) {
            handle(login);
        }
    }

    public void handle(AsyncPlayerPreLoginEvent login) {
        String remote = login.getAddress().getHostAddress();
        if (limit(remote)) {
            login.setLoginResult(KICK_OTHER);
        } else {
            Banned banned = plugin.getDatabase().find(Banned.class)
                    .where()
                    .eq("name", login.getName())
                    .gt("expire", new Timestamp($.now()))
                    .setMaxRows(1)
                    .findUnique();
            if (!$.nil(banned)) {
                login.setLoginResult(KICK_BANNED);
            }
        }
    }

    public boolean limit(String remote) {
        Integer i = plugin.limit.get(remote);
        if ($.nil(i)) {
            i = MAX_LIMIT;
        }
        if (i < 0) return true;
        plugin.limit.put(remote, --i);

        return false;
    }

}
