package me.alexisevelyn.internetredstone.utilities.exceptions;

public class DuplicateObjectException extends Exception {
    public DuplicateObjectException(String message) {
        super(message);
    }

    public DuplicateObjectException(String message, Throwable cause) {
        super(message, cause);
    }
}
