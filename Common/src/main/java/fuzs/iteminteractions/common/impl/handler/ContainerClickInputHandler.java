package fuzs.iteminteractions.common.impl.handler;

import fuzs.iteminteractions.common.api.v2.world.item.storage.ItemStorageHolder;
import fuzs.iteminteractions.common.impl.ItemInteractions;
import fuzs.puzzleslib.common.api.event.v1.core.EventResult;
import fuzs.puzzleslib.common.api.network.v4.NetworkingHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class ContainerClickInputHandler {

    public static EventResult onContainerItemClicked(ItemStack hoveredItem, Slot hoveredSlot, ItemStack itemHeldByCursor, SlotAccess slotHeldByCursor, ClickAction clickAction, Player player) {
        // Filter out vanilla clients, so they do not trigger any interactions from the mod.
        if (player instanceof ServerPlayer serverPlayer && !NetworkingHelper.isModPresentClientside(serverPlayer,
                ItemInteractions.MOD_ID)) {
            return EventResult.PASS;
        }

        ItemStorageHolder holderHeldByCursor = ItemStorageHolder.ofItem(itemHeldByCursor);
        if (holderHeldByCursor.allowModification(itemHeldByCursor, player)) {
            return holderHeldByCursor.overrideStackedOnOther(itemHeldByCursor, hoveredSlot, clickAction, player) ?
                    EventResult.ALLOW : EventResult.DENY;
        }

        ItemStorageHolder hoveredHolder = ItemStorageHolder.ofItem(hoveredItem);
        if (hoveredHolder.allowModification(hoveredItem, player)) {
            return hoveredHolder.overrideOtherStackedOnMe(hoveredItem,
                    itemHeldByCursor,
                    hoveredSlot,
                    clickAction,
                    player,
                    slotHeldByCursor) ? EventResult.ALLOW : EventResult.DENY;
        }

        return EventResult.PASS;
    }
}
