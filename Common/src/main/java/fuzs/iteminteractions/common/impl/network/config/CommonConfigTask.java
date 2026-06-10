package fuzs.iteminteractions.common.impl.network.config;

import fuzs.iteminteractions.common.impl.ItemInteractions;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.network.ConfigurationTask;
import net.minecraft.server.network.ServerConfigurationPacketListenerImpl;

import java.util.function.Consumer;

public record CommonConfigTask(ServerConfigurationPacketListenerImpl listener) implements ConfigurationTask {
    public static final Type TYPE = new Type(ItemInteractions.id("common_config").toString());

    @Override
    public void start(Consumer<Packet<?>> connection) {
        connection.accept(new ClientboundCommonConfigMessage().toPacket());
        this.listener().finishCurrentTask(this.type());
    }

    @Override
    public Type type() {
        return TYPE;
    }
}
