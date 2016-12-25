package com.i5mc.ban;

import com.mengcraft.simpleorm.DatabaseException;
import com.mengcraft.simpleorm.EbeanHandler;
import com.mengcraft.simpleorm.EbeanManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created on 16-12-25.
 */
public class BanPlugin extends JavaPlugin implements Listener {

    public Map<String, Integer> limit;

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
            handler.install();
        }
        handler.reflect();

        getServer().getServicesManager().register(BanPlugin.class, this, this, ServicePriority.Normal);

        getCommand("ban").setExecutor(new BanCommand(this));
        getCommand("unban").setExecutor(new UnBanCommand(this));

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
    }

    public void run(Runnable r, int i) {
        getServer().getScheduler().runTaskLater(this, r, i);
    }

    public void run(Runnable r, int i, int timer) {
        getServer().getScheduler().runTaskTimer(this, r, i, timer);
    }

    public void ban(String name, long expire, String reason) {
        Banned banned = new Banned();
        banned.setName(name);
        banned.setExpire(new Timestamp($.now() + expire));
        banned.setReason(reason);

        unban(name);

        getDatabase().save(banned);
    }

    public void unban(String name) {
        Timestamp now = new Timestamp($.now());
        List<Banned> list = getDatabase().find(Banned.class)
                .where()
                .eq("name", name)
                .gt("expire", now)
                .findList();
        if (!list.isEmpty()) {
            for (Banned banned : list) {
                banned.setExpire(now);
            }
            getDatabase().save(list);
        }
    }

    public Player getPlayer(String name) {
        return getServer().getPlayerExact(name);
    }

    public void execute(Runnable r) {
        getServer().getScheduler().runTaskAsynchronously(this, r);
    }

}
