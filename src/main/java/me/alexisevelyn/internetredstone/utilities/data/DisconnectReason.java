package me.alexisevelyn.internetredstone.utilities.data;

import lombok.Data;

import javax.annotation.Nullable;
import java.util.UUID;

@Data
public class DisconnectReason {
    @Nullable UUID player;
    Reason reason;

    public DisconnectReason(Reason reason) {
        this.reason = reason;
    }

    public enum Reason {
        BROKEN_LECTERN,
        OTHER,
        REMOVED_BOOK,
        SERVER_SHUTDOWN,
        UNSPECIFIED

    }
}
