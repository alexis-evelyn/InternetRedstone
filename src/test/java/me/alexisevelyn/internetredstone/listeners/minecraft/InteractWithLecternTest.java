package me.alexisevelyn.internetredstone.listeners.minecraft;

import me.alexisevelyn.internetredstone.Main;
import me.alexisevelyn.internetredstone.utilities.LecternTrackers;
import me.alexisevelyn.internetredstone.utilities.exceptions.DuplicateObjectException;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.UUID;

@RunWith(PowerMockRunner.class)
@PrepareForTest(PlayerInteractEvent.class)
public class InteractWithLecternTest {
    // TODO: Make This Test Work!!!

    @Mock Main main;
    @Mock LecternTrackers trackers;
    @Mock Location location;

    @Test
    public void testDetectLectern() {
        // Initialize Objects For Mocking
        PlayerInteractEvent playerInteractEvent = PowerMockito.mock(PlayerInteractEvent.class);
        Player player = Mockito.mock(Player.class);

        // Use Notch's IGN and UUID for First Player Test
        Mockito.when(player.getName()).thenReturn("Notch");
        Mockito.when(player.getUniqueId()).thenReturn(UUID.fromString("069a79f4-44e9-4726-a5be-fca90e38aaf5"));

        // Return Mock Player When PlayerInteractEvent is Called
        Mockito.when(playerInteractEvent.getPlayer()).thenReturn(player);

        // TODO: Mock Book and Other Classes Too
        //  But First, Clean Up Code and Rewrite

        // As you can see, I don't know how to set this up properly
        InteractWithLectern interactWithLectern = new InteractWithLectern(trackers);

        // Send in fake player "Notch"
        interactWithLectern.interactWithLectern(playerInteractEvent);

        try {
            // Verify Tracker Registry Function Was Called!!!
            Mockito.verify(trackers).registerTracker(location, player.getUniqueId());
            System.out.println("Mock Test Verified Registering Tracker With LecternTrackers!!!");
        } catch (DuplicateObjectException exception) {
            System.out.println("Mock Test Called DuplicateObjectException!!!");
        }
    }
}
