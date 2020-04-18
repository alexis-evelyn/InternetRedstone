package me.alexisevelyn.internetredstone.listeners.minecraft;

import lombok.Data;
import me.alexisevelyn.internetredstone.utilities.LecternHandlers;
import me.alexisevelyn.internetredstone.utilities.data.DisconnectReason;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTakeLecternBookEvent;

@Data
public class TakeBook implements Listener {
    final private LecternHandlers handlers;

    // We get called last, so a claim plugin can handle their stuff before we get the event
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void takeBook(PlayerTakeLecternBookEvent event) {
        // Unregister Lectern With Plugin if Registered
        Location location = event.getLectern().getLocation();

        if (handlers.isRegistered(location)) {
            DisconnectReason disconnectReason = new DisconnectReason(DisconnectReason.Reason.REMOVED_BOOK);
            disconnectReason.setPlayer(event.getPlayer().getUniqueId());

            handlers.unregisterHandler(location, disconnectReason);
        }
    }
}
