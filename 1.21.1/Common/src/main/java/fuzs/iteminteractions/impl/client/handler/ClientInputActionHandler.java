package fuzs.iteminteractions.impl.client.handler;

import com.mojang.blaze3d.platform.InputConstants;
import fuzs.iteminteractions.api.v1.provider.ItemContentsBehavior;
import fuzs.iteminteractions.impl.ItemInteractions;
import fuzs.iteminteractions.impl.config.ClientConfig;
import fuzs.iteminteractions.impl.config.ServerConfig;
import fuzs.iteminteractions.impl.network.client.C2SContainerClientInputMessage;
import fuzs.iteminteractions.impl.world.inventory.ContainerSlotHelper;
import fuzs.iteminteractions.impl.world.item.container.ItemContentsProviders;
import fuzs.puzzleslib.api.event.v1.core.EventResult;
import fuzs.puzzleslib.api.event.v1.data.DefaultedFloat;
import fuzs.puzzleslib.api.event.v1.data.MutableValue;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.Holder;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class ClientInputActionHandler {
    private static int lastSentContainerSlot = -1;
    private static boolean lastSentExtractSingleItem;

    public static EventResult onBeforeKeyPressed(AbstractContainerScreen<?> screen, int keyCode, int scanCode, int modifiers) {
        // this must be sent before any slot click action is performed server side, by vanilla this can be caused by either mouse clicks (normal menu interactions)
        // or key presses (hotbar keys for swapping items to those slots)
        // this is already added via mixin to where vanilla sends the click packet, but creative screen doesn't use it, and you never know with other mods...
        ensureHasSentContainerClientInput(screen, screen.minecraft.player, false);
        return EventResult.PASS;
    }

    public static EventResult onBeforeMousePressed(AbstractContainerScreen<?> screen, double mouseX, double mouseY, int button) {
        // this must be sent before any slot click action is performed server side, by vanilla this can be caused by either mouse clicks (normal menu interactions)
        // or key presses (hotbar keys for swapping items to those slots)
        // this is already added via mixin to where vanilla sends the click packet, but creative screen doesn't use it, and you never know with other mods...
        ensureHasSentContainerClientInput(screen, screen.minecraft.player, false);
        return EventResult.PASS;
    }

    public static EventResult onBeforeMouseRelease(AbstractContainerScreen<?> screen, double mouseX, double mouseY, int button) {
        // prevent vanilla double click feature from interfering with our precision mode, adding an unnecessary delay when quickly inserting items via left-click
        // it wouldn't work anyway, and right-click is fine, leading to inconsistent behavior
        if (precisionModeAllowedAndActive() && !getContainerStack(screen, false).isEmpty()) {
            screen.doubleclick = false;
        }

        return EventResult.PASS;
    }

    public static void onAfterRender(AbstractContainerScreen<?> screen, GuiGraphics guiGraphics, int mouseX, int mouseY, float tickDelta) {
        // renders vanilla item tooltips when a stack is carried and the cursor hovers over a container item
        // intended to be used with single item extraction/insertion feature to be able to continuously see what's going on in the container item
        if (!ItemInteractions.CONFIG.get(ClientConfig.class).carriedItemTooltips.isActive()) return;
        if (!screen.getMenu().getCarried().isEmpty()) {
            ItemStack stack = getContainerStack(screen, false);
            if (!stack.isEmpty()) {
                guiGraphics.renderTooltip(screen.font, stack, mouseX, mouseY);
            }
        }
    }

    public static EventResult onBeforeMouseScroll(AbstractContainerScreen<?> screen, double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        // allows to scroll between filled slots on a container items tooltip to select the slot to be interacted with next
        if (verticalAmount == 0.0) return EventResult.PASS;
        if (!ItemInteractions.CONFIG.get(ClientConfig.class).revealContents.isActive()) return EventResult.PASS;
        if (precisionModeAllowedAndActive()) {
            Slot hoveredSlot = screen.hoveredSlot;
            if (hoveredSlot != null) {
                if (!ItemContentsProviders.get(screen.getMenu().getCarried()).isEmpty() || !ItemContentsProviders.get(
                        hoveredSlot.getItem()).isEmpty()) {
                    int mouseButton = (ItemInteractions.CONFIG.get(ClientConfig.class).invertPrecisionModeScrolling ? verticalAmount < 0.0 : verticalAmount > 0.0) ? InputConstants.MOUSE_BUTTON_RIGHT : InputConstants.MOUSE_BUTTON_LEFT;
                    // sometimes fails to send the selected container slot before items are extracted / inserted
                    // no clue why this happens, but this seems to fix it for now
                    ensureHasSentContainerClientInput(screen, screen.minecraft.player, true);
                    screen.slotClicked(hoveredSlot, hoveredSlot.index, mouseButton, ClickType.PICKUP);
                    return EventResult.INTERRUPT;
                }
            }
        } else if (ItemInteractions.CONFIG.get(ServerConfig.class).allowSlotCycling) {
            ItemStack carriedStack = screen.getMenu().getCarried();
            if (!carriedStack.isEmpty() && !ItemInteractions.CONFIG.get(ClientConfig.class).carriedItemTooltips.isActive()) {
                return EventResult.PASS;
            }
            ItemStack itemStack = getContainerStack(screen, true);
            if (!itemStack.isEmpty()) {
                int currentContainerSlot = ContainerSlotHelper.getCurrentContainerSlot(screen.minecraft.player);
                SimpleContainer container = ItemContentsProviders.get(itemStack)
                        .getItemContainerView(itemStack, screen.minecraft.player);
                currentContainerSlot = ContainerSlotHelper.findClosestSlotWithContent(container, currentContainerSlot, verticalAmount < 0.0);
                ContainerSlotHelper.setCurrentContainerSlot(screen.minecraft.player, currentContainerSlot);
                // sometimes fails to send the selected container slot before items are extracted / inserted
                // no clue why this happens, but this seems to fix it for now
                ensureHasSentContainerClientInput(screen, screen.minecraft.player, true);
                return EventResult.INTERRUPT;
            }
        }

        return EventResult.PASS;
    }

    public static ItemStack getContainerStack(AbstractContainerScreen<?> screen, boolean requireItemContainerData) {
        ItemStack itemStack = screen.getMenu().getCarried();
        ItemContentsBehavior behavior = ItemContentsProviders.get(itemStack);
        if (!behavior.isEmpty() && (!requireItemContainerData || behavior.provider().hasContents(itemStack))) {
            return itemStack;
        }
        if (screen.hoveredSlot != null) {
            itemStack = screen.hoveredSlot.getItem();
            behavior = ItemContentsProviders.get(itemStack);
            if (!behavior.isEmpty() && (!requireItemContainerData || behavior.provider().hasContents(itemStack))) {
                return itemStack;
            }
        }

        return ItemStack.EMPTY;
    }

    public static EventResult onPlaySoundAtPosition(Level level, Entity entity, MutableValue<Holder<SoundEvent>> sound, MutableValue<SoundSource> source, DefaultedFloat volume, DefaultedFloat pitch) {
        if (!ItemInteractions.CONFIG.get(ClientConfig.class).disableInteractionSounds) return EventResult.PASS;
        if (source.get() == SoundSource.PLAYERS && (sound.get().value() == SoundEvents.BUNDLE_INSERT || sound.get().value() == SoundEvents.BUNDLE_REMOVE_ONE)) {
            return EventResult.INTERRUPT;
        }

        return EventResult.PASS;
    }

    public static boolean precisionModeAllowedAndActive() {
        return ItemInteractions.CONFIG.get(ServerConfig.class).allowPrecisionMode && ItemInteractions.CONFIG.get(ClientConfig.class).precisionMode.isActive();
    }

    public static void ensureHasSentContainerClientInput(Screen screen, Player player, boolean alwaysSendMessage) {
        if (!(screen instanceof AbstractContainerScreen<?>)) return;
        int currentContainerSlot = ContainerSlotHelper.getCurrentContainerSlot(player);
        boolean extractSingleItem = precisionModeAllowedAndActive();
        if (alwaysSendMessage || currentContainerSlot != lastSentContainerSlot || extractSingleItem != lastSentExtractSingleItem) {
            lastSentContainerSlot = currentContainerSlot;
            lastSentExtractSingleItem = extractSingleItem;
            // this is where the client sets this value, so it's important to call before click actions even when syncing isn't so important (applies mostly to creative menu)
            ContainerSlotHelper.extractSingleItem(player, extractSingleItem);
            ItemInteractions.NETWORK.sendToServer(new C2SContainerClientInputMessage(currentContainerSlot, extractSingleItem).toServerboundMessage());
        }
    }
}
