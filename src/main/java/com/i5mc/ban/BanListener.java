package com.i5mc.ban;

import lombok.RequiredArgsConstructor;
import org.bukkit.event.Event;
import org.bukkit.event.EventException;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.plugin.EventExecutor;

import java.sql.Timestamp;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static org.bukkit.event.player.AsyncPlayerPreLoginEvent.Result.ALLOWED;
import static org.bukkit.event.player.AsyncPlayerPreLoginEvent.Result.KICK_BANNED;
import static org.bukkit.event.player.AsyncPlayerPreLoginEvent.Result.KICK_OTHER;

/**
 * Created on 16-12-25.
 */
@RequiredArgsConstructor
public class BanListener implements EventExecutor {

    public static final Integer MAX_LIMIT = 10;
    public final BanPlugin plugin;
    public final ConcurrentMap<String, Banned> map = new ConcurrentHashMap<>();

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
            login.setKickMessage("服务器繁忙，请稍后尝试登陆");
            login.setLoginResult(KICK_OTHER);
        } else {
            process(login, login.getName().toLowerCase());
        }
    }

    private void process(AsyncPlayerPreLoginEvent login, String who) {
        Timestamp now = new Timestamp($.now());
        Banned banned = map.get(who);
        if ($.nil(banned)) {
            banned = fetch(who, now);
        }
        if (!$.nil(banned)) {
            if (banned.getExpire().after(now)) {
                login.setLoginResult(KICK_BANNED);
                String r = banned.getReason();
                if (r.isEmpty()) {
                    r = "系统封禁";
                }
                login.setKickMessage(r + " 至 " + banned.getExpire().toString());
            } else {
                map.remove(who, banned);
            }
        }
    }

    private Banned fetch(String who, Timestamp now) {
        Banned banned = plugin.getDatabase().find(Banned.class)
                .where()
                .eq("name", who)
                .gt("expire", now)
                .setMaxRows(1)
                .findUnique();
        if (!$.nil(banned)) {
            map.put(who, banned);
            plugin.run(() -> map.remove(who, banned), 6000);
        }
        return banned;
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
