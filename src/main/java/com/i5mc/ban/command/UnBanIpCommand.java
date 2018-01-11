package com.i5mc.ban.command;

import com.i5mc.ban.BanPlugin;
import lombok.AllArgsConstructor;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import static java.util.concurrent.CompletableFuture.runAsync;

/**
 * Created by on 2017/8/26.
 */
@AllArgsConstructor
public class UnBanIpCommand implements CommandExecutor {

    private BanPlugin plugin;

    @Override
    public boolean onCommand(CommandSender sender, Command _i, String lab, String[] input) {
        if (input.length == 0) return false;
        runAsync(() -> plugin.unbanip(sender, input[0]));
        plugin.getMessenger().send(sender, "default.commit", ChatColor.GREEN + "操作已提交");
        return true;
    }
}
