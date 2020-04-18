package me.alexisevelyn.internetredstone.listeners.minecraft;

import lombok.Data;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

@Data
public class PlayerJoined implements Listener {
    // Hello

    // We get called last, so another plugin can handle their stuff before we get the event
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void playerJoinedLocaleUpdate(PlayerJoinEvent event) {
        // TODO: Update Player's Locale in Database

    }

    // We get called last, so another plugin can handle their stuff before we get the event
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void playerJoinedIGNUpdate(PlayerJoinEvent event) {
        // TODO: Update ign in lecterns if changed

    }
}
