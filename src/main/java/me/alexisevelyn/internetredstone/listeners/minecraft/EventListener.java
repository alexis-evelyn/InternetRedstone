package me.alexisevelyn.internetredstone.listeners.minecraft;

import me.alexisevelyn.internetredstone.utilities.LecternTracker;
import me.alexisevelyn.internetredstone.utilities.Logger;
import org.bukkit.*;
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

public class EventListener implements Listener {
    LecternTracker tracker;

    public EventListener() {
        try {
            Location location = Objects.requireNonNull(Bukkit.getWorld("world")).getSpawnLocation();
            UUID alexis_evelyn = UUID.fromString("f3b4e8a4-7f52-4b0a-a18d-1af64935a89f"); // My UUID For Testing
            this.tracker = new LecternTracker(location, alexis_evelyn);
        } catch (Exception exception) {
            Logger.printException(exception);
        }
    }

    @EventHandler
    public void onRedstoneUpdate(BlockPhysicsEvent event) {
        BlockState snapshot = event.getBlock().getState();

        // TODO: Figure out why turning redstone off from a distance spams the MQTT Log!!!
        if (snapshot instanceof Lectern) {
            Lectern lectern = (Lectern) snapshot;
            LecternInventory inventory = (LecternInventory) lectern.getSnapshotInventory();

            // Lecterns should only have 1 slot, but that may change in the future.
            ItemStack[] lecternItems = inventory.getContents();
            int lecternSize = lecternItems.length;

            // Something's Wrong With The Lectern - No Need For Further Processing
            if (lecternSize == 0)
                return;

            ItemStack book = lecternItems[0];

            // Not An Expected Book Or No Book - No Need For Further Processing
            if (book == null || !(book.getItemMeta() instanceof BookMeta))
                return;

            BookMeta bookMeta = (BookMeta) book.getItemMeta();

            // Note: This Page Check Keeps The Book From Being "Frozen" on the First Page. Page 0 is Page 1 of the Book!!!
            if (lectern.getPage() == 0) {
                bookMeta.setPage(1, ChatColor.DARK_PURPLE + "" + ChatColor.BOLD
                        + "Redstone Power: " + lectern.getBlock().getBlockPower());

                book.setItemMeta(bookMeta);

                inventory.setItem(0, book);
                lectern.update();
            }

            try {
                this.tracker.sendRedstoneUpdate(lectern.getBlock().getBlockPower());
            } catch (Exception exception) {
                Logger.printException(exception);
            }
        }
    }
}
