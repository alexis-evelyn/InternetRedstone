package me.alexisevelyn.internetredstone.listeners.minecraft;

import lombok.Data;
import me.alexisevelyn.internetredstone.utilities.LecternHandlers;
import me.alexisevelyn.internetredstone.utilities.LecternUtilities;
import me.alexisevelyn.internetredstone.utilities.exceptions.InvalidBook;
import org.bukkit.Location;
import org.bukkit.block.BlockState;
import org.bukkit.block.Lectern;
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

        // Process Only if Right Click and Clicking a Block
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK || event.getClickedBlock() == null)
            return;

        BlockState snapshot = event.getClickedBlock().getState();
        Location location = snapshot.getLocation();

        // If Block is Registered, Then Just Return
        if (handlers.isRegistered(location))
            return;

        // Block is a Lectern, Then Validate and Register The Lectern
        // Validate means to ensure it's a special lectern for the plugin
        if (snapshot instanceof Lectern) {
            LecternInventory inventory = (LecternInventory) ((Lectern) snapshot).getSnapshotInventory();

            // Check If Lectern is empty and if so, make sure the player has a special book
            if (!isLecternEmpty(inventory) || !checkHand(event))
                return;

            // Get Player's UUID and Register Lectern
            UUID player_uuid = event.getPlayer().getUniqueId();
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
