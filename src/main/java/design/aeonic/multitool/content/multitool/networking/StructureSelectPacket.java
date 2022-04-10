package design.aeonic.multitool.content.multitool.networking;

import design.aeonic.multitool.api.structure.StructureBuildingRecipe;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.InteractionHand;
import net.minecraftforge.server.ServerLifecycleHooks;

public record StructureSelectPacket(StructureBuildingRecipe recipe, InteractionHand hand) {
    public static void encode(StructureSelectPacket packet, FriendlyByteBuf buf) {
        buf.writeResourceLocation(packet.recipe().getId());
        buf.writeEnum(packet.hand());
    }
    public static StructureSelectPacket decode(FriendlyByteBuf buf) {
        var recipe = ServerLifecycleHooks.getCurrentServer().getRecipeManager().byKey(buf.readResourceLocation()).orElse(null);
        if (recipe instanceof StructureBuildingRecipe structureBuildingRecipe)
            return new StructureSelectPacket(structureBuildingRecipe, buf.readEnum(InteractionHand.class));
        return new StructureSelectPacket(null, buf.readEnum(InteractionHand.class));
    }
}
