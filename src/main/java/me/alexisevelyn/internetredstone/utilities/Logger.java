package me.alexisevelyn.internetredstone.utilities;

import io.reactivex.functions.Consumer;
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

    public static void warning(String message) {
        Bukkit.getLogger().warning(ChatColor.DARK_PURPLE
                + "["
                + getPluginPrefix()
                + "]"
                + ChatColor.RESET
                + " "
                + message);
    }

    public static void config(String message) {
        Bukkit.getLogger().config(ChatColor.DARK_PURPLE
                + "["
                + getPluginPrefix()
                + "]"
                + ChatColor.RESET
                + " "
                + message);
    }

    public static void fine(String message) {
        Bukkit.getLogger().fine(ChatColor.DARK_PURPLE
                + "["
                + getPluginPrefix()
                + "]"
                + ChatColor.RESET
                + " "
                + message);
    }

    public static void finer(String message) {
        Bukkit.getLogger().finer(ChatColor.DARK_PURPLE
                + "["
                + getPluginPrefix()
                + "]"
                + ChatColor.RESET
                + " "
                + message);
    }

    public static void finest(String message) {
        Bukkit.getLogger().finest(ChatColor.DARK_PURPLE
                + "["
                + getPluginPrefix()
                + "]"
                + ChatColor.RESET
                + " "
                + message);
    }

    public static void rxHandler(Throwable error) {
        severe(ChatColor.GOLD + "" + ChatColor.BOLD + "Severe Throwable Error (Cannot Recover): "
                + ChatColor.DARK_RED + "" + ChatColor.BOLD + error.getMessage());

        severe(ChatColor.RED + "" + ChatColor.BOLD + "---");

        for (StackTraceElement line : error.getStackTrace()) {
            severe(ChatColor.DARK_GREEN + "" + ChatColor.BOLD + line.getLineNumber()
                    + ChatColor.RED + "" + ChatColor.BOLD + " - "
                    + ChatColor.DARK_RED + "" + ChatColor.BOLD + line.toString());
        }

        severe(ChatColor.RED + "" + ChatColor.BOLD + "---");
    }
}
