package com.i5mc.ban;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

/**
 * Created on 16-12-25.
 */
public class UnBanCommand implements CommandExecutor {

    public final BanPlugin plugin;

    public UnBanCommand(BanPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command i, String label, String[] j) {
        if (j.length == 0) return false;
        plugin.execute(() -> plugin.unBan(j[0]));
        sender.sendMessage("操作已完成");
        return true;
    }
}
