package me.alexisevelyn.internetredstone.utilities;

import java.math.BigInteger;

public class GenericUtilities {
    // Just Some Random Utility Functions Not Related To Lecterns

    // Pulled From: https://stackoverflow.com/a/2149927/6828099
    public static String toHex(String arg) {
        return String.format("%040x", new BigInteger(1, arg.getBytes()));
    }
}
