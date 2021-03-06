package design.aeonic.multitool.network;

import design.aeonic.multitool.content.multitool.behaviors.StructureBuildingBehavior;
import design.aeonic.multitool.registry.EMItems;
import design.aeonic.multitool.util.Locations;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.Objects;

public class MultitoolSyncHandler {
    private static final String PROTOCOL_VERSION = "MultitoolSyncHandler_v0.1";
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            Locations.make("multitool"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals);

    public static void registerMessages() {
        int id = -1;
        INSTANCE.registerMessage(++id, ServerBoundBehaviorSelectPacket.class, ServerBoundBehaviorSelectPacket::encode, ServerBoundBehaviorSelectPacket::decode, (packet, ctx) -> {
            EMItems.MULTITOOL.get().handleBehaviorSelectScreenClosed(Objects.requireNonNull(ctx.get().getSender()), packet.hand(), packet.behavior());
            ctx.get().setPacketHandled(true);
        });
        INSTANCE.registerMessage(++id, ServerBoundStructureSelectPacket.class, ServerBoundStructureSelectPacket::encode, ServerBoundStructureSelectPacket::decode, (packet, ctx) -> {
            ServerPlayer player = Objects.requireNonNull(ctx.get().getSender());
            var stack = player.getItemInHand(packet.hand());
            if (stack.is(EMItems.MULTITOOL.get()))
                if (EMItems.MULTITOOL.get().getSelectedBehavior(stack) instanceof StructureBuildingBehavior behavior)
                    behavior.handleStructureSelect(player, packet.hand(), packet.key(), packet.direction());
            ctx.get().setPacketHandled(true);
        });
    }
}
