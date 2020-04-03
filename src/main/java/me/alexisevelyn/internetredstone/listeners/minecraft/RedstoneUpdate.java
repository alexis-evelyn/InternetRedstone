package me.alexisevelyn.internetredstone.listeners.minecraft;

import me.alexisevelyn.internetredstone.utilities.LecternTracker;
import me.alexisevelyn.internetredstone.utilities.LecternTrackers;
import me.alexisevelyn.internetredstone.utilities.LecternUtilities;
import me.alexisevelyn.internetredstone.utilities.Logger;
import me.alexisevelyn.internetredstone.utilities.exceptions.InvalidBook;
import me.alexisevelyn.internetredstone.utilities.exceptions.InvalidLectern;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.BlockState;
import org.bukkit.block.Lectern;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.LecternInventory;
import org.bukkit.inventory.meta.BookMeta;

/*
 * This is not a true Redstone update listener. The actual Redstone update listener doesn't trigger on the Lectern.
 * So, instead, I have to use a BlockPhysicsEvent and filter out all the unnecessary noise.
 */
public class RedstoneUpdate implements Listener {
    LecternTrackers trackers;

    public RedstoneUpdate(LecternTrackers trackers) {
        this.trackers = trackers;
    }

    @EventHandler
    public void onRedstoneUpdate(BlockPhysicsEvent event) {
        // TODO: Prevent Lectern From Sending Out Signal When Player Flips Book Page
        // Most Likely Disable Block Updating For Book Page Flip

        BlockState snapshot = event.getBlock().getState();

        if (snapshot instanceof Lectern) {
            Lectern lectern = (Lectern) snapshot;
            LecternInventory inventory = (LecternInventory) lectern.getSnapshotInventory();
            Location location = lectern.getLocation();

            // If Tracker Not Registered, Don't Bother
            // We Won't Register Here As We Will Register on Startup
            if (!trackers.isRegistered(location))
                return;

            LecternTracker tracker = trackers.getTracker(location);

            // Prevent Sending Duplicate Redstone Power Levels
            if (tracker.isLastKnownPower(snapshot.getBlock().getBlockPower()))
                return;

            ItemStack book;
            BookMeta bookMeta;

            try {
                book = LecternUtilities.getItem(inventory);
                bookMeta = LecternUtilities.getBookMeta(book);
            } catch (InvalidLectern | InvalidBook exception) {
                Logger.warning("RedstoneUpdate: " + exception.getMessage());
                return;
            }

            // If not marked as a special Lectern, then ignore
            if (!LecternUtilities.hasIdentifier(bookMeta, LecternTrackers.getIdentifier()))
                return;

            // Now that the lectern verification is finished, store the current power level to prevent duplicates later
            tracker.setLastKnownPower(lectern.getBlock().getBlockPower());

            try {
                tracker.sendRedstoneUpdate(lectern.getBlock().getBlockPower());
            } catch (Exception exception) {
                Logger.printException(exception);
            }
        }
    }
}
