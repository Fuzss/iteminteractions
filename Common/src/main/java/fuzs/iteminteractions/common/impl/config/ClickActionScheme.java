package fuzs.iteminteractions.common.impl.config;

import com.mojang.serialization.Codec;
import fuzs.puzzleslib.common.api.network.v4.codec.ExtraStreamCodecs;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.item.ItemStack;

import java.util.Locale;

public enum ClickActionScheme implements StringRepresentable {
    SPLIT_INPUT {
        @Override
        public boolean insertStackedOnOther(ClickAction clickAction, ItemStack otherItem, boolean extractSingleItemOnly) {
            return clickAction == ClickAction.PRIMARY && (!otherItem.isEmpty() || extractSingleItemOnly);
        }

        @Override
        public boolean removeStackedOnOther(ClickAction clickAction, ItemStack otherItem, boolean extractSingleItemOnly) {
            return clickAction == ClickAction.SECONDARY && (otherItem.isEmpty() || extractSingleItemOnly);
        }

        @Override
        public boolean insertOtherStackedOnMe(ClickAction clickAction, ItemStack itemHeldByCursor, boolean extractSingleItemOnly) {
            return clickAction == ClickAction.PRIMARY && !itemHeldByCursor.isEmpty();
        }

        @Override
        public boolean removeOtherStackedOnMe(ClickAction clickAction, ItemStack itemHeldByCursor, boolean extractSingleItemOnly) {
            return clickAction == ClickAction.SECONDARY && (itemHeldByCursor.isEmpty() || extractSingleItemOnly);
        }
    },
    SINGLE_INPUT {
        @Override
        public boolean insertStackedOnOther(ClickAction clickAction, ItemStack otherItem, boolean extractSingleItemOnly) {
            return clickAction == ClickAction.SECONDARY && !otherItem.isEmpty() || extractSingleItemOnly;
        }

        @Override
        public boolean removeStackedOnOther(ClickAction clickAction, ItemStack otherItem, boolean extractSingleItemOnly) {
            return clickAction == ClickAction.SECONDARY && (otherItem.isEmpty() || extractSingleItemOnly);
        }

        @Override
        public boolean insertOtherStackedOnMe(ClickAction clickAction, ItemStack itemHeldByCursor, boolean extractSingleItemOnly) {
            return (clickAction == ClickAction.SECONDARY || extractSingleItemOnly) && !itemHeldByCursor.isEmpty();
        }

        @Override
        public boolean removeOtherStackedOnMe(ClickAction clickAction, ItemStack itemHeldByCursor, boolean extractSingleItemOnly) {
            return clickAction == ClickAction.SECONDARY && (itemHeldByCursor.isEmpty() || extractSingleItemOnly);
        }
    };

    public static final Codec<ClickActionScheme> CODEC = StringRepresentable.fromEnum(ClickActionScheme::values);
    public static final StreamCodec<ByteBuf, ClickActionScheme> STREAM_CODEC = ExtraStreamCodecs.fromEnum(
            ClickActionScheme.class);

    public abstract boolean insertStackedOnOther(ClickAction clickAction, ItemStack otherItem, boolean extractSingleItemOnly);

    public abstract boolean removeStackedOnOther(ClickAction clickAction, ItemStack otherItem, boolean extractSingleItemOnly);

    public abstract boolean insertOtherStackedOnMe(ClickAction clickAction, ItemStack itemHeldByCursor, boolean extractSingleItemOnly);

    public abstract boolean removeOtherStackedOnMe(ClickAction clickAction, ItemStack itemHeldByCursor, boolean extractSingleItemOnly);

    @Override
    public String getSerializedName() {
        return this.name().toLowerCase(Locale.ROOT);
    }
}
