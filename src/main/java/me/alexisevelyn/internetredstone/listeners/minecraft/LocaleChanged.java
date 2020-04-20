package me.alexisevelyn.internetredstone.listeners.minecraft;

import lombok.Data;
import me.alexisevelyn.internetredstone.utilities.Logger;
import me.alexisevelyn.internetredstone.utilities.Translator;
import me.alexisevelyn.internetredstone.utilities.handlers.PlayerHandlers;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLocaleChangeEvent;

import java.sql.SQLException;

@Data
public class LocaleChanged implements Listener {
    final Translator translator;
    final private PlayerHandlers playerHandlers;

    // We get called last, so another plugin can handle their stuff before we get the event
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void localeChanged(PlayerLocaleChangeEvent event) {
        // Update Player's Locale in Database

        try {
            playerHandlers.updatePlayerLocale(event.getPlayer().getUniqueId(), event.getLocale());
        } catch (SQLException exception) {
            Logger.severe(String.valueOf(ChatColor.GOLD) + ChatColor.BOLD
                    + translator.getString("update_locale_sql_exception"));

            Logger.printException(exception);
        }
    }
}
