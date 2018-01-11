package com.i5mc.ban.command;

import com.avaje.ebean.EbeanServer;
import com.i5mc.ban.$;
import com.i5mc.ban.BanPlugin;
import com.i5mc.ban.L2Pool;
import com.i5mc.ban.entity.BanLog;
import com.i5mc.ban.entity.BanLogType;
import com.i5mc.ban.entity.Mute;
import lombok.RequiredArgsConstructor;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.sql.Timestamp;
import java.util.Arrays;

import static com.i5mc.ban.$.nil;

@RequiredArgsConstructor
public class UnMuteCommand implements CommandExecutor {

    private final BanPlugin plugin;

    public boolean onCommand(CommandSender sender, Command __, String label, String[] input) {
        if (input.length < 1) {
            sender.sendMessage("/unmute <player>");
            return false;
        }

        plugin.execute(() -> {
            EbeanServer db = plugin.getDataSource();

            Mute mute = db.find(Mute.class).where("name = ?").setParameter(1, input[0].toLowerCase()).findUnique();

            if (nil(mute) || mute.getExpire() == null || mute.getExpire().before(new Timestamp($.now()))) {
                sender.sendMessage(ChatColor.RED + "玩家未禁言");
                return;
            }

            BanLog log = db.createEntityBean(BanLog.class);
            log.setName(mute.getName());
            log.setLogType(BanLogType.PARDON_MUTE.name());
            log.setExecutor(sender.getName());
            log.setLogTime(new Timestamp($.now()));

            mute.setExpire(null);
            mute.setLatestLog(log);
            mute.setLatestUpdate(log.getLogTime());

            db.save(Arrays.asList(log, mute));

            L2Pool.invalid("mute:" + mute.getName());

            sender.sendMessage(ChatColor.GREEN + "操作已完成");
        });

        return true;
    }
}
