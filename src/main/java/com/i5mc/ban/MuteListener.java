package com.i5mc.ban;

import com.i5mc.ban.entity.Mute;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.bukkit.ChatColor;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.EventExecutor;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;

import static com.i5mc.ban.$.nil;

@RequiredArgsConstructor
public class MuteListener implements EventExecutor {

    private final BanPlugin plugin;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("y-M-d HH:mm:ss");

    public void execute(Listener l, Event event) {
        val p = ((AsyncPlayerChatEvent) event).getPlayer();
        val mute = L2Pool.pull("mute:" + p.getName().toLowerCase(), () -> plugin.getDataSource().find(Mute.class)
                .where("name = ? and expire > now()")
                .setParameter(1, p.getName().toLowerCase())
                .findUnique());
        if (!nil(mute) && mute.getExpire().after(new Timestamp($.now()))) {
            ((Cancellable) event).setCancelled(true);
            p.sendMessage(ChatColor.RED + "您已禁言至 " + dateFormat.format(mute.getExpire()));
        }
    }

}
