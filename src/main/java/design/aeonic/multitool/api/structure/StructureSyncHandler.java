package design.aeonic.multitool.api.structure;

import design.aeonic.multitool.mixin.access.StructureTemplateAccess;
import design.aeonic.multitool.registry.EMRecipeTypes;
import design.aeonic.multitool.util.Locations;
import net.minecraft.ResourceLocationException;
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

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Includes utilities for syncing structures to the client.
 */
public class StructureSyncHandler {
    private static Map<ResourceLocation, StructureInfo> STRUCTURE_INFO_MAP;

    public static Map<ResourceLocation, StructureInfo> getStructureInfoMap() {
        return STRUCTURE_INFO_MAP == null ? Map.of() : STRUCTURE_INFO_MAP;
    }

    private static final String PROTOCOL_VERSION = "StructureSyncHandler_v0.1";
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            Locations.make("structures"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals);

    public static void registerMessages() {
        int id = -1;
        INSTANCE.registerMessage(++id, StructureInfoPacket.class, StructureInfoPacket::encode, StructureInfoPacket::decode, (packet, ctx) -> {
//            EngineersMultitool.LOGGER.info(packet.infoMap());
            STRUCTURE_INFO_MAP = packet.infoMap();
            ctx.get().setPacketHandled(true);
        });
    }

    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getPlayer() instanceof ServerPlayer player)
            syncStructureBlocksToPlayers(List.of(player));
    }

    /**
     * Called only on the server; syncs the blocks within structures in {@link StructureBuildingRecipe}s to clients.
     * If the passed list is null, syncs to all connected clients.
     */
    public static void syncStructureBlocksToPlayers(@Nullable List<ServerPlayer> players) {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) return;
        RecipeManager recipes = server.getRecipeManager();
        StructureManager structures = server.getStructureManager();

        Map<ResourceLocation, StructureInfo> structureInfo = new HashMap<>();
        recipes.getAllRecipesFor(EMRecipeTypes.MULTITOOL_BUILDING.get()).forEach(recipe -> {
            try {
                ResourceLocation location = recipe.structure().structure();
                structures.get(location).ifPresent(template -> structureInfo.put(location, new StructureInfo(template.getSize(),
                        ((StructureTemplateAccess) template).getPalettes().get(0).blocks().stream().map(StructureInfo.PosAndState::fromStructureBlockInfo).toList())));
            } catch (ResourceLocationException ignored) {}
        });

        STRUCTURE_INFO_MAP = structureInfo;
        StructureInfoPacket packet = new StructureInfoPacket(structureInfo);
        if (players == null)
            server.getPlayerList().getPlayers().forEach(player -> StructureSyncHandler.INSTANCE.sendTo(packet, player.connection.getConnection(), NetworkDirection.PLAY_TO_CLIENT));
        else
            players.forEach(player -> StructureSyncHandler.INSTANCE.sendTo(packet, player.connection.getConnection(), NetworkDirection.PLAY_TO_CLIENT));
    }

    public record StructureInfoPacket(Map<ResourceLocation, StructureInfo> infoMap) {
        public static void encode(StructureInfoPacket packet, FriendlyByteBuf buf) {
            buf.writeMap(packet.infoMap(), FriendlyByteBuf::writeResourceLocation, (buf2, info) -> buf2.writeWithCodec(StructureInfo.CODEC, info));
        }

        public static StructureInfoPacket decode(FriendlyByteBuf buf) {
            return new StructureInfoPacket(buf.readMap(FriendlyByteBuf::readResourceLocation, buf2 -> buf2.readWithCodec(StructureInfo.CODEC)));
        }
    }
}
