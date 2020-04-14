package me.alexisevelyn.internetredstone.listeners.minecraft;

import lombok.Data;
import me.alexisevelyn.internetredstone.utilities.LecternHandler;
import me.alexisevelyn.internetredstone.utilities.LecternHandlers;
import me.alexisevelyn.internetredstone.utilities.LecternUtilities;
import me.alexisevelyn.internetredstone.utilities.Logger;
import me.alexisevelyn.internetredstone.utilities.exceptions.InvalidBook;
import me.alexisevelyn.internetredstone.utilities.exceptions.InvalidLectern;
import org.bukkit.Location;
import org.bukkit.block.BlockState;
import org.bukkit.block.Lectern;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.LecternInventory;
import org.bukkit.inventory.meta.BookMeta;

/*
 * This is not a true Redstone update listener. The actual Redstone update listener doesn't trigger on the Lectern.
 * So, instead, I have to use a BlockPhysicsEvent and filter out all the unnecessary noise.
 */
@Data
public class RedstoneUpdate implements Listener {
    final LecternHandlers handlers;

    // We get called last, so a claim plugin can handle their stuff before we get the event
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onRedstoneUpdate(BlockPhysicsEvent event) {
        BlockState snapshot = event.getBlock().getState();

        if (snapshot instanceof Lectern) {
            Lectern lectern = (Lectern) snapshot;
            LecternInventory inventory = (LecternInventory) lectern.getSnapshotInventory();
            Location location = lectern.getLocation();

            // If Tracker Not Registered, Don't Bother
            // We Won't Register Here As We Will Register on Startup
            if (!handlers.isRegistered(location))
                return;

            LecternHandler handler = handlers.getHandler(location);

            // Prevent Sending Duplicate Redstone Power Levels
            if (handler.isLastKnownPower(snapshot.getBlock().getBlockPower()))
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
            if (!LecternUtilities.hasIdentifier(bookMeta, LecternUtilities.getIdentifier()))
                return;

            // Now that the lectern verification is finished, store the current power level to prevent duplicates later
            handler.setLastKnownPower(lectern.getBlock().getBlockPower());

            try {
                handler.sendRedstoneUpdate(lectern.getBlock().getBlockPower());
            } catch (Exception exception) {
                Logger.printException(exception);
            }
        }
    }
}
