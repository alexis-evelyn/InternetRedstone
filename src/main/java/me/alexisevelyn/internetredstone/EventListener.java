package me.alexisevelyn.internetredstone;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.BlockState;
import org.bukkit.block.Lectern;
import org.bukkit.block.Sign;
import org.bukkit.block.data.AnaloguePowerable;
import org.bukkit.block.data.Powerable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.Arrays;

public class EventListener implements Listener {
    Main main;

    public EventListener(Main passed) {
        this.main = passed;
    }

    @EventHandler
    public void onRedstoneUpdate(BlockPhysicsEvent event) {
        BlockState snapshot = event.getBlock().getState();

        // TODO: Get Redstone Power Level Update For Signs
        if (snapshot instanceof Sign) {
            Sign sign = (Sign) snapshot;

//            this.main.getLogger().info(ChatColor.DARK_GREEN + "RPU: " + ChatColor.DARK_RED + snapshot.getBlock().getBlockPower());
            sign.setLine(1, ChatColor.DARK_GREEN + "RP: " + ChatColor.DARK_RED + snapshot.getBlock().getBlockPower());
            sign.update();
        }
    }

//    @EventHandler
//    public void onSignChange(SignChangeEvent event) {
//        BlockState snapshot = event.getBlock().getState();
//
////        event.getPlayer().sendMessage(ChatColor.GOLD + "Redstone Power: " + ChatColor.DARK_GREEN + snapshot.getBlock().getBlockPower());
//
//        event.setLine(1, ChatColor.DARK_GREEN + "RP: " + ChatColor.DARK_RED + snapshot.getBlock().getBlockPower());
//    }

    @EventHandler
    public void onSignClick(PlayerInteractEvent event) {
        // Block Doesn't Exist or Is Invalid
        if (!event.hasBlock() || event.getClickedBlock() == null)
            return;

        // Not A Right Click
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK)
            return;

        BlockState snapshot = event.getClickedBlock().getState();

        if (snapshot instanceof Sign) {
            Sign sign;
            try {
                sign = (Sign) snapshot;

                updateSurroundingBlocks(snapshot);

                event.getPlayer().sendMessage(ChatColor.DARK_GREEN + "RPRC: " + ChatColor.DARK_RED + snapshot.getBlock().getBlockPower());

                sign.setLine(1, ChatColor.DARK_GREEN + "RPRC: " + ChatColor.DARK_RED + snapshot.getBlock().getBlockPower());
                sign.update(); // Attempts To Save Modified Sign Data From Snapshot - Fails If Sign Is No Longer In Same State As When Captured
            } catch (ClassCastException exception) {
                event.getPlayer().sendMessage(ChatColor.DARK_GREEN + "ClassCastException: " + ChatColor.DARK_RED + exception.getMessage());

                this.main.getLogger().severe(ChatColor.GOLD + "" + ChatColor.BOLD + "ClassCastException: "
                            + ChatColor.DARK_RED + "" + ChatColor.BOLD + exception.getMessage());

                this.main.getLogger().severe(ChatColor.GOLD + "" + ChatColor.BOLD + "Clicked Block: "
                        + ChatColor.DARK_RED + "" + ChatColor.BOLD + snapshot.getType().toString());

                this.main.getLogger().severe(ChatColor.GOLD + "" + ChatColor.BOLD + "Location: "
                        + ChatColor.DARK_GREEN + "" + ChatColor.BOLD + "("
                        + ChatColor.DARK_RED + "" + ChatColor.BOLD + snapshot.getLocation().getBlockX()
                        + ChatColor.DARK_GREEN + "" + ChatColor.BOLD + ", "
                        + ChatColor.DARK_RED + "" + ChatColor.BOLD + snapshot.getLocation().getBlockY()
                        + ChatColor.DARK_GREEN + "" + ChatColor.BOLD + ", "
                        + ChatColor.DARK_RED + "" + ChatColor.BOLD + snapshot.getLocation().getBlockZ()
                        + ChatColor.DARK_GREEN + "" + ChatColor.BOLD + ")");

                this.main.getLogger().severe(ChatColor.RED + "" + ChatColor.BOLD + "---");

                for (StackTraceElement line : exception.getStackTrace()) {
                    this.main.getLogger().severe(ChatColor.DARK_GREEN + "" + ChatColor.BOLD + line.getLineNumber()
                            + ChatColor.RED + "" + ChatColor.BOLD + " - "
                            + ChatColor.DARK_RED + "" + ChatColor.BOLD + line.toString());
                }

                this.main.getLogger().severe(ChatColor.RED + "" + ChatColor.BOLD + "---");
            }
        }
    }

    public void updateSurroundingBlocks(BlockState snapshot) {
        // TODO: Take World Border Into Account
        // TODO: Make Sure To Emulate Vanilla Redstone Mechanics
        // TODO: Relocate Function to Another Class (A General Purpose Sign Handling Class For Parsing/Handling Sign Data)

        int snapX, snapY, snapZ = 0;
        World world = snapshot.getWorld();

        snapX = snapshot.getLocation().getBlockX();
        snapY = snapshot.getLocation().getBlockY();
        snapZ = snapshot.getLocation().getBlockZ();

        BlockState surroundingBlock;
        Powerable powerable;
        AnaloguePowerable aPowerable;

        for (int x = snapX - 1; x <= snapX + 1; x++) {
            for (int z = snapZ - 1; z <= snapZ + 1; z++) {

                // TODO: Prevent Powering Corners - Still Needs Some Work. Doesn't Power Along X-Axis Now
                if (x != snapX && z != snapZ)
                    continue;

//                this.main.getLogger().info(ChatColor.GOLD + "" + ChatColor.BOLD + "Looping: "
//                        + ChatColor.DARK_GREEN + "" + ChatColor.BOLD + "("
//                        + ChatColor.DARK_RED + "" + ChatColor.BOLD + x
//                        + ChatColor.DARK_GREEN + "" + ChatColor.BOLD + ", "
//                        + ChatColor.DARK_RED + "" + ChatColor.BOLD + snapY
//                        + ChatColor.DARK_GREEN + "" + ChatColor.BOLD + ", "
//                        + ChatColor.DARK_RED + "" + ChatColor.BOLD + z
//                        + ChatColor.DARK_GREEN + "" + ChatColor.BOLD + ")");

                surroundingBlock = world.getBlockAt(x, snapY, z).getState();

                if (surroundingBlock.getBlockData() instanceof Powerable) {
                    // Note: Blocks that Can Take Redstone, But Have No Signal Strength
                    powerable = (Powerable) surroundingBlock.getBlockData();

                    powerable.setPowered(!powerable.isPowered());

                    surroundingBlock.setBlockData(powerable);
                    surroundingBlock.update();
                } else if (surroundingBlock.getBlockData() instanceof AnaloguePowerable) {
                    // TODO: Fix This To Allow Non-Binary Data With Ease Of Access
                    // Note: I may use a lectern with a signed book and a specific page to handle this. You'll just need a comparator to use it.
                    // Note: Due to Block Updating, This Cannot Create A Proper Redstone Line For Analog Signal Strength :(
                    // If Anyone Has A Workaround That Allows Strength 0-15 To Be Emitted From The Redstone Wire Next To The Sign
                    // Please Let Me Know or Submit a Pull Request With The Fix!!! Thanks!!!

                    // Note: Blocks that Can Take Redstone, And Have Signal Strength
                    // Currently, Daylight Detector and Redstone Wire
//                    aPowerable = (AnaloguePowerable) surroundingBlock.getBlockData();
//
//                    aPowerable.setPower(10);
//
//                    surroundingBlock.setBlockData(aPowerable);
//                    surroundingBlock.update();

                    // Useful Methods
//                    aPowerable.getMaximumPower();
//                    aPowerable.getPower();
//                    aPowerable.setPower(0);
                }
            }
        }
    }
}
