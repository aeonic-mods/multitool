package design.aeonic.multitool.content.multitool.networking;

import design.aeonic.multitool.registry.EMItems;
import design.aeonic.multitool.util.Locations;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.Objects;

public class MultitoolPacketHandler {
    private static final String PROTOCOL_VERSION = "awooga booga";
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            Locations.make("multitool"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals);

    public static void registerMessages() {
        int id = -1;
        INSTANCE.registerMessage(++id, BehaviorSelectPacket.class, BehaviorSelectPacket::encode, BehaviorSelectPacket::decode, (packet, ctx) -> {
            EMItems.MULTITOOL.get().handleBehaviorSelectScreenClosed(Objects.requireNonNull(ctx.get().getSender()), packet.hand(), packet.behavior());
            ctx.get().setPacketHandled(true);
        });
    }
}
