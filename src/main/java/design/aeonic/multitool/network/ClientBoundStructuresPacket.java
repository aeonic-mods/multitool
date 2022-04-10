package design.aeonic.multitool.network;

import design.aeonic.multitool.api.structure.BuildableStructure;
import design.aeonic.multitool.api.structure.StructureInfo;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Map;

/**
 * We use this packet to sync buildable structures - we don't need to read the actual recipes on the client; they will be checked on the server when built.
 */
public record ClientBoundStructuresPacket(Map<ResourceLocation, Pair<BuildableStructure, StructureInfo>> structureMap) {
    public static void encode(ClientBoundStructuresPacket packet, FriendlyByteBuf buf) {
        buf.writeMap(packet.structureMap(), FriendlyByteBuf::writeResourceLocation, (buf2, info) -> {
            buf2.writeWithCodec(BuildableStructure.CODEC, info.getLeft());
            buf2.writeWithCodec(StructureInfo.CODEC, info.getRight());
        });
    }

    public static ClientBoundStructuresPacket decode(FriendlyByteBuf buf) {
        return new ClientBoundStructuresPacket(buf.readMap(FriendlyByteBuf::readResourceLocation, buf2 -> Pair.of(buf2.readWithCodec(BuildableStructure.CODEC), buf2.readWithCodec(StructureInfo.CODEC))));
    }
}
