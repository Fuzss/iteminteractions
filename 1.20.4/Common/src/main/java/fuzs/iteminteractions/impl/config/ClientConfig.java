package fuzs.iteminteractions.impl.config;

import fuzs.iteminteractions.impl.client.core.HeldActivationType;
import fuzs.iteminteractions.impl.client.handler.KeyBindingTogglesHandler;
import fuzs.puzzleslib.api.config.v3.Config;
import fuzs.puzzleslib.api.config.v3.ConfigCore;

public class ClientConfig implements ConfigCore {
    private static final String ACTIVATION_TYPE_MESSAGE = "Select a modifier key required to be held, otherwise selecting \"KEY\" serves as a toggle. The key is defined in vanilla's controls menu.";

    @Config(description = "Color item inventories on tooltips according to the container item's color.")
    public boolean colorfulTooltips = true;
    @Config(name = "reveal_contents", description = {"Expand container item tooltips to reveal their contents.", ACTIVATION_TYPE_MESSAGE})
    @Config.AllowedValues(values = {"KEY", "ALWAYS", "SHIFT", "CONTROL", "ALT"})
    String visualItemContentsRaw = KeyBindingTogglesHandler.ALWAYS.getIdentifier();
    @Config(description = "Render a white overlay or the hotbar selected item frame over the slot the next item will be taken out of when right-clicking the container item.")
    public SlotOverlay slotOverlay = SlotOverlay.HOVER;
    @Config(description = "Show an indicator on container items when the stack carried by the cursor can be added in your inventory.")
    public boolean containerItemIndicator = true;
    @Config(name = "selected_item_tooltips", description = {"Show a tooltip for the item currently selected in a container item's tooltip next to the main tooltip.", ACTIVATION_TYPE_MESSAGE})
    @Config.AllowedValues(values = {"KEY", "ALWAYS", "SHIFT", "CONTROL", "ALT"})
    String selectedItemTooltipsRaw = KeyBindingTogglesHandler.ALWAYS.getIdentifier();
    @Config(name = "precision_mode", description = {"Select a modifier key required to be held to use precision mode.", ServerConfig.PRECISION_MODE_MESSAGE})
    @Config.AllowedValues(values = {"SHIFT", "CONTROL", "ALT"})
    String extractSingleItemRaw = KeyBindingTogglesHandler.CONTROL.getIdentifier();
    @Config(description = "Disable sounds from inserting and extracting items from playing, as they trigger quite often with all the new interactions.")
    public boolean disableInteractionSounds = true;
    @Config(name = "carried_item_tooltips", description = {"Always show item tooltips while interacting with container items, even when the cursor is currently carrying an item.", ACTIVATION_TYPE_MESSAGE})
    @Config.AllowedValues(values = {"KEY", "ALWAYS", "SHIFT", "CONTROL", "ALT"})
    String carriedItemTooltipsRaw = KeyBindingTogglesHandler.ALT.getIdentifier();
    @Config(description = "Invert scroll wheel direction for extracting / inserting items from a container item in precision mode.")
    public boolean invertPrecisionModeScrolling = false;

    public HeldActivationType revealContents;
    public HeldActivationType selectedItemTooltips;
    public HeldActivationType precisionMode;
    public HeldActivationType carriedItemTooltips;

    @Override
    public void afterConfigReload() {
        this.revealContents = HeldActivationType.getActivationTypeById(this.visualItemContentsRaw, KeyBindingTogglesHandler.VISUAL_ITEM_CONTENTS_KEY);
        this.selectedItemTooltips = HeldActivationType.getActivationTypeById(this.selectedItemTooltipsRaw, KeyBindingTogglesHandler.SELECTED_ITEM_TOOLTIPS_KEY);
        this.precisionMode = HeldActivationType.getActivationTypeById(this.extractSingleItemRaw, null);
        this.carriedItemTooltips = HeldActivationType.getActivationTypeById(this.carriedItemTooltipsRaw, KeyBindingTogglesHandler.CARRIED_ITEM_TOOLTIPS_KEY);
    }

    public enum SlotOverlay {
        HOTBAR, HOVER
    }
}
