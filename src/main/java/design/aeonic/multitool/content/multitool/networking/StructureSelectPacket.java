package design.aeonic.multitool.content.multitool.networking;

import design.aeonic.multitool.api.structure.StructureBuildingRecipe;
import design.aeonic.multitool.util.Locations;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.InteractionHand;
import net.minecraftforge.server.ServerLifecycleHooks;

import javax.annotation.Nullable;

public record StructureSelectPacket(@Nullable  StructureBuildingRecipe recipe, Direction direction, InteractionHand hand) {
    public static void encode(StructureSelectPacket packet, FriendlyByteBuf buf) {
        buf.writeResourceLocation(packet.recipe() != null ? packet.recipe().getId() : Locations.NULL);
        buf.writeEnum(packet.direction());
        buf.writeEnum(packet.hand());
    }
    public static StructureSelectPacket decode(FriendlyByteBuf buf) {
        var key = buf.readResourceLocation();
        if (!key.equals(Locations.NULL)) {
            var recipe = ServerLifecycleHooks.getCurrentServer().getRecipeManager().byKey(key).orElse(null);
            if (recipe instanceof StructureBuildingRecipe structureBuildingRecipe)
                return new StructureSelectPacket(structureBuildingRecipe, buf.readEnum(Direction.class), buf.readEnum(InteractionHand.class));
        }
        return new StructureSelectPacket(null, buf.readEnum(Direction.class), buf.readEnum(InteractionHand.class));
    }
}
