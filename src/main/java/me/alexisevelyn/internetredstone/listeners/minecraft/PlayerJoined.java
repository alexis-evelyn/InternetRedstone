package me.alexisevelyn.internetredstone.listeners.minecraft;

import lombok.Data;
import me.alexisevelyn.internetredstone.database.mysql.MySQLClient;
import me.alexisevelyn.internetredstone.utilities.LecternHandlers;
import me.alexisevelyn.internetredstone.utilities.Logger;
import me.alexisevelyn.internetredstone.utilities.Translator;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.sql.SQLException;

@Data
public class PlayerJoined implements Listener {
    final MySQLClient client;
    final Translator translator;
    final private LecternHandlers handlers;

    // We get called last, so another plugin can handle their stuff before we get the event
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void playerJoinedLocaleUpdate(PlayerJoinEvent event) {
        // Update Player's Locale in Database

        try {
            client.updateLocale(event.getPlayer().getUniqueId(), event.getPlayer().getLocale());
            handlers.updatePlayerLocale(event.getPlayer().getUniqueId(), event.getPlayer().getLocale());
        } catch (SQLException exception) {
            Logger.severe(String.valueOf(ChatColor.GOLD) + ChatColor.BOLD
                    + translator.getString("update_locale_sql_exception"));

            Logger.printException(exception);
        }
    }

    // We get called last, so another plugin can handle their stuff before we get the event
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void playerJoinedIGNUpdate(PlayerJoinEvent event) {
        // TODO: Update ign in lecterns if changed

    }
}
