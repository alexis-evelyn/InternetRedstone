package me.alexisevelyn.internetredstone.utilities.data;

import lombok.Data;
import me.alexisevelyn.internetredstone.utilities.Translator;
import java.util.UUID;

@Data
public class PlayerSettings {
    final UUID UUID;
    String locale = "en";
    Translator translator;
}
