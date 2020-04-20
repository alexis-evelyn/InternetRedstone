package me.alexisevelyn.internetredstone.listeners.minecraft;

import lombok.Data;
import me.alexisevelyn.internetredstone.utilities.handlers.LecternHandlers;
import me.alexisevelyn.internetredstone.utilities.data.DisconnectReason;
import org.bukkit.Location;
import org.bukkit.block.BlockState;
import org.bukkit.block.Lectern;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

@Data
public class BreakLectern implements Listener {
    final private LecternHandlers handlers;

    // We want to be the last to know if the lectern is broken, that way we can ignore it if a claim plugin prevented the breakage
    // The way priorities work is that we get the last say at highest priority and therefor what we say happens.
    // However, we are not cancelling the event, we just want to make sure a claim plugin runs before we do,
    // so they take care of if it should be broken or not. That way, if it's cancelled, we can keep the lectern registered
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void breakLectern(BlockBreakEvent event) {
        BlockState snapshot = event.getBlock().getState();

        if (snapshot instanceof Lectern) {
            // Unregister Lectern With Plugin if Registered
            Location location = snapshot.getLocation();

            if (handlers.isRegistered(location)) {
                DisconnectReason disconnectReason = new DisconnectReason(DisconnectReason.Reason.BROKEN_LECTERN);
                disconnectReason.setPlayer(event.getPlayer().getUniqueId());

                handlers.unregisterHandler(location, disconnectReason);
            }
        }
    }
}
