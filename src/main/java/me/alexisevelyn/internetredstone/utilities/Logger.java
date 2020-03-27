package me.alexisevelyn.internetredstone.utilities;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import java.util.Objects;

public class Logger {
    static String pluginID = "InternetRedstone";

    public static String getPluginPrefix() {
        String prefix;
        try {
            prefix = Objects.requireNonNull(Bukkit.getServer().getPluginManager().getPlugin(pluginID)).getDescription().getPrefix();
        } catch (NullPointerException exception) {
            return pluginID;
        }

        return prefix;
    }

    public static void printException(Exception exception) {
        severe(ChatColor.GOLD + "" + ChatColor.BOLD + "Exception: "
                + ChatColor.DARK_RED + "" + ChatColor.BOLD + exception.getMessage());

        severe(ChatColor.RED + "" + ChatColor.BOLD + "---");

        for (StackTraceElement line : exception.getStackTrace()) {
            severe(ChatColor.DARK_GREEN + "" + ChatColor.BOLD + line.getLineNumber()
                    + ChatColor.RED + "" + ChatColor.BOLD + " - "
                    + ChatColor.DARK_RED + "" + ChatColor.BOLD + line.toString());
        }

        severe(ChatColor.RED + "" + ChatColor.BOLD + "---");
    }

    public static void info(String message) {
        Bukkit.getLogger().info(ChatColor.DARK_PURPLE
                + "["
                + getPluginPrefix()
                + "]"
                + ChatColor.RESET
                + " "
                + message);
    }

    public static void severe(String message) {
        Bukkit.getLogger().severe(ChatColor.DARK_PURPLE
                + "["
                + getPluginPrefix()
                + "]"
                + ChatColor.RESET
                + " "
                + message);
    }
}
