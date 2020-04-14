package me.alexisevelyn.internetredstone.utilities;

import lombok.Data;
import me.alexisevelyn.internetredstone.utilities.exceptions.InvalidBook;
import me.alexisevelyn.internetredstone.utilities.exceptions.InvalidLectern;
import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.LecternInventory;
import org.bukkit.inventory.meta.BookMeta;

public class LecternUtilities {
    // Identifier to Look For In Order To Help Track Lecterns
    final static String identifier = "[Internet Redstone]";

    public static ItemStack getItem(LecternInventory inventory) throws InvalidLectern {
        // Lecterns should only have 1 slot, but that may change in the future.
        ItemStack[] lecternItems = inventory.getContents();
        int lecternSize = lecternItems.length;

        // Something's Wrong With The Lectern - No Need For Further Processing
        if (lecternSize != 1)
            throw new InvalidLectern("Lectern Inventory Size is " + lecternSize);

        // Grabs item from first slot - Will be null if none in slot
        return lecternItems[0];
    }

    public static BookMeta getBookMeta(ItemStack book) throws InvalidBook {
        // Not An Expected Book Or No Book - No Need For Further Processing
        if (book == null)
            throw new InvalidBook("Book is missing!!!");

        if (!(book.getItemMeta() instanceof BookMeta))
            throw new InvalidBook("Item is not a book: " + book.getType().toString());

        // Get the book's metadata (so, nbt tags)
        return (BookMeta) book.getItemMeta();
    }

    public static boolean hasIdentifier(BookMeta bookMeta, String identifier) {
        // If not marked as a special Lectern, then ignore
        return ChatColor.stripColor(bookMeta.getPage(1)).contains(identifier);
    }

    public static String getIdentifier() {
        return identifier;
    }
}
