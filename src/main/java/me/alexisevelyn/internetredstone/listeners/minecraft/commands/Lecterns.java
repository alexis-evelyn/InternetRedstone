package me.alexisevelyn.internetredstone.listeners.minecraft.commands;

import lombok.Data;
import me.alexisevelyn.internetredstone.utilities.LecternHandler;
import me.alexisevelyn.internetredstone.utilities.LecternHandlers;
import me.alexisevelyn.internetredstone.utilities.Logger;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ConcurrentHashMap;

@Data
public class Lecterns implements CommandExecutor {
    final LecternHandlers handlers;

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
        ConcurrentHashMap<Location, LecternHandler> trackerMap = handlers.getHandlers();

        if (trackerMap.size() == 0) {
            sender.sendMessage(ChatColor.GOLD + "No Registered Lecterns!!!");
        }

        for (Location location : trackerMap.keySet()) {
            sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD
                    + "Lectern: "
                    + ChatColor.DARK_GREEN + "" + ChatColor.BOLD
                    + location.getWorld().getName()
                    + Logger.getFormattedLocation(location)

                    + ChatColor.DARK_PURPLE + "" + ChatColor.BOLD
                    + " - "
                    + trackerMap.get(location).getLecternID());
        }
    }
}
