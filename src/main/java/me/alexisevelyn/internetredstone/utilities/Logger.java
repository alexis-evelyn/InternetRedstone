package me.alexisevelyn.internetredstone.utilities;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import java.util.Objects;

public class Logger {
    static final String pluginID = "InternetRedstone";
    static Boolean debug = false;

    public static void setDebugMode(Boolean debugMode) {
        debug = debugMode;
    }

    public static boolean isDebugMode() {
        return debug;
    }

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
        String message = exception.getMessage();

        // Some exceptions don't have messages, print their class name instead!!!
        if (message == null) {
            message = exception.getClass().getName();
        }

        severe(ChatColor.GOLD + "" + ChatColor.BOLD + "Exception: "
                + ChatColor.DARK_RED + "" + ChatColor.BOLD + message);

        severe(ChatColor.RED + "" + ChatColor.BOLD + "---");

        for (StackTraceElement line : exception.getStackTrace()) {
            severe(ChatColor.DARK_GREEN + "" + ChatColor.BOLD + line.getLineNumber()
                    + ChatColor.RED + "" + ChatColor.BOLD + " - "
                    + ChatColor.DARK_RED + "" + ChatColor.BOLD + line.toString());
        }

        severe(ChatColor.RED + "" + ChatColor.BOLD + "---");
    }

    public static void info(String message) {
        if (!isDebugMode())
            return;

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

    @SuppressWarnings("unused")
    public static void config(String message) {
        if (!isDebugMode())
            return;

        Bukkit.getLogger().config(ChatColor.DARK_PURPLE
                + "["
                + getPluginPrefix()
                + "]"
                + ChatColor.RESET
                + " "
                + message);
    }

    @SuppressWarnings("unused")
    public static void fine(String message) {
        if (!isDebugMode())
            return;

        Bukkit.getLogger().fine(ChatColor.DARK_PURPLE
                + "["
                + getPluginPrefix()
                + "]"
                + ChatColor.RESET
                + " "
                + message);
    }

    @SuppressWarnings("unused")
    public static void finer(String message) {
        if (!isDebugMode())
            return;

        Bukkit.getLogger().finer(ChatColor.DARK_PURPLE
                + "["
                + getPluginPrefix()
                + "]"
                + ChatColor.RESET
                + " "
                + message);
    }

    @SuppressWarnings("unused")
    public static void finest(String message) {
        if (!isDebugMode())
            return;

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
