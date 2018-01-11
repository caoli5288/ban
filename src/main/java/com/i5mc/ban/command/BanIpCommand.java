package com.i5mc.ban.command;

import com.i5mc.ban.$;
import com.i5mc.ban.BanPlugin;
import com.i5mc.ban.TimeTickUtil;
import lombok.AllArgsConstructor;
import lombok.val;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import static java.util.concurrent.CompletableFuture.runAsync;

/**
 * Created by on 2017/8/26.
 */
@AllArgsConstructor
public class BanIpCommand implements CommandExecutor {

    private BanPlugin plugin;

    @Override
    public boolean onCommand(CommandSender sender, Command _i, String label, String[] input) {
        if (input.length == 0) return false;
        val itr = Arrays.asList(input).iterator();
        val p = Bukkit.getPlayerExact(itr.next());
        if ($.nil(p)) {
            plugin.getMessenger().send(sender, "default.offline", ChatColor.RED + "玩家不在线");
            return false;
        }
        long expire = itr.hasNext() ? TimeTickUtil.toTime(itr.next(), TimeUnit.MILLISECONDS) : Integer.MAX_VALUE;
        runAsync(() -> plugin.banip(sender, p, expire, itr.hasNext() ? itr.next() : ""));
        plugin.getMessenger().send(sender, "default.commit", ChatColor.GREEN + "操作已提交");
        return true;
    }
}
