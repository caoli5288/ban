package com.i5mc.ban;

import com.avaje.ebean.EbeanServer;
import com.google.common.io.ByteStreams;
import com.i5mc.ban.command.BanCommand;
import com.i5mc.ban.command.BanIpCommand;
import com.i5mc.ban.command.MuteCommand;
import com.i5mc.ban.command.UnBanCommand;
import com.i5mc.ban.command.UnBanIpCommand;
import com.i5mc.ban.command.UnMuteCommand;
import com.i5mc.ban.entity.BanLog;
import com.i5mc.ban.entity.BanLogType;
import com.i5mc.ban.entity.Banned;
import com.i5mc.ban.entity.BannedIp;
import com.i5mc.ban.entity.Mute;
import com.mengcraft.simpleorm.DatabaseException;
import com.mengcraft.simpleorm.EbeanHandler;
import com.mengcraft.simpleorm.EbeanManager;
import lombok.Getter;
import lombok.val;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.Timestamp;
import java.util.Arrays;

import static com.i5mc.ban.$.nil;

/**
 * Created on 16-12-25.
 */
@Getter
public class BanPlugin extends JavaPlugin implements Listener {

    private Messenger messenger;
    private EbeanServer dataSource;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        EbeanHandler handler = EbeanManager.DEFAULT.getHandler(this);
        if (handler.isNotInitialized()) {
            handler.define(BanLog.class);
            handler.define(Mute.class);
            handler.define(Banned.class);
            handler.define(BannedIp.class);
            try {
                handler.initialize();
            } catch (DatabaseException e) {
                throw new RuntimeException(e);
            }
        }
        handler.install();
        handler.reflect();

        dataSource = handler.getServer();
        messenger = new Messenger(this);

        getServer().getServicesManager().register(BanPlugin.class,
                this,
                this,
                ServicePriority.Normal
        );

        if (getConfig().getBoolean("listener")) {
            getServer().getPluginManager().registerEvent(AsyncPlayerPreLoginEvent.class
                    , this
                    , EventPriority.HIGHEST
                    , new BanListener(this)
                    , this
            );
            getServer().getPluginManager().registerEvent(AsyncPlayerChatEvent.class
                    , this
                    , EventPriority.LOW
                    , new MuteListener(this)
                    , this
            );
        }

        getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");

        getCommand("ban").setExecutor(new BanCommand(this));
        getCommand("unban").setExecutor(new UnBanCommand(this));
        getCommand("banip").setExecutor(new BanIpCommand(this));
        getCommand("unbanip").setExecutor(new UnBanIpCommand(this));

        getCommand("mute").setExecutor(new MuteCommand(this));
        getCommand("unmute").setExecutor(new UnMuteCommand(this));
    }

    public void run(Runnable r, int i) {
        getServer().getScheduler().runTaskLater(this, r, i);
    }

    public void run(Runnable r, int i, int timer) {
        getServer().getScheduler().runTaskTimer(this, r, i, timer);
    }

    public void ban(CommandSender sender, String name, long expire, String reason) {
        Banned banned = dataSource.find(Banned.class).where("name = ?")
                .setParameter(1, name)
                .findUnique();

        if (nil(banned)) {
            banned = dataSource.createEntityBean(Banned.class);
            banned.setName(name);
        }

        BanLog log = new BanLog();
        log.setName(name);
        log.setLogType(BanLogType.BAN.name());
        log.setDuration(expire);
        log.setReason(reason);
        log.setExecutor(sender.getName());
        log.setLogTime(new Timestamp($.now()));

        banned.setExpire(new Timestamp($.now() + expire));

        banned.setLatestLog(log);
        banned.setLatestUpdate(log.getLogTime());

        dataSource.save(Arrays.asList(log, banned));

        L2Pool.invalid("ban:" + name);
    }

    public void banip(CommandSender sender, Player p, long expire, String reason) {
        String ip = p.getAddress().getAddress().getHostAddress();
        BannedIp banned = dataSource.find(BannedIp.class).where("ip = ?")
                .setParameter(1, ip)
                .findUnique();

        if (nil(banned)) {
            banned = dataSource.createEntityBean(BannedIp.class);
            banned.setIp(ip);
        }

        BanLog log = new BanLog();
        log.setName(ip);
        log.setLogType(BanLogType.BAN_IP.name());
        log.setDuration(expire);
        log.setReason(reason);
        log.setExecutor(sender.getName());
        log.setLogTime(new Timestamp($.now()));

        banned.setExpire(new Timestamp($.now() + expire));

        banned.setLatestLog(log);
        banned.setLatestUpdate(log.getLogTime());

        dataSource.save(Arrays.asList(log, banned));

        L2Pool.invalid("ip:" + ip);
    }

    public void unban(CommandSender sender, String name) {
        Banned banned = dataSource.find(Banned.class).where("name = ?")
                .setParameter(1, name)
                .findUnique();

        if (nil(banned)) {
            return;
        }

        Timestamp expire = banned.getExpire();
        if (nil(expire) || expire.getTime() < System.currentTimeMillis()) {
            return;
        }

        BanLog log = new BanLog();
        log.setName(name);
        log.setLogType(BanLogType.PARDON.name());
        log.setExecutor(sender.getName());
        log.setLogTime(new Timestamp($.now()));

        banned.setExpire(null);
        banned.setLatestLog(log);
        banned.setLatestUpdate(log.getLogTime());

        dataSource.save(Arrays.asList(log, banned));

        L2Pool.invalid("ban:" + name);
    }

    public void unbanip(CommandSender sender, String ip) {
        BannedIp banned = dataSource.find(BannedIp.class).where("ip = ?")
                .setParameter(1, ip)
                .findUnique();

        if (nil(banned)) {
            return;
        }

        Timestamp expire = banned.getExpire();
        if (nil(expire) || expire.getTime() < System.currentTimeMillis()) {
            return;
        }

        BanLog log = new BanLog();
        log.setName(ip);
        log.setLogType(BanLogType.PARDON_IP.name());
        log.setExecutor(sender.getName());
        log.setLogTime(new Timestamp($.now()));

        banned.setExpire(null);
        banned.setLatestLog(log);
        banned.setLatestUpdate(log.getLogTime());

        dataSource.save(Arrays.asList(log, banned));

        L2Pool.invalid("ip:" + ip);
    }

    public Player getPlayer(String name) {
        return getServer().getPlayerExact(name);
    }

    public void execute(Runnable r) {
        getServer().getScheduler().runTaskAsynchronously(this, r);
    }

    public void globalKick(String name, String reason) {
        val itr = getServer().getOnlinePlayers().iterator();
        if (!itr.hasNext()) {
            return;
        }

        val receiver = itr.next();
        val buf = ByteStreams.newDataOutput();
        buf.writeUTF("KickPlayer");
        buf.writeUTF(name);
        buf.writeUTF(nil(reason) ? "" : reason);

        receiver.sendPluginMessage(this, "BungeeCord", buf.toByteArray());
    }
}
