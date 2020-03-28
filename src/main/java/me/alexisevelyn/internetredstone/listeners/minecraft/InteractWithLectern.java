package me.alexisevelyn.internetredstone.listeners.minecraft;

import me.alexisevelyn.internetredstone.utilities.LecternTrackers;
import me.alexisevelyn.internetredstone.utilities.Logger;
import me.alexisevelyn.internetredstone.utilities.exceptions.DuplicateObjectException;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.BlockState;
import org.bukkit.block.Lectern;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.LecternInventory;
import org.bukkit.inventory.meta.BookMeta;

import java.util.Objects;
import java.util.UUID;

public class InteractWithLectern implements Listener {
    LecternTrackers trackers;

    public InteractWithLectern(LecternTrackers trackers) {
        this.trackers = trackers;
    }

    @EventHandler
    public void interactWithLectern(PlayerInteractEvent event) {
        // TODO: Make Tracker Register If Book Is Put in Lectern In Same Event
        // Register Lectern With Plugin if Special Lectern

        if (event.getAction() != Action.RIGHT_CLICK_BLOCK)
            return;

        BlockState snapshot = Objects.requireNonNull(event.getClickedBlock()).getState();

        if (snapshot instanceof Lectern) {
            Lectern lectern = (Lectern) snapshot;
            LecternInventory inventory = (LecternInventory) lectern.getSnapshotInventory();

            // Lecterns should only have 1 slot, but that may change in the future.
            ItemStack[] lecternItems = inventory.getContents();
            int lecternSize = lecternItems.length;

            // Something's Wrong With The Lectern - No Need For Further Processing
            if (lecternSize == 0)
                return;

            // Grabs item from first slot - Will be null if none in slot
            ItemStack book = lecternItems[0];

            // Not An Expected Book Or No Book - No Need For Further Processing
            if (book == null || !(book.getItemMeta() instanceof BookMeta))
                return;

            // Get the book's metadata (so, nbt tags)
            BookMeta bookMeta = (BookMeta) book.getItemMeta();

            // If not marked as a special Lectern, then ignore
            if (!ChatColor.stripColor(bookMeta.getPage(1)).contains(trackers.getIdentifier()))
                return;

            try {
                Location location = lectern.getLocation();
                UUID player = event.getPlayer().getUniqueId();

                if (!trackers.isRegistered(location))
                    trackers.registerTracker(location, player);
            } catch (DuplicateObjectException exception) {
                Logger.printException(exception);
            }
        }
    }
}
