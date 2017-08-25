package com.i5mc.ban;

import com.avaje.ebean.EbeanServer;
import com.mengcraft.simpleorm.DatabaseException;
import com.mengcraft.simpleorm.EbeanHandler;
import com.mengcraft.simpleorm.EbeanManager;
import lombok.val;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.Timestamp;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created on 16-12-25.
 */
public class BanPlugin extends JavaPlugin implements Listener {

    public static final long MAX_EXPIRE = 2147483647000L;
    public static final int MAX_LIMIT = 10;

    public Map<String, Integer> limit;
    Messenger messenger;
    private EbeanServer database;

    public EbeanServer getDatabase() {
        return database;
    }

    @Override
    public void onEnable() {
        saveDefaultConfig();

        EbeanHandler handler = EbeanManager.DEFAULT.getHandler(this);
        if (handler.isNotInitialized()) {
            handler.define(Banned.class);
            try {
                handler.initialize();
            } catch (DatabaseException e) {
                throw new RuntimeException(e);
            }
        }
        handler.install();
        handler.reflect();

        database = handler.getServer();
        messenger = new Messenger(this);

        getServer().getServicesManager().register(BanPlugin.class,
                this,
                this,
                ServicePriority.Normal
        );

        if (getConfig().getBoolean("listener")) {
            limit = new ConcurrentHashMap<>();
            run(() -> limit.clear(), 1200, 1200);
            getServer().getPluginManager().registerEvent(AsyncPlayerPreLoginEvent.class
                    , this
                    , EventPriority.HIGHEST
                    , new BanListener(this)
                    , this
            );
        }

        getCommand("ban").setExecutor(new BanCommand(this));
        getCommand("unban").setExecutor(new UnBanCommand(this));
        getCommand("banip").setExecutor(new BanIpCommand(this));
        getCommand("unbanip").setExecutor(new UnBanIpCommand(this));
    }

    public void run(Runnable r, int i) {
        getServer().getScheduler().runTaskLater(this, r, i);
    }

    public void run(Runnable r, int i, int timer) {
        getServer().getScheduler().runTaskTimer(this, r, i, timer);
    }

    public void ban(CommandSender sender, String name, long expire, String reason) {
        Banned banned = new Banned();
        banned.setName(name);
        banned.setExecutor(sender.getName());
        banned.setExpire(new Timestamp($.now() + expire));
        banned.setReason(reason);

        unBan(name);

        getDatabase().save(banned);
    }

    public void unBan(String name) {
        val list = getDatabase().find(Banned.class).where("name = :name and expire > now()")
                .setParameter("name", name)
                .findList();
        if (!list.isEmpty()) {
            val now = new Timestamp($.now());
            for (Banned banned : list) {
                banned.setExpire(now);
            }
            getDatabase().save(list);
        }
    }

    public void banIp(CommandSender executor, Player who, long expire, String reason) {
        val cli = who.getAddress().getAddress().getHostAddress();
        unBanIp(cli);

        val ban = new BannedIp();
        ban.setIp(cli);
        ban.setExecutor(executor.getName());
        ban.setReason(reason);
        if (expire > $.now()) {
            ban.setExpire(new Timestamp(expire));
        }

        database.save(ban);
    }

    public void unBanIp(String ip) {
        database.createUpdate(BannedIp.class, "DELETE FROM banned_ip WHERE ip = :ip")
                .set("ip", ip)
                .execute();
    }

    public Player getPlayer(String name) {
        return getServer().getPlayerExact(name);
    }

    public void execute(Runnable r) {
        getServer().getScheduler().runTaskAsynchronously(this, r);
    }

}
