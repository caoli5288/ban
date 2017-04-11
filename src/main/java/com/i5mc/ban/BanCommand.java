package com.i5mc.ban;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Iterator;

/**
 * Created on 16-12-25.
 */
public class BanCommand implements CommandExecutor {

    private final BanPlugin plugin;

    public BanCommand(BanPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command i, String label, String[] j) {
        if (j.length == 0) return false;
        ban(sender, Arrays.asList(j).iterator());
        return true;
    }

    private void ban(CommandSender sender, Iterator<String> it) {
        String name = it.next();
        long expire = it.hasNext() ? Long.parseLong(it.next()) * 60000 : plugin.getConfig().getInt("default.expire", 15) * 60000;
        String reason = it.hasNext() ? it.next() : "";
        Player p = plugin.getPlayer(name);
        if (Bukkit.isPrimaryThread()) {
            kick(reason, p);
        } else {
            plugin.run(() -> kick(reason, p), 1);
        }
        sender.sendMessage("操作已完成");
        plugin.execute(() -> plugin.ban(name.toLowerCase(), expire, reason));
    }

    private void kick(String reason, Player p) {
        if (!$.nil(p) && p.isOnline()) {
            p.kickPlayer(reason);
        }
    }

}
