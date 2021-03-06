package com.i5mc.ban.command;

import com.i5mc.ban.BanPlugin;
import com.i5mc.ban.TimeTickUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;

import static com.i5mc.ban.$.nil;

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

    private void ban(CommandSender sender, Iterator<String> itr) {
        String name = itr.next();
        long expire = itr.hasNext() ? TimeTickUtil.toTime(itr.next(), TimeUnit.MILLISECONDS) : plugin.getConfig().getInt("default.expire", 15) * 60000;
        String reason = itr.hasNext() ? itr.next() : "";
        Player p = plugin.getPlayer(name);
        if (nil(p)) {
            plugin.globalKick(name, reason);
        } else {
            if (Bukkit.isPrimaryThread()) {
                kick(reason, p);
            } else {
                plugin.run(() -> kick(reason, p), 1);
            }
        }
        sender.sendMessage("操作已完成");
        plugin.execute(() -> plugin.ban(sender, name.toLowerCase(), expire, reason));
    }

    private void kick(String reason, Player p) {
        if (!nil(p) && p.isOnline()) {
            p.kickPlayer(reason);
        }
    }

}
