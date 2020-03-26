package me.alexisevelyn.internetredstone;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.BlockState;
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
        if (isSign(snapshot.getBlock().getType())) {
            Sign sign = (Sign) snapshot;

//            this.main.getLogger().info(ChatColor.DARK_GREEN + "RPU: " + ChatColor.DARK_RED + snapshot.getBlock().getBlockPower());
            sign.setLine(1, ChatColor.DARK_GREEN + "RP: " + ChatColor.DARK_RED + snapshot.getBlock().getBlockPower());
            sign.update();
        }
    }

//    @EventHandler
//    public void onRedstoneUpdate(BlockRedstoneEvent event) {
//        BlockState snapshot = event.getBlock().getState();
//
//        // TODO: Get Redstone Power Level Update For Signs
//        if (isSign(snapshot.getBlock().getType())) {
//            Sign sign = (Sign) snapshot.getBlock();
//
//            this.main.getLogger().info(ChatColor.DARK_GREEN + "RPU: " + ChatColor.DARK_RED + snapshot.getBlock().getBlockPower());
//            sign.setLine(1, ChatColor.DARK_GREEN + "RPU: " + ChatColor.DARK_RED + snapshot.getBlock().getBlockPower());
//            sign.update();
//        } else {
//            this.main.getLogger().info(ChatColor.DARK_GREEN + "Redstone Update Block: " + ChatColor.DARK_RED + snapshot.getBlock().getType());
//        }
//    }

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
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK)
            return;
        if (!event.hasBlock())
            return;
        if (event.getClickedBlock() == null)
            return;

        BlockState snapshot = event.getClickedBlock().getState();

        if (isSign(snapshot.getBlock().getType())) {
            Sign sign;
            try {
//                sign = (Sign) snapshot.getBlock();
//                sign = (Sign) event.getClickedBlock();
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

    public boolean isSign(Material object) {
        // TODO: Read Sign Types From Config
        // TODO: Relocate Function to Another Class (A General Purpose Sign Handling Class For Parsing/Handling Sign Data)

        if (object == Material.ACACIA_SIGN || object == Material.ACACIA_WALL_SIGN) {
            return true;
        } else if (object == Material.BIRCH_SIGN || object == Material.BIRCH_WALL_SIGN) {
            return true;
        } else if (object == Material.DARK_OAK_SIGN || object == Material.DARK_OAK_WALL_SIGN) {
            return true;
        } else if (object == Material.JUNGLE_SIGN || object == Material.JUNGLE_WALL_SIGN) {
            return true;
        } else if (object == Material.OAK_SIGN || object == Material.OAK_WALL_SIGN) {
            return true;
        } else //noinspection RedundantIfStatement
            if (object == Material.SPRUCE_SIGN || object == Material.SPRUCE_WALL_SIGN) {
            return true;
        }

        return false;
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

        this.main.getLogger().info(ChatColor.GOLD + "" + ChatColor.BOLD + "About To Looping!!!");
        for (int x = snapX - 1; x <= snapX + 1; x++) {
            for (int z = snapZ - 1; z <= snapZ + 1; z++) {

                // Prevent Powering Corners - Still Needs Some Work. Doesn't Power Along X-Axis Now
//                if (x != snapX && z != snapZ)
//                    break;

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

                    this.main.getLogger().info(ChatColor.GOLD + "" + ChatColor.BOLD + "Powering Powerable!!!");
                } else if (surroundingBlock.getBlockData() instanceof AnaloguePowerable) {
                    // Note: Blocks that Can Take Redstone, And Have Signal Strength
                    // Currently, Daylight Detector and Redstone Wire
                    aPowerable = (AnaloguePowerable) surroundingBlock.getBlockData();

                    aPowerable.setPower(10);

                    surroundingBlock.setBlockData(aPowerable);
                    surroundingBlock.update();

                    this.main.getLogger().info(ChatColor.GOLD + "" + ChatColor.BOLD + "Powering AnaloguePowerable!!!");

                    // Useful Methods
//                    aPowerable.getMaximumPower();
//                    aPowerable.getPower();
//                    aPowerable.setPower(0);
                }
            }
        }
    }
}
