package me.alexisevelyn.internetredstone.listeners.minecraft.commands;

import me.alexisevelyn.internetredstone.utilities.LecternTracker;
import me.alexisevelyn.internetredstone.utilities.LecternTrackers;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ConcurrentHashMap;

public class Commands implements CommandExecutor {
    final LecternTrackers trackers;

    public Commands(LecternTrackers trackers) {
        this.trackers = trackers;
    }

    /**
     * Executes the given command, returning its success.
     * <br>
     * If false is returned, then the "usage" plugin.yml entry for this command
     * (if defined) will be sent to the player.
     *
     * @param sender  Source of the command
     * @param command Command which was executed
     * @param label   Alias of the command which was used
     * @param args    Passed command arguments
     * @return true if a valid command, otherwise false
     */
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        listTrackers(sender);

        return true;
    }

    public void listTrackers(CommandSender sender) {
        ConcurrentHashMap<Location, LecternTracker> trackerMap = trackers.getTrackers();

        if (trackerMap.size() == 0) {
            sender.sendMessage(ChatColor.GOLD + "No Registered Lecterns!!!");
        }

        for (Location location : trackerMap.keySet()) {
//            trackerMap.get(location).subscribe();

            sender.sendMessage(ChatColor.GOLD + "Registered Lectern Location: "
                    + ChatColor.DARK_GREEN + "" + ChatColor.BOLD + "("
                    + ChatColor.DARK_PURPLE + "" + ChatColor.BOLD
                    + location.getBlockX()
                    + ChatColor.DARK_GREEN + "" + ChatColor.BOLD + ", "
                    + ChatColor.DARK_PURPLE + "" + ChatColor.BOLD
                    + location.getBlockY()
                    + ChatColor.DARK_GREEN + "" + ChatColor.BOLD + ", "
                    + ChatColor.DARK_PURPLE + "" + ChatColor.BOLD
                    + location.getBlockZ()
                    + ChatColor.DARK_GREEN + "" + ChatColor.BOLD + ")");
        }
    }
}
