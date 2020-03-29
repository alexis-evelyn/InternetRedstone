package me.alexisevelyn.internetredstone.utilities.exceptions;

public class InvalidLectern extends Exception {
    public InvalidLectern(String message) {
        super(message);
    }

    public InvalidLectern(String message, Throwable cause) {
        super(message, cause);
    }
}
