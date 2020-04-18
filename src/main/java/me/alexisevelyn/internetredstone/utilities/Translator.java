package me.alexisevelyn.internetredstone.utilities;

import org.jetbrains.annotations.NotNull;

import java.util.Enumeration;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class Translator extends ResourceBundle {
    private ResourceBundle translation;
    private ResourceBundle fallback;
    final private String baseName = "translations/MessagesBundle";

    public Translator(@NotNull  String parse) {
        //  https://papermc.io/javadocs/paper/1.15/org/bukkit/entity/Player.html#getLocale--
        //  https://www.spigotmc.org/threads/getting-client-language.103710/#post-1126571

        // Parse Locale Out of String (Only Throws NPE)
        Locale locale = Locale.forLanguageTag(parse);

        try {
            translation = ResourceBundle.getBundle(baseName, locale);
            fallback = ResourceBundle.getBundle(baseName, new Locale("en"));
        } catch (MissingResourceException exception) {
            translation = ResourceBundle.getBundle(baseName, new Locale("en"));

            Logger.info("Player's Locale, " + locale + " not found, defaulting to English (en)!!!");
        }
    }

    public Translator(String language, String country, String variant) {
        Locale locale = new Locale(language, country, variant);
        try {
            translation = ResourceBundle.getBundle(baseName, locale);
            fallback = ResourceBundle.getBundle(baseName, new Locale("en"));
        } catch (MissingResourceException exception) {
            translation = ResourceBundle.getBundle(baseName, new Locale("en"));

            Logger.severe("Locale, " + locale + " not found, defaulting to English (en)!!!");
            Logger.printException(exception);
        }
    }

    /**
     * Gets an object for the given key from this resource bundle.
     * Returns null if this resource bundle does not contain an
     * object for the given key.
     *
     * @param key the key for the desired object
     * @return the object for the given key, or null
     * @throws NullPointerException if <code>key</code> is <code>null</code>
     */
    @Override
    protected Object handleGetObject(@NotNull String key) {
        // If translation is missing from the locale, fallback to English (en)
        if (!translation.containsKey(key)) {
            return fallback.getObject(key);
        }

        return translation.getObject(key);
    }

    /**
     * Returns an enumeration of the keys.
     *
     * @return an <code>Enumeration</code> of the keys contained in
     * this <code>ResourceBundle</code> and its parent bundles.
     */
    @NotNull
    @Override
    public Enumeration<String> getKeys() {
        return translation.getKeys();
    }
}
