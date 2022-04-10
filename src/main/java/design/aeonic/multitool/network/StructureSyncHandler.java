package design.aeonic.multitool.network;

import design.aeonic.multitool.api.structure.StructureInfo;
import design.aeonic.multitool.api.structure.Structures;
import design.aeonic.multitool.data.StructureBuildingRecipe;
import design.aeonic.multitool.mixin.access.StructureTemplateAccess;
import design.aeonic.multitool.registry.EMRecipeTypes;
import design.aeonic.multitool.util.Locations;
import net.minecraft.ResourceLocationException;
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
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

/**
 * Includes utilities for syncing structures to the client.
 */
public class StructureSyncHandler {
    private static final String PROTOCOL_VERSION = "StructureSyncHandler_v0.1";
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            Locations.make("structures"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals);

    public static void registerMessages() {
        int id = -1;
        INSTANCE.registerMessage(++id, ClientBoundStructuresPacket.class, ClientBoundStructuresPacket::encode, ClientBoundStructuresPacket::decode, (packet, ctx) -> {
            Structures.setStructureMap(packet.structureMap());
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

        Structures.clear();
        recipes.getAllRecipesFor(EMRecipeTypes.MULTITOOL_BUILDING.get()).forEach(recipe -> {
            try {
                ResourceLocation location = recipe.structure().structure();
                structures.get(location).ifPresent(template -> Structures.putStructure(recipe.getId(), Pair.of(recipe.structure(),
                        new StructureInfo(template.getSize(), ((StructureTemplateAccess) template).getPalettes().get(0).blocks().stream().map(StructureInfo.PosAndState::fromStructureBlockInfo).toList()))));
            } catch (ResourceLocationException ignored) {}
        });


        ClientBoundStructuresPacket packet = new ClientBoundStructuresPacket(Structures.getStructureMap());
        if (players == null)
            server.getPlayerList().getPlayers().forEach(player -> StructureSyncHandler.INSTANCE.sendTo(packet, player.connection.getConnection(), NetworkDirection.PLAY_TO_CLIENT));
        else
            players.forEach(player -> StructureSyncHandler.INSTANCE.sendTo(packet, player.connection.getConnection(), NetworkDirection.PLAY_TO_CLIENT));
    }

}
