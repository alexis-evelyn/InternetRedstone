package me.alexisevelyn.internetredstone.database.mysql;

import me.alexisevelyn.internetredstone.utilities.Logger;
import org.bukkit.ChatColor;

import java.sql.*;

public class MySQLClient {
    public MySQLClient() {
        // TODO: Create database to store registered lecterns in as well as player preferences

        String url = "jdbc:mysql://localhost:3306/internetredstone?useSSL=false";
        String user = "internetredstone";

        // As you can tell, this is not a real password. This is one that goes to a localhost
        // development database. I use this database to test Minecraft plugins and other programs that need MySQL
        String password = "localhost-dev-password";

        String query = "SELECT VERSION()";

        try {
            // Load and Register the MySQL JBDC Driver If Available
            // This shouldn't be necessary anymore, but it won't hurt having this
            Class.forName("com.mysql.jdbc.Driver");

            // Setup MySQL Connection
            Connection con = DriverManager.getConnection(url, user, password);
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery(query);

            if (rs.next()) {
                Logger.info(ChatColor.GOLD + "MySQL Version: "
                        + ChatColor.DARK_PURPLE
                        + rs.getString(1));
            }
        } catch (SQLException exception) {
            Logger.severe(ChatColor.GOLD + "" + ChatColor.BOLD
                            + "SQL State: "
                            + ChatColor.DARK_PURPLE + "" + ChatColor.BOLD
                            + exception.getSQLState());

            Logger.printException(exception);
        } catch (ClassNotFoundException exception) {
            // This should never run as long as the MySQL package is not shaded!!!

            // Connector/J - https://dev.mysql.com/downloads/connector/j/
            Logger.severe(ChatColor.GOLD + "" + ChatColor.BOLD
                    + "Could Not Find JBDC Driver 'com.mysql.jdbc.Driver': "
                    + ChatColor.DARK_PURPLE + "" + ChatColor.BOLD
                    + exception.getMessage());

            Logger.severe(ChatColor.GOLD + "" + ChatColor.BOLD
                    + "Do you not have Connector/J installed? Download it at: "
                    + ChatColor.DARK_PURPLE + "" + ChatColor.BOLD
                    + "https://dev.mysql.com/downloads/connector/j/");

            Logger.printException(exception);
        }
    }
}
