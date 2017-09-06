package com.i5mc.ban;

import lombok.RequiredArgsConstructor;
import lombok.val;
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

    public final BanPlugin plugin;
    public final ConcurrentMap<String, BannedIp> ip = new ConcurrentHashMap<>();
    public final ConcurrentMap<String, Banned> name = new ConcurrentHashMap<>();

    @Override
    public void execute(Listener listener, Event event) throws EventException {
        AsyncPlayerPreLoginEvent login = (AsyncPlayerPreLoginEvent) event;
        if (login.getLoginResult() == ALLOWED) {
            handle(login);
        }
    }

    boolean checkIp(String cli) {
        BannedIp i = ip.computeIfAbsent(cli, key -> {
            BannedIp ban = plugin.getDatabase().find(BannedIp.class).where("ip = :ip and expire > now()").setParameter("ip", cli).findUnique();
            plugin.run(() -> ip.remove(cli), 12000);
            return $.nil(ban) ? BannedIp.NIL : ban;
        });
        return i == BannedIp.NIL;
    }

    public void handle(AsyncPlayerPreLoginEvent login) {
        String remote = login.getAddress().getHostAddress();
        if (limit(remote)) {
            login.setKickMessage(plugin.messenger.find("kick.fail", "服务器繁忙，请稍后尝试登陆"));
            login.setLoginResult(KICK_OTHER);
        } else if (!checkIp(remote)) {
            login.setKickMessage(plugin.messenger.find("kick.ipban", "您的网络ip已被封禁"));
            login.setLoginResult(KICK_OTHER);
        } else {
            process(login, login.getName().toLowerCase());
        }
    }

    private void process(AsyncPlayerPreLoginEvent login, String who) {
        Timestamp now = new Timestamp($.now());
        Banned banned = name.get(who);
        if ($.nil(banned) || !banned.getExpire().after(now)) {
            banned = fetch(who);
        }
        if (!$.nil(banned)) {
            login.setLoginResult(KICK_BANNED);
            String reason = banned.getReason();
            if (reason.isEmpty()) {
                reason = plugin.messenger.find("default.reason", "系统封禁");
            }
            login.setKickMessage(plugin.messenger.find("default.title", "§4§l您已被系统封禁暂时无法登陆游戏，原因如下") + "\n"
                    + reason + "\n"
                    + "解封时间 " + banned.getExpire().toString() + "\n"
                    + "\n"
                    + plugin.messenger.find("default.tail", "如需申诉请前往 www.i5mc.com"));
        }
    }

    private Banned fetch(String who) {
        val banned = plugin.getDatabase().find(Banned.class)
                .where("name = :who and expire > now()")
                .setParameter("who", who)
                .findUnique();
        if (!$.nil(banned)) {
            name.put(who, banned);
            plugin.run(() -> name.remove(who, banned), 12000);
        }
        return banned;
    }

    public boolean limit(String remote) {
        Integer i = plugin.limit.get(remote);
        if ($.nil(i)) {
            i = BanPlugin.MAX_LIMIT;
        }
        if (i < 0) return true;
        plugin.limit.put(remote, --i);

        return false;
    }

}
