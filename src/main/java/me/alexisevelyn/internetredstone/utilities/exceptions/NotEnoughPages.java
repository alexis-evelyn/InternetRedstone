package me.alexisevelyn.internetredstone.utilities.exceptions;

public class NotEnoughPages extends Exception {
    public NotEnoughPages(String message) {
        super(message);
    }

    @SuppressWarnings("unused")
    public NotEnoughPages(String message, Throwable cause) {
        super(message, cause);
    }
}
