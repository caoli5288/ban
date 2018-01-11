package com.i5mc.ban;

import com.i5mc.ban.entity.Banned;
import com.i5mc.ban.entity.BannedIp;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.bukkit.event.Event;
import org.bukkit.event.EventException;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.plugin.EventExecutor;

import java.sql.Timestamp;

import static org.bukkit.event.player.AsyncPlayerPreLoginEvent.Result.ALLOWED;
import static org.bukkit.event.player.AsyncPlayerPreLoginEvent.Result.KICK_BANNED;
import static org.bukkit.event.player.AsyncPlayerPreLoginEvent.Result.KICK_OTHER;

/**
 * Created on 16-12-25.
 */
@RequiredArgsConstructor
public class BanListener implements EventExecutor {

    public final BanPlugin plugin;

    @Override
    public void execute(Listener listener, Event event) throws EventException {
        AsyncPlayerPreLoginEvent login = (AsyncPlayerPreLoginEvent) event;
        if (login.getLoginResult() == ALLOWED) {
            handle(login);
        }
    }

    public void handle(AsyncPlayerPreLoginEvent login) {
        String ip = login.getAddress().getHostAddress();
        if (!validNet(ip)) {
            login.setKickMessage(plugin.getMessenger().find("kick.ipban", "您的网络ip已被封禁"));
            login.setLoginResult(KICK_OTHER);
        } else {
            process(login, login.getName().toLowerCase());
        }
    }

    boolean validNet(String ip) {
        val banned = L2Pool.pull("ip:" + ip, () -> plugin.getDataSource().find(BannedIp.class)
                .where("ip = ? and expire > now()")
                .setParameter(1, ip)
                .findUnique()
        );
        return banned == null || banned.getExpire() == null || banned.getExpire().before(new Timestamp($.now()));
    }

    private void process(AsyncPlayerPreLoginEvent login, String name) {
        Banned banned = banned(name);
        if (banned == null || banned.getExpire() == null || banned.getExpire().before(new Timestamp($.now()))) {
            return;
        }
        login.setLoginResult(KICK_BANNED);
        String reason = banned.getLatestLog().getReason();
        if (reason.isEmpty()) {
            reason = plugin.getMessenger().find("default.reason", "系统封禁");
        }
        login.setKickMessage(plugin.getMessenger().find("default.title", "§4§l您已被系统封禁暂时无法登陆游戏，原因如下") + "\n"
                + reason + "\n"
                + "解封时间 " + banned.getExpire().toString() + "\n"
                + "\n"
                + plugin.getMessenger().find("default.tail", "如需申诉请前往 www.i5mc.com"));
    }

    private Banned banned(String name) {
        return L2Pool.pull("ban:" + name, () -> plugin.getDataSource().find(Banned.class)
                .where("name = ? and expire > now()")
                .setParameter(1, name)
                .findUnique());
    }

}
