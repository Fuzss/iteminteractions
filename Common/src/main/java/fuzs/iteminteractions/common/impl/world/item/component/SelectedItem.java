package fuzs.iteminteractions.common.impl.world.item.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import fuzs.iteminteractions.common.impl.ItemInteractions;
import fuzs.iteminteractions.common.impl.config.CommonConfig;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;

import java.util.Objects;

public abstract sealed class SelectedItem permits SelectedItem.Component, SelectedItem.Attachment {
    public static final int DEFAULT_SELECTED_ITEM = -1;
    public static final SelectedItem DEFAULT = ofUnchecked(DEFAULT_SELECTED_ITEM);
    public static final Codec<SelectedItem> CODEC = MapCodec.unitCodec(DEFAULT);
    public static final StreamCodec<ByteBuf, SelectedItem> STREAM_CODEC = StreamCodec.unit(DEFAULT);

    final int selectedItemIndex;

    SelectedItem(int selectedItemIndex) {
        this.selectedItemIndex = selectedItemIndex;
    }

    private static SelectedItem ofUnchecked(int selectedItemIndex) {
        if (ItemInteractions.CONFIG.get(CommonConfig.class).supportVanillaConnections) {
            return new Attachment(selectedItemIndex);
        } else {
            return new Component(selectedItemIndex);
        }
    }

    public static SelectedItem of(int selectedItemIndex) {
        if (selectedItemIndex == DEFAULT_SELECTED_ITEM) {
            return DEFAULT;
        } else if (selectedItemIndex >= 0) {
            return ofUnchecked(selectedItemIndex);
        } else {
            throw new IllegalArgumentException("Invalid selectedItemIndex: " + selectedItemIndex);
        }
    }

    public int selectedItem() {
        return this.selectedItemIndex;
    }

    @Override
    public String toString() {
        return "SelectedItem[" + this.selectedItemIndex + "]";
    }

    static final class Component extends SelectedItem {

        Component(int selectedItemIndex) {
            super(selectedItemIndex);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            } else {
                return obj instanceof SelectedItem.Component;
            }
        }

        @Override
        public int hashCode() {
            return SelectedItem.Component.class.hashCode();
        }
    }

    static final class Attachment extends SelectedItem {

        Attachment(int selectedItemIndex) {
            super(selectedItemIndex);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            } else if (obj == null || this.getClass() != obj.getClass()) {
                return false;
            } else {
                return this.selectedItemIndex == ((SelectedItem.Attachment) obj).selectedItemIndex;
            }
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.selectedItemIndex);
        }
    }
}
