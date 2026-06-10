package fuzs.iteminteractions.common.impl.network.config;

import fuzs.iteminteractions.common.impl.ItemInteractions;
import fuzs.iteminteractions.common.impl.config.CommonConfig;
import fuzs.puzzleslib.common.api.network.v4.message.MessageListener;
import fuzs.puzzleslib.common.api.network.v4.message.configuration.ClientboundConfigurationMessage;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record ClientboundCommonConfigMessage(boolean supportVanillaConnections) implements ClientboundConfigurationMessage {
    public static final StreamCodec<ByteBuf, ClientboundCommonConfigMessage> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL,
            ClientboundCommonConfigMessage::supportVanillaConnections,
            ClientboundCommonConfigMessage::new);

    public ClientboundCommonConfigMessage() {
        this(ItemInteractions.CONFIG.get(CommonConfig.class).supportVanillaConnections());
    }

    @Override
    public MessageListener<Context> getListener() {
        return new MessageListener<Context>() {
            @Override
            public void accept(Context context) {
                // This option is only supported when connected to a multiplayer server.
                boolean supportVanillaConnections = ClientboundCommonConfigMessage.this.supportVanillaConnections
                        && !context.packetListener().connection.isMemoryConnection();
                CommonConfig.setSyncedSupportVanillaConnections(supportVanillaConnections);
            }
        };
    }
}
