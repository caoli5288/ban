package com.i5mc.ban;

import lombok.AllArgsConstructor;
import lombok.val;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.Arrays;

import static java.util.concurrent.CompletableFuture.runAsync;

/**
 * Created by on 2017/8/26.
 */
@AllArgsConstructor
public class BanIpCommand implements CommandExecutor {

    private BanPlugin plugin;

    @Override
    public boolean onCommand(CommandSender op, Command _i, String label, String[] input) {
        if (input.length == 0) return false;
        val itr = Arrays.asList(input).iterator();
        val p = Bukkit.getPlayerExact(itr.next());
        if ($.nil(p)) {
            plugin.messenger.send(op, "default.offline", ChatColor.RED + "玩家不在线");
            return false;
        }
        long t = itr.hasNext() ? ($.now() + Long.parseLong(itr.next()) * 86400000L) : BanPlugin.MAX_EXPIRE;
        runAsync(() -> plugin.banIp(op, p, t, itr.hasNext() ? itr.next() : ""));
        plugin.messenger.send(op, "default.commit", ChatColor.GREEN + "操作已提交");
        return true;
    }
}
