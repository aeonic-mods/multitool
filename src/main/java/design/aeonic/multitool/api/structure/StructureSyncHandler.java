package design.aeonic.multitool.api.structure;

import design.aeonic.multitool.registry.EMRecipeTypes;
import design.aeonic.multitool.util.Locations;
import net.minecraft.ResourceLocationException;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.util.HashMap;
import java.util.Map;

/**
 * Includes utilities for syncing structures to the client.
 * The current implementation only syncs structure size to render a box preview.
 */
public class StructureSyncHandler {
    private static Map<ResourceLocation, Vec3i> CLIENT_STRUCTURE_SIZE_MAP;

    public static Map<ResourceLocation, Vec3i> getClientStructureSizeMap() {
        return CLIENT_STRUCTURE_SIZE_MAP == null ? Map.of() : CLIENT_STRUCTURE_SIZE_MAP;
    }

    private static final String PROTOCOL_VERSION = "StructureSyncHandler_v0.1";
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            Locations.make("structures"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals);

    public static void registerMessages() {
        int id = -1;
        INSTANCE.registerMessage(++id, StructureSizesPacket.class, StructureSizesPacket::encode, StructureSizesPacket::decode, (packet, ctx) -> {
            CLIENT_STRUCTURE_SIZE_MAP = packet.sizeMap();
            ctx.get().setPacketHandled(true);
        });
    }

    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getPlayer() instanceof ServerPlayer)
            syncStructureSizesToConnectedPlayers();
    }

    /**
     * Syncs the sizes of all structures in {@link design.aeonic.multitool.api.data.MultitoolBuildingRecipe}s to the client.
     */
    public static void syncStructureSizesToConnectedPlayers() {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) return;
        RecipeManager recipes = server.getRecipeManager();
        StructureManager structures = server.getStructureManager();

        Map<ResourceLocation, Vec3i> structureSizes = new HashMap<>();
        recipes.getAllRecipesFor(EMRecipeTypes.MULTITOOL_BUILDING.get()).forEach(recipe -> {
            try {
                ResourceLocation location = recipe.structure().structure();
                structures.get(location).ifPresent(template -> {
                    structureSizes.put(location, template.getSize());
                });
            } catch (ResourceLocationException ignored) {}
        });

        StructureSizesPacket packet = new StructureSizesPacket(structureSizes);
        server.getPlayerList().getPlayers().forEach(player -> StructureSyncHandler.INSTANCE.sendTo(packet, player.connection.getConnection(), NetworkDirection.PLAY_TO_CLIENT));
    }

    public record StructureSizesPacket(Map<ResourceLocation, Vec3i> sizeMap) {
        public static void encode(StructureSizesPacket packet, FriendlyByteBuf buf) {
            buf.writeMap(packet.sizeMap(), FriendlyByteBuf::writeResourceLocation, (buf2, vec) -> buf2.writeBlockPos(new BlockPos(vec.getX(), vec.getY(), vec.getZ())));
        }

        public static StructureSizesPacket decode(FriendlyByteBuf buf) {
            return new StructureSizesPacket(buf.readMap(FriendlyByteBuf::readResourceLocation, FriendlyByteBuf::readBlockPos));
        }
    }
}
