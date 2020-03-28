package me.alexisevelyn.internetredstone.listeners.minecraft;

import org.bukkit.block.BlockState;
import org.bukkit.block.Lectern;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.LecternInventory;

import java.util.Objects;

public class InteractWithLectern implements Listener {
    @EventHandler
    public void interactWithLectern(PlayerInteractEvent event) {
        // Do Nothing For Now!!!

        // This will be used to register a lectern with the plugin

        if (event.getAction() != Action.RIGHT_CLICK_BLOCK)
            return;

        BlockState snapshot = Objects.requireNonNull(event.getClickedBlock()).getState();

        if (snapshot instanceof Lectern) {
            Lectern lectern = (Lectern) snapshot;
            LecternInventory inventory = (LecternInventory) lectern.getSnapshotInventory();

            Player player = event.getPlayer();
        }
    }
}
