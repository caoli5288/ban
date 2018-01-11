package com.i5mc.ban.command;

import com.avaje.ebean.EbeanServer;
import com.i5mc.ban.$;
import com.i5mc.ban.BanPlugin;
import com.i5mc.ban.L2Pool;
import com.i5mc.ban.TimeTickUtil;
import com.i5mc.ban.entity.BanLog;
import com.i5mc.ban.entity.BanLogType;
import com.i5mc.ban.entity.Mute;
import lombok.RequiredArgsConstructor;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;

import static com.i5mc.ban.$.nil;

@RequiredArgsConstructor
public class MuteCommand implements CommandExecutor {

    private final BanPlugin plugin;

    public boolean onCommand(CommandSender sender, Command __, String label, String[] input) {
        if (input.length < 1) {
            sender.sendMessage("/mute <player> [time] [message]");
            return false;
        }

        Iterator<String> itr = Arrays.asList(input).iterator();
        Player p = plugin.getPlayer(itr.next());
        if (p == null) {
            sender.sendMessage(ChatColor.RED + "玩家不在线");
            return false;
        }

        long duration = itr.hasNext() ? TimeTickUtil.toTime(itr.next(), TimeUnit.MILLISECONDS) : -1;
        String reason = itr.hasNext() ? itr.next() : null;

        plugin.execute(() -> {
            EbeanServer db = plugin.getDataSource();

            Mute mute = db.find(Mute.class).where("name = ?").setParameter(1, p.getName().toLowerCase()).findUnique();
            if (nil(mute)) {
                mute = db.createEntityBean(Mute.class);
                mute.setName(p.getName().toLowerCase());
            }

            BanLog log = db.createEntityBean(BanLog.class);
            log.setName(mute.getName());
            log.setLogType(BanLogType.MUTE.name());
            log.setDuration(duration);
            log.setExecutor(sender.getName());
            log.setReason(reason);
            log.setLogTime(new Timestamp($.now()));

            mute.setExpire(duration >= 1 ? new Timestamp($.now() + duration) : Timestamp.from(Instant.now().plus(1, ChronoUnit.DAYS)));
            mute.setLatestLog(log);
            mute.setLatestUpdate(log.getLogTime());

            db.save(Arrays.asList(log, mute));

            L2Pool.put("mute:" + p.getName().toLowerCase(), mute);

            sender.sendMessage(ChatColor.GREEN + "操作已完成");
        });

        return true;
    }

}
