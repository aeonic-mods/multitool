package design.aeonic.multitool.network;

import design.aeonic.multitool.api.Registries;
import design.aeonic.multitool.api.multitool.MultitoolBehavior;
import design.aeonic.multitool.content.multitool.behaviors.EmptyBehavior;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.InteractionHand;

public record ServerBoundBehaviorSelectPacket(MultitoolBehavior behavior, InteractionHand hand) {
    public static void encode(ServerBoundBehaviorSelectPacket packet, FriendlyByteBuf buf) {
        var key = packet.behavior().getRegistryName();
        buf.writeResourceLocation(key == null ? EmptyBehavior.KEY : key);
        buf.writeEnum(packet.hand());
    }
    public static ServerBoundBehaviorSelectPacket decode(FriendlyByteBuf buf) {
        var behavior = Registries.MULTITOOL_BEHAVIORS.getValue(buf.readResourceLocation());
        return new ServerBoundBehaviorSelectPacket(behavior == null ? EmptyBehavior.INSTANCE : behavior, buf.readEnum(InteractionHand.class));
    }
}
