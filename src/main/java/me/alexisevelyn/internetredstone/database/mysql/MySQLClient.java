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
            Connection con = DriverManager.getConnection(url, user, password);
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery(query);

            if (rs.next()) {
                Logger.info(ChatColor.GOLD + "MySQL Version: "
                        + ChatColor.DARK_PURPLE
                        + rs.getString(1));
            }
        } catch (SQLException exception) {
            Logger.printException(exception);
        }
    }
}
