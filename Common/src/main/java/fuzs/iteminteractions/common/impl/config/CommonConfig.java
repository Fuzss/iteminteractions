package fuzs.iteminteractions.common.impl.config;

import fuzs.puzzleslib.common.api.config.v3.Config;
import fuzs.puzzleslib.common.api.config.v3.ConfigCore;
import fuzs.puzzleslib.common.api.core.v1.ModLoaderEnvironment;
import org.jspecify.annotations.Nullable;

import java.util.Objects;

public class CommonConfig implements ConfigCore {
    @Config(description = {
            "This option allows vanilla clients to connect to a server with this mod installed.",
            "Nothing will happen when changing this option on the client.",
            "It is generally recommended to keep this option disabled, as it partially breaks the selected item feature."
    }, gameRestart = true)
    boolean supportVanillaConnections = false;
    @Nullable
    private Boolean originalSupportVanillaConnections;
    private static boolean syncedSupportVanillaConnections = false;

    /**
     * Freeze this on first access as it controls registering the data component, which then won't be available when the
     * setting is changed during runtime.
     */
    public boolean supportVanillaConnections() {
        return Objects.requireNonNullElseGet(this.originalSupportVanillaConnections,
                () -> this.originalSupportVanillaConnections = this.supportVanillaConnections);
    }

    /**
     * This will not be synced on dedicated servers; otherwise it's fine, since the static field is the same for all
     * threads on the physical client.
     */
    public boolean syncedSupportVanillaConnections() {
        if (ModLoaderEnvironment.INSTANCE.isClient()) {
            return syncedSupportVanillaConnections;
        } else {
            return this.supportVanillaConnections();
        }
    }

    public static void setSyncedSupportVanillaConnections(boolean supportVanillaConnections) {
        syncedSupportVanillaConnections = supportVanillaConnections;
    }
}
