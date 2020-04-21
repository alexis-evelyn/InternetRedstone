package me.alexisevelyn.internetredstone.utilities.exceptions;

public class InvalidBook extends Exception {
    public InvalidBook(String message) {
        super(message);
    }

    @SuppressWarnings("unused")
    public InvalidBook(String message, Throwable cause) {
        super(message, cause);
    }
}
