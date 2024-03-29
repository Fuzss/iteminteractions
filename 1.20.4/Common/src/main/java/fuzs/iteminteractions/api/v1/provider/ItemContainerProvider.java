package fuzs.iteminteractions.api.v1.provider;

import com.google.gson.JsonObject;
import fuzs.iteminteractions.api.v1.ItemContainerProviderSerializers;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/**
 * An interface that when implemented represents a provider for any item to enable bundle-like inventory item
 * interactions (extracting and adding items via right-clicking on the item) and bundle-like tooltips.
 * <p>
 * A container does not necessarily need to provide both item interactions and tooltips, what is provided is defined by
 * implementing {@link ItemContainerProvider#allowsPlayerInteractions} and
 * {@link ItemContainerProvider#canProvideTooltipImage}.
 * <p>
 * This overrides any already implemented behavior (the default providers in Easy Shulker Boxes actually do this for
 * vanilla bundles).
 */
public interface ItemContainerProvider {

    /**
     * Does this provider support item inventory interactions (extracting and adding items) on the given
     * <code>containterStack</code>.
     *
     * @param containerStack the container stack
     * @param player         the player performing the interaction
     * @return are inventory interactions allowed (is a container present on this item)
     */
    default boolean allowsPlayerInteractions(ItemStack containerStack, Player player) {
        return containerStack.getCount() == 1;
    }

    /**
     * Get the maximum stack size for this item in the current container.
     * <p>
     * Allows supporting containers with custom max stack sizes such as our Limitless Container library.
     *
     * @param container current container
     * @param itemStack item stack to retrieve stack size for
     * @return supported max stack size
     */
    default int getMaxStackSize(Container container, ItemStack itemStack) {
        return itemStack.getMaxStackSize();
    }

    /**
     * does the item stack have data for stored items
     * <p>an easy check if the corresponding container is empty without having to create a container instance
     * <p>mainly used by tooltip image and client-side mouse scroll handler
     *
     * @param containerStack the container stack
     * @return is the item stack tag with stored item data present
     */
    boolean hasItemContainerData(ItemStack containerStack);

    /**
     * read the item tag containing the stored inventory (usually as an <code>Items</code> sub-tag)
     * <p>this method is mainly required for items with block entity data, as the inventory is stored as part of the
     * block entity data, not directly in the item tag, so we need to override this method then
     *
     * @param containerStack stack to read item tag from
     * @return the tag
     */
    @Nullable CompoundTag getItemContainerData(ItemStack containerStack);

    /**
     * Sets items stored in a {@link ListTag} to an item stack's tag, if the list is empty the tag key
     * (<code>nbtKey</code>) is properly removed (possibly removing the whole stack tag if it is empty afterward).
     *
     * @param containerStack stack to set item tag data for
     * @param itemsTag       items to save as {@link ListTag}
     * @param nbtKey         nbt key to store <code>itemsTag</code> as
     */
    void setItemContainerData(ItemStack containerStack, ListTag itemsTag, String nbtKey);

    /**
     * called on the client-side to sync changes made during inventory item interactions back to the server
     * <p>this only works in the creative inventory, as any other menu does not allow the client to sync changes
     * <p>only really need for ender chests right now
     *
     * @param containerStack item stack providing the container
     * @param player         the player performing the item interaction
     */
    default void broadcastContainerChanges(ItemStack containerStack, Player player) {
        // NO-OP
    }

    /**
     * Is <code>stackToAdd</code> allowed to be added to the container supplied by <code>containerStack</code>.
     * <p>
     * This should be the same behavior as vanilla's {@link Item#canFitInsideContainerItems()}.
     *
     * @param containerStack the item stack providing the container to add <code>stackToAdd</code> to
     * @param stackToAdd     the stack to be added to the container
     * @return is <code>stack</code> allowed to be added to the container
     */
    default boolean isItemAllowedInContainer(ItemStack containerStack, ItemStack stackToAdd) {
        return true;
    }

    /**
     * Is there enough space in the container provided by <code>containerStack</code> to add <code>stack</code> (not
     * necessarily the full stack).
     * <p>
     * Before this is called {@link #allowsPlayerInteractions(ItemStack, Player)} and
     * {@link #isItemAllowedInContainer(ItemStack, ItemStack)} are checked.
     *
     * @param containerStack the item stack providing the container to add <code>stack</code> to
     * @param stackToAdd     the stack to be added to the container
     * @param player         the player interacting with both items
     * @return is adding any portion of <code>stackToAdd</code> to the container possible
     */
    default boolean canAddItem(ItemStack containerStack, ItemStack stackToAdd, Player player) {
        return this.getItemContainer(containerStack, player, false).canAddItem(stackToAdd);
    }

    /**
     * Get the container implementation provided by <code>containerStack</code> as a {@link SimpleContainer}, must not
     * return <code>null</code>.
     *
     * @param containerStack item stack providing the container
     * @param player         player involved in the interaction
     * @param allowSaving    attach a saving listener to the container (this is set to <code>false</code> when creating
     *                       a container e.g. for rendering a tooltip)
     * @return the provided container
     */
    SimpleContainer getItemContainer(ItemStack containerStack, @Nullable Player player, boolean allowSaving);

    /**
     * How much space is available in the container provided by <code>containerStack</code> to add
     * <code>stackToAdd</code>.
     * <p>
     * Mainly used by bundles, otherwise {@link ItemContainerProvider#canAddItem} should be enough.
     * <p>
     * Before this is called {@link #allowsPlayerInteractions(ItemStack, Player)} and
     * {@link #isItemAllowedInContainer(ItemStack, ItemStack)} are checked.
     *
     * @param containerStack the item stack providing the container to add <code>stackToAdd</code> to
     * @param stackToAdd     the stack to be added to the container
     * @param player         the player interacting with both item stacks
     * @return the portion of <code>stackToAdd</code> that can be added to the container
     */
    default int getAcceptableItemCount(ItemStack containerStack, ItemStack stackToAdd, Player player) {
        return stackToAdd.getCount();
    }

    /**
     * Does this provider support an image tooltip.
     * <p>
     * This is required despite {@link #getTooltipImage} providing an {@link Optional} when overriding the tooltip image
     * for items which normally provide their own (like bundles).
     *
     * @param containerStack the item stack providing the container to show a tooltip for
     * @param player         player involved in the interaction
     * @return does <code>containerStack</code> provide a tooltip image
     */
    boolean canProvideTooltipImage(ItemStack containerStack, Player player);

    /**
     * The image tooltip provided by the item stack.
     *
     * @param containerStack the item stack providing the container to show a tooltip for
     * @param player         player involved in the interaction
     * @return the image tooltip provided by the item stack.
     */
    Optional<TooltipComponent> getTooltipImage(ItemStack containerStack, Player player);

    /**
     * Serialize this provider to json for syncing to client (as part of data pack contents).
     * <p>
     * A serializer needs to be registered in {@link ItemContainerProviderSerializers#register} additionally.
     *
     * @param jsonObject the json object to add data to
     */
    void toJson(JsonObject jsonObject);
}
