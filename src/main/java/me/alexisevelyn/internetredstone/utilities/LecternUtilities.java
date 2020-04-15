package me.alexisevelyn.internetredstone.utilities;

import me.alexisevelyn.internetredstone.utilities.exceptions.InvalidBook;
import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.LecternInventory;
import org.bukkit.inventory.meta.BookMeta;

public class LecternUtilities {
    // Identifier to Look For In Order To Help Track Lecterns
    final static String identifier = "[Internet Redstone]";

    public static ItemStack getItem(LecternInventory inventory) {
        // Grabs item from first slot - Will be null if none in slot
        return inventory.getContents()[0];
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

    public static boolean hasIdentifier(BookMeta bookMeta) {
        // If not marked as a special Lectern, then ignore
        return ChatColor.stripColor(bookMeta.getPage(1)).contains(identifier);
    }

    public static String getIdentifier() {
        return identifier;
    }
}
