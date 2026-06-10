package fuzs.iteminteractions.common.impl.config;

import fuzs.puzzleslib.common.api.config.v3.Config;
import fuzs.puzzleslib.common.api.config.v3.ConfigCore;

public class CommonConfig implements ConfigCore {
    @Config(description = {
            "This option allows vanilla clients to connect to a server with this mod installed.",
            "Nothing will happen when changing this option on the client.",
            "It is generally recommended to keep this option disabled, as it partially breaks the selected item feature."
    }, gameRestart = true)
    public boolean supportVanillaConnections = false;
}
