package me.alexisevelyn.internetredstone.listeners.minecraft;

import lombok.Data;
import me.alexisevelyn.internetredstone.utilities.handlers.LecternHandler;
import me.alexisevelyn.internetredstone.utilities.handlers.LecternHandlers;
import org.bukkit.Location;
import org.bukkit.block.BlockState;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPhysicsEvent;

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
        Location location = snapshot.getLocation();

        // If Tracker Not Registered, Don't Bother
        // We Won't Register Here As We Will Register on Startup
        if (!handlers.isRegistered(location))
            return;

        LecternHandler handler = handlers.getHandler(location);
        Integer blockPower = snapshot.getBlock().getBlockPower();

        // Prevent Sending Duplicate Redstone Power Levels
        if (handler.isLastKnownPower(blockPower))
            return;

        // Now that the lectern verification is finished,
        // send the redstone update, block power is automatically stored for us
        handler.sendRedstoneUpdate(blockPower);
    }
}
