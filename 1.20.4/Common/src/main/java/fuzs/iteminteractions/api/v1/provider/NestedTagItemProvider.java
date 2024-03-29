package fuzs.iteminteractions.api.v1.provider;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import fuzs.iteminteractions.api.v1.ContainerItemHelper;
import fuzs.iteminteractions.impl.world.item.container.ItemInteractionHelper;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public abstract class NestedTagItemProvider implements TooltipItemContainerProvider {
    @Nullable
    private final DyeColor dyeColor;
    private final float[] backgroundColor;
    private final String[] nbtKey;
    private final List<Value> values = Lists.newArrayList();
    @Nullable
    private Set<Item> disallowedItems;

    public NestedTagItemProvider(@Nullable DyeColor dyeColor, String... nbtKey) {
        this.dyeColor = dyeColor;
        this.backgroundColor = ContainerItemHelper.INSTANCE.getBackgroundColor(dyeColor);
        this.nbtKey = nbtKey.length == 0 ? new String[]{ItemInteractionHelper.TAG_ITEMS} : nbtKey;
    }

    protected float[] getBackgroundColor() {
        return this.backgroundColor;
    }

    protected String getNbtKey() {
        return this.nbtKey[this.nbtKey.length - 1];
    }

    private String[] getNbtPath() {
        return Arrays.copyOf(this.nbtKey, this.nbtKey.length - 1);
    }

    public NestedTagItemProvider disallowValues(Collection<String> value) {
        for (String s : value) {
            this.disallowValue(s);
        }
        return this;
    }

    public NestedTagItemProvider disallowValue(String value) {
        boolean tag = value.startsWith("#");
        if (tag) value = value.substring(1);
        ResourceLocation id = ResourceLocation.tryParse(value);
        Objects.requireNonNull(id, "invalid resource location '%s'".formatted(value));
        Value valueObj = tag ? new TagValue(id) : new ItemValue(id);
        this.values.add(valueObj);
        return this;
    }

    @Override
    public boolean isItemAllowedInContainer(ItemStack containerStack, ItemStack stackToAdd) {
        if (this.disallowedItems == null) {
            this.disallowedItems = this.values.stream()
                    .flatMap(value -> value.getItems().stream())
                    .collect(ImmutableSet.toImmutableSet());
        }
        return !this.disallowedItems.contains(stackToAdd.getItem());
    }

    @Override
    public boolean hasItemContainerData(ItemStack containerStack) {
        CompoundTag tag = this.getItemContainerData(containerStack);
        return tag != null && tag.contains(this.getNbtKey());
    }

    @Nullable
    @Override
    public final CompoundTag getItemContainerData(ItemStack containerStack) {
        return this.getItemDataAtPath(this.getItemDataBase(containerStack), false);
    }

    @Nullable
    protected CompoundTag getItemDataBase(ItemStack containerStack) {
        return containerStack.getTag();
    }

    @Nullable
    private CompoundTag getItemDataAtPath(@Nullable CompoundTag tag, boolean computeIfAbsent) {
        for (String path : this.getNbtPath()) {
            if (tag != null) {
                if (tag.contains(path, Tag.TAG_COMPOUND)) {
                    tag = tag.getCompound(path);
                } else if (computeIfAbsent) {
                    CompoundTag compoundTag = new CompoundTag();
                    tag.put(path, compoundTag);
                    tag = compoundTag;
                } else {
                    tag = null;
                }
            } else {
                break;
            }
        }
        return tag;
    }

    @Override
    public final void setItemContainerData(ItemStack containerStack, ListTag itemsTag, String nbtKey) {
        CompoundTag itemDataBase = this.getItemDataBase(containerStack);
        if (itemsTag.isEmpty()) {
            CompoundTag itemData = this.getItemDataAtPath(itemDataBase, false);
            if (itemData != null) itemData.remove(nbtKey);
        } else {
            if (itemDataBase == null) itemDataBase = new CompoundTag();
            CompoundTag itemData = this.getItemDataAtPath(itemDataBase, true);
            Objects.requireNonNull(itemData, "tag at path %s was null".formatted(Arrays.toString(this.getNbtPath())));
            itemData.put(nbtKey, itemsTag);
        }
        if (itemDataBase != null && itemDataBase.isEmpty()) itemDataBase = null;
        this.setItemDataToStack(containerStack, itemDataBase);
    }

    protected void setItemDataToStack(ItemStack containerStack, @Nullable CompoundTag tag) {
        containerStack.setTag(tag);
    }

    @Override
    public void toJson(JsonObject jsonObject) {
        if (this.dyeColor != null) {
            jsonObject.addProperty("background_color", this.dyeColor.getName());
        }
        if (this.nbtKey.length != 1 || !this.nbtKey[0].equals(ItemInteractionHelper.TAG_ITEMS)) {
            jsonObject.addProperty("nbt_key", String.join("/", this.nbtKey));
        }
        if (!this.values.isEmpty()) {
            JsonArray jsonArray = new JsonArray();
            for (Value value : this.values) {
                jsonArray.add(value.getValue());
            }
            jsonObject.add("disallowed_items", jsonArray);
        }
    }

    private interface Value {

        Collection<Item> getItems();

        String getValue();
    }

    private static class ItemValue implements Value {
        private final ResourceLocation value;
        @Nullable
        private final Item item;

        public ItemValue(ResourceLocation value) {
            this.value = value;
            this.item = BuiltInRegistries.ITEM.containsKey(this.value) ? BuiltInRegistries.ITEM.get(this.value) : null;
        }

        @Override
        public Collection<Item> getItems() {
            if (this.item == null) return Collections.emptySet();
            return Collections.singleton(this.item);
        }

        @Override
        public String getValue() {
            return this.value.toString();
        }
    }

    private static class TagValue implements Value {
        private final ResourceLocation value;
        private final TagKey<Item> tag;

        public TagValue(ResourceLocation value) {
            this.value = value;
            this.tag = TagKey.create(Registries.ITEM, this.value);
        }

        @Override
        public Collection<Item> getItems() {
            List<Item> list = Lists.newArrayList();
            for (Holder<Item> holder : BuiltInRegistries.ITEM.getTagOrEmpty(this.tag)) {
                list.add(holder.value());
            }
            return list;
        }

        @Override
        public String getValue() {
            return "#" + this.value.toString();
        }
    }
}
