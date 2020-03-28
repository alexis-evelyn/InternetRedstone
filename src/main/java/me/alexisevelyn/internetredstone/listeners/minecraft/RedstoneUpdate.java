package me.alexisevelyn.internetredstone.listeners.minecraft;

import me.alexisevelyn.internetredstone.utilities.LecternTracker;
import me.alexisevelyn.internetredstone.utilities.Logger;
import org.bukkit.Bukkit;
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

import java.util.Objects;
import java.util.UUID;

/*
 * This is not a true Redstone update listener. The actual Redstone update listener doesn't trigger on the Lectern.
 * So, instead, I have to use a BlockPhysicsEvent and filter out all the unnecessary noise.
 */
public class RedstoneUpdate implements Listener {
    LecternTracker tracker;
    String identifier = "[Internet Redstone]";

    public RedstoneUpdate() {
        try {
            Location location = Objects.requireNonNull(Bukkit.getWorld("world")).getSpawnLocation();
            UUID alexis_evelyn = UUID.fromString("f3b4e8a4-7f52-4b0a-a18d-1af64935a89f"); // My UUID For Testing
            tracker = new LecternTracker(location, alexis_evelyn);
            tracker.subscribe();
        } catch (Exception exception) {
            Logger.printException(exception);
        }
    }

    @EventHandler
    public void onRedstoneUpdate(BlockPhysicsEvent event) {
        BlockState snapshot = event.getBlock().getState();

        if (snapshot instanceof Lectern) {
            Lectern lectern = (Lectern) snapshot;
            LecternInventory inventory = (LecternInventory) lectern.getSnapshotInventory();
            LecternTracker tracker = this.tracker; // TODO: Retrieve Specific Tracker From Another Class

            // Prevent Sending Duplicate Redstone Power Levels
            if (lectern.getBlock().getBlockPower() == tracker.getLastKnownPower())
                return;

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
            if (!ChatColor.stripColor(bookMeta.getPage(1)).contains(identifier))
                return;

            // Now that the lectern verification is finished, store the current power level to prevent duplicates later
            tracker.setLastKnownPower(lectern.getBlock().getBlockPower());

            // Note: This Page Check Keeps The Book From Being "Frozen" on the First Page. Page 0 is Page 1 of the Book!!!
            if (lectern.getPage() == 0) {
                bookMeta.setPage(1, ChatColor.DARK_PURPLE + "" + ChatColor.BOLD
                        + identifier
                        + ChatColor.DARK_GREEN + "" + ChatColor.BOLD
                        + "\n"
                        + "Redstone Power: "
                        + ChatColor.DARK_RED + "" + ChatColor.BOLD
                        + lectern.getBlock().getBlockPower());

                book.setItemMeta(bookMeta);

                inventory.setItem(0, book);
                lectern.update();
            }

            try {
                tracker.sendRedstoneUpdate(lectern.getBlock().getBlockPower());
            } catch (Exception exception) {
                Logger.printException(exception);
            }
        }
    }
}
