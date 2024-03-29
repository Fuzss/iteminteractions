package fuzs.iteminteractions.api.v1;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import fuzs.iteminteractions.api.v1.provider.*;
import fuzs.iteminteractions.impl.world.item.container.ItemInteractionHelper;
import fuzs.puzzleslib.api.config.v3.json.GsonEnumHelper;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.StreamSupport;

public class ItemContainerProviderBuilder {
    public int inventoryWidth;
    public int inventoryHeight;
    @Nullable
    public DyeColor dyeColor;
    public String[] nbtKey;
    public boolean filterContainerItems;
    public BlockEntityType<?> blockEntityType;
    public int capacity;
    public List<String> disallowedItems;
    public boolean anyGameMode;
    @Nullable
    public EquipmentSlot equipmentSlot;

    public ItemContainerProviderBuilder(JsonElement jsonElement) {
        this.fromJson(jsonElement);
    }

    public static Function<JsonElement, ItemContainerProvider> fromJson(Function<ItemContainerProviderBuilder, ItemContainerProvider> factory) {
        return jsonElement -> {
            ItemContainerProviderBuilder builder = new ItemContainerProviderBuilder(jsonElement);
            ItemContainerProvider provider = factory.apply(builder);
            if (provider instanceof SimpleItemProvider itemProvider && builder.filterContainerItems) {
                itemProvider.filterContainerItems();
            }
            if (provider instanceof NestedTagItemProvider nestedTagProvider) {
                nestedTagProvider.disallowValues(builder.disallowedItems);
            }
            return provider;
        };
    }

    private void fromJson(JsonElement jsonElement) {
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        this.inventoryWidth = GsonHelper.getAsInt(jsonObject, "inventory_width", -1);
        this.inventoryHeight = GsonHelper.getAsInt(jsonObject, "inventory_height", -1);
        this.dyeColor = DyeColor.byName(GsonHelper.getAsString(jsonObject, "background_color", ""), null);
        this.nbtKey = GsonHelper.getAsString(jsonObject, "nbt_key", ItemInteractionHelper.TAG_ITEMS).split("/");
        this.filterContainerItems = GsonHelper.getAsBoolean(jsonObject, "filter_container_items", false);
        if (jsonObject.has("block_entity_type")) {
            ResourceLocation blockEntityTypeKey = new ResourceLocation(GsonHelper.getAsString(jsonObject, "block_entity_type"));
            this.blockEntityType = BuiltInRegistries.BLOCK_ENTITY_TYPE.get(blockEntityTypeKey);
        }
        this.capacity = GsonHelper.getAsInt(jsonObject, "capacity", -1);
        JsonArray disallowedItemsData = GsonHelper.getAsJsonArray(jsonObject, "disallowed_items", new JsonArray());
        this.disallowedItems = StreamSupport.stream(disallowedItemsData.spliterator(), false).map(JsonElement::getAsString).toList();
        this.anyGameMode = GsonHelper.getAsBoolean(jsonObject, "any_game_mode", false);
        this.equipmentSlot = GsonEnumHelper.getAsEnum(jsonObject, "equipment_slot", EquipmentSlot.class, null);
    }

    public ItemContainerProvider toSimpleItemContainerProvider() {
        this.checkInventorySize("item");
        return new SimpleItemProvider(this.inventoryWidth, this.inventoryHeight, this.dyeColor, this.nbtKey).equipmentSlot(this.equipmentSlot);
    }

    public ItemContainerProvider toBlockEntityProvider() {
        this.checkInventorySize("block_entity");
        Objects.requireNonNull(this.blockEntityType, getErrorMessage("block_entity_type", "block_entity"));
        BlockEntityProvider provider = new BlockEntityProvider(this.blockEntityType, this.inventoryWidth, this.inventoryHeight, this.dyeColor, this.nbtKey);
        if (this.anyGameMode) provider.anyGameMode();
        return provider.equipmentSlot(this.equipmentSlot);
    }

    public ItemContainerProvider toBlockEntityViewProvider() {
        this.checkInventorySize("block_entity_view");
        Objects.requireNonNull(this.blockEntityType, getErrorMessage("block_entity_type", "block_entity_view"));
        return new BlockEntityViewProvider(this.blockEntityType, this.inventoryWidth, this.inventoryHeight, this.dyeColor, this.nbtKey).equipmentSlot(this.equipmentSlot);
    }

    public ItemContainerProvider toBundleProvider() {
        if (this.capacity == -1) {
            throw new IllegalStateException(getErrorMessage("capacity", "bundle"));
        }
        return new BundleProvider(this.capacity, this.dyeColor, this.nbtKey);
    }

    public ItemContainerProvider toEnderChestProvider() {
        return new EnderChestProvider();
    }

    public void checkInventorySize(String type) {
        if (this.inventoryWidth == -1)
            throw new IllegalStateException(getErrorMessage("inventory_width", type));
        if (this.inventoryHeight == -1)
            throw new IllegalStateException(getErrorMessage("inventory_height", type));
    }

    public static String getErrorMessage(String jsonKey, String providerType) {
        return "'%s' not set for provider of type '%s'".formatted(jsonKey, providerType);
    }
}
