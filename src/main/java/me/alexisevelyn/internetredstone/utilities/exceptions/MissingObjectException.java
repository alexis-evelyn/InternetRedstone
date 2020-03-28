package me.alexisevelyn.internetredstone.utilities.exceptions;

public class MissingObjectException extends Exception {
    public MissingObjectException(String message) {
        super(message);
    }

    public MissingObjectException(String message, Throwable cause) {
        super(message, cause);
    }
}
