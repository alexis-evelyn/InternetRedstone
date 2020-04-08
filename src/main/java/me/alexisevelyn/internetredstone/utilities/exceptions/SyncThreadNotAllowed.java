package me.alexisevelyn.internetredstone.utilities.exceptions;

public class SyncThreadNotAllowed extends Exception {
    public SyncThreadNotAllowed(String message) {
        super(message);
    }

    @SuppressWarnings("unused")
    public SyncThreadNotAllowed(String message, Throwable cause) {
        super(message, cause);
    }
}
