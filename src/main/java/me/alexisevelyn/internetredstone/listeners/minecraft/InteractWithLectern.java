package me.alexisevelyn.internetredstone.listeners.minecraft;

import lombok.Data;
import me.alexisevelyn.internetredstone.utilities.LecternHandlers;
import me.alexisevelyn.internetredstone.utilities.LecternUtilities;
import me.alexisevelyn.internetredstone.utilities.exceptions.InvalidBook;
import org.bukkit.Location;
import org.bukkit.block.BlockState;
import org.bukkit.block.Lectern;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.LecternInventory;
import org.bukkit.inventory.meta.BookMeta;

import java.util.UUID;

@Data
public class InteractWithLectern implements Listener {
    final private LecternHandlers handlers;

    // We get called last, so a claim plugin can handle their stuff before we get the event
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void interactWithLectern(PlayerInteractEvent event) {
        // Register Lectern With Plugin if Special Lectern

        if (event.getAction() != Action.RIGHT_CLICK_BLOCK)
            return;

        if (event.getClickedBlock() == null)
            return;

        BlockState snapshot = event.getClickedBlock().getState();

        if (snapshot instanceof Lectern) {
            Lectern lectern = (Lectern) snapshot;
            LecternInventory inventory = (LecternInventory) lectern.getSnapshotInventory();
            Player player = event.getPlayer();

            // That way the player doesn't have to click twice to register the lectern
            if (!isLecternEmpty(inventory) || !checkHand(event)) {
                // Return since it's not special book or lectern is not empty
                // Assuming vanilla lectern then
                return;
            }

            Location location = lectern.getLocation();
            UUID player_uuid = player.getUniqueId();

            if (!handlers.isRegistered(location))
                handlers.registerHandler(location, player_uuid);
        }
    }

    private boolean isLecternEmpty(LecternInventory inventory) {
        // If lectern is not empty, return false
        return LecternUtilities.getItem(inventory) == null;
    }

    // This works no matter which hand it is, only one hand is called per event
    private boolean checkHand(PlayerInteractEvent event) {
        BookMeta bookMeta;

        try {
            bookMeta = LecternUtilities.getBookMeta(event.getItem());
        } catch (InvalidBook invalidBook) {
            return false;
        }

        return LecternUtilities.hasIdentifier(bookMeta);
    }
}
