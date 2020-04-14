package me.alexisevelyn.internetredstone.listeners.minecraft;

import lombok.Data;
import me.alexisevelyn.internetredstone.utilities.LecternHandlers;
import me.alexisevelyn.internetredstone.utilities.LecternUtilities;
import me.alexisevelyn.internetredstone.utilities.Logger;
import me.alexisevelyn.internetredstone.utilities.exceptions.InvalidBook;
import me.alexisevelyn.internetredstone.utilities.exceptions.InvalidLectern;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.BlockState;
import org.bukkit.block.Lectern;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.LecternInventory;
import org.bukkit.inventory.meta.BookMeta;

import java.util.Objects;
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

        BlockState snapshot;

        try {
            snapshot = Objects.requireNonNull(event.getClickedBlock()).getState();
        } catch (NullPointerException exception) {
            Logger.warning(ChatColor.GOLD + "InteractWithLectern: No Block Was Clicked!!!");
            return;
        }

        if (snapshot instanceof Lectern) {
            Lectern lectern = (Lectern) snapshot;
            LecternInventory inventory = (LecternInventory) lectern.getSnapshotInventory();
            Player player = event.getPlayer();

            // Check if Lectern is Empty
            if (!isLecternEmpty(inventory)) {
                // If not empty, check item in inventory
                // For special book
                if (!checkLectern(inventory)) {
                    // Return if not special book
                    // Assuming vanilla lectern then
                    return;
                }
            } else {
                // If empty, then check hand for special book
                // That way the player doesn't have to click twice to register the lectern

                if (!checkHand(event)) {
                    // Return if not special book
                    // Assuming vanilla lectern then
                    return;
                }
            }

            try {
                Location location = lectern.getLocation();
                UUID player_uuid = player.getUniqueId();

                if (!handlers.isRegistered(location))
                    handlers.registerHandler(location, player_uuid);
            } catch (NullPointerException exception) {
                Logger.severe(ChatColor.GOLD + "" + ChatColor.BOLD
                        + "InteractWithLectern: Failed to Initialize Lectern Tracker!!!");

                Logger.printException(exception);
            }
        }
    }

    private boolean isLecternEmpty(LecternInventory inventory) {
        ItemStack book;

        try {
            book = LecternUtilities.getItem(inventory);
        } catch (InvalidLectern invalidLectern) {
            // Not a lectern, so false
            return false;
        }

        // If lectern is not empty, return false
        return book == null;
    }

    private boolean checkLectern(LecternInventory inventory) {
        ItemStack book;
        BookMeta bookMeta;

        try {
            book = LecternUtilities.getItem(inventory);
            bookMeta = LecternUtilities.getBookMeta(book);
        } catch (InvalidLectern exception) {
            Logger.warning("InteractWithLectern: " + exception.getMessage());
            return false;
        } catch (InvalidBook exception) {
            return false;
        }

        // If not marked as a special Lectern, then ignore
        return LecternUtilities.hasIdentifier(bookMeta, LecternUtilities.getIdentifier());
    }

    private boolean checkHand(PlayerInteractEvent event) {
        try {
            LecternUtilities.getBookMeta(event.getItem());
        } catch (InvalidBook invalidBook) {
            return false;
        }

        return true;
    }
}
