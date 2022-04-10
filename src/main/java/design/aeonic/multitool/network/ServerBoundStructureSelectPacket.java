package design.aeonic.multitool.network;

import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;

public record ServerBoundStructureSelectPacket(ResourceLocation key, Direction direction, InteractionHand hand) {
    public static void encode(ServerBoundStructureSelectPacket packet, FriendlyByteBuf buf) {
        buf.writeResourceLocation(packet.key());
        buf.writeEnum(packet.direction());
        buf.writeEnum(packet.hand());
    }
    public static ServerBoundStructureSelectPacket decode(FriendlyByteBuf buf) {
        return new ServerBoundStructureSelectPacket(buf.readResourceLocation(), buf.readEnum(Direction.class), buf.readEnum(InteractionHand.class));
    }
}
