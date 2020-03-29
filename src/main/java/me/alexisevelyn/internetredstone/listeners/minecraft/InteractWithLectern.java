package me.alexisevelyn.internetredstone.listeners.minecraft;

import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5Publish;
import me.alexisevelyn.internetredstone.utilities.LecternTrackers;
import me.alexisevelyn.internetredstone.utilities.LecternUtilities;
import me.alexisevelyn.internetredstone.utilities.Logger;
import me.alexisevelyn.internetredstone.utilities.exceptions.DuplicateObjectException;
import me.alexisevelyn.internetredstone.utilities.exceptions.InvalidBook;
import me.alexisevelyn.internetredstone.utilities.exceptions.InvalidLectern;
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

import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;

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

            ItemStack book;
            BookMeta bookMeta;

            try {
                book = LecternUtilities.getItem(inventory);
                bookMeta = LecternUtilities.getBookMeta(book);
            } catch (InvalidLectern | InvalidBook exception) {
                Logger.warning("InteractWithRedstone: " + exception.getMessage());
                return;
            }

            // If not marked as a special Lectern, then ignore
            if (!LecternUtilities.hasIdentifier(bookMeta, trackers.getIdentifier()))
                return;

            try {
                Location location = lectern.getLocation();
                UUID player_uuid = player.getUniqueId();

                if (!trackers.isRegistered(location))
                    trackers.registerTracker(location, player_uuid)
                            .subscribe("alexis/redstone/" + Math.random(), callback);
            } catch (DuplicateObjectException exception) {
                Logger.printException(exception);
            }
        }
    }

    Consumer<Mqtt5Publish> callback = mqtt5Publish -> { // Function To Run Asynchronously When Message is Received
        try {
            String decoded = new String(mqtt5Publish.getPayloadAsBytes(), StandardCharsets.UTF_8);

            Logger.info(ChatColor.GOLD + "" + ChatColor.BOLD
                            + "Received Message On Topic: "
                            + ChatColor.DARK_PURPLE + "" + ChatColor.BOLD
                            + mqtt5Publish.getTopic().toString());

            Logger.info(ChatColor.GOLD + "" + ChatColor.BOLD
                            + "Message: "
                            + ChatColor.AQUA + "" + ChatColor.BOLD
                            + decoded
                            + ChatColor.RESET);

            Logger.info(ChatColor.DARK_PURPLE + "Callback Located in InteractWithLectern.java!!!");
        } catch (Exception exception) {
            Logger.printException(exception);
        }
    };
}
