package me.alexisevelyn.internetredstone;

import com.hivemq.client.mqtt.mqtt5.Mqtt5AsyncClient;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.BlockState;
import org.bukkit.block.Lectern;
import org.bukkit.block.Sign;
import org.bukkit.block.data.AnaloguePowerable;
import org.bukkit.block.data.Powerable;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.LecternInventory;
import org.bukkit.inventory.meta.BookMeta;

import java.util.Arrays;
import java.util.UUID;

public class EventListener implements Listener {
    Main main;
    MQTTClient mqttClient;

    public EventListener(Main main, MQTTClient mqttClient) {
        this.main = main;
        this.mqttClient = mqttClient;
    }

    @EventHandler
    public void onRedstoneUpdate(BlockPhysicsEvent event) {
        BlockState snapshot = event.getBlock().getState();

        // TODO: Figure out why turning redstone off from a distance spams the MQTT Log!!!
        if (snapshot instanceof Lectern) {
            Lectern lectern = (Lectern) snapshot;
            LecternInventory inventory = (LecternInventory) lectern.getSnapshotInventory();

            // Lecterns should only have 1 slot, but that may change in the future.
            ItemStack[] lecternItems = inventory.getContents();
            int lecternSize = lecternItems.length;

            // Something's Wrong With The Lectern - No Need For Further Processing
            if (lecternSize == 0)
                return;

            ItemStack book = lecternItems[0];

            // Not An Expected Book Or No Book - No Need For Further Processing
            if (book == null || !(book.getItemMeta() instanceof BookMeta))
                return;

            BookMeta bookMeta = (BookMeta) book.getItemMeta();
            //noinspection WrapperTypeMayBePrimitive - For Reason of Need To Convert To Byte[] later on
            Integer redstonePower = lectern.getBlock().getBlockPower();

            // Note: This Page Check Keeps The Book From Being "Frozen" on the First Page. Page 0 is Page 1 of the Book!!!
            if (lectern.getPage() == 0) {
                bookMeta.setPage(1, ChatColor.DARK_PURPLE + "" + ChatColor.BOLD
                        + "Redstone Power: " + redstonePower);

                book.setItemMeta(bookMeta);

                inventory.setItem(0, book);
                lectern.update();
            }

            try {
                byte[] payload = redstonePower.toString().getBytes();
                UUID alexis_evelyn = UUID.fromString("f3b4e8a4-7f52-4b0a-a18d-1af64935a89f"); // My UUID For Testing

                Mqtt5AsyncClient client = this.mqttClient.registerClient(alexis_evelyn, lectern.getLocation());
                this.mqttClient.sendMessage(client, alexis_evelyn, payload);
            } catch (Exception exception) {
                printException(exception);
            }
        }
    }

    public void printException(Exception exception) {
        this.main.getLogger().severe(ChatColor.GOLD + "" + ChatColor.BOLD + "Exception: "
                + ChatColor.DARK_RED + "" + ChatColor.BOLD + exception.getMessage());

        this.main.getLogger().severe(ChatColor.RED + "" + ChatColor.BOLD + "---");

        for (StackTraceElement line : exception.getStackTrace()) {
            this.main.getLogger().severe(ChatColor.DARK_GREEN + "" + ChatColor.BOLD + line.getLineNumber()
                    + ChatColor.RED + "" + ChatColor.BOLD + " - "
                    + ChatColor.DARK_RED + "" + ChatColor.BOLD + line.toString());
        }

        this.main.getLogger().severe(ChatColor.RED + "" + ChatColor.BOLD + "---");
    }
}
