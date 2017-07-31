package com.i5mc.ban;

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

    public Map<String, Integer> limit;
    Messenger messenger;

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

        unban(name);

        getDatabase().save(banned);
    }

    public void unban(String name) {
        val t = new Timestamp($.now());
        val list = getDatabase().find(Banned.class).where("name = :name and expire > :expire")
                .setParameter("name", name)
                .setParameter("expire", t).findList();
        if (!list.isEmpty()) {
            for (Banned banned : list) {
                banned.setExpire(t);
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
