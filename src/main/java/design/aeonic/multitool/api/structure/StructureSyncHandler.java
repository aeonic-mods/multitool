package design.aeonic.multitool.api.structure;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import design.aeonic.multitool.EngineersMultitool;
import design.aeonic.multitool.api.Vectors;
import design.aeonic.multitool.mixin.access.StructureTemplateAccess;
import design.aeonic.multitool.registry.EMRecipeTypes;
import design.aeonic.multitool.util.Locations;
import net.minecraft.ResourceLocationException;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
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
                        ((StructureTemplateAccess) template).getPalettes().get(0).blocks().stream().map(PosAndState::fromStructureBlockInfo).toList())));
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

    public record StructureInfo(Vec3i size, List<PosAndState> blockInfo) {
        public static final Codec<StructureInfo> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Vec3i.CODEC.fieldOf("size").forGetter(StructureInfo::size),
                PosAndState.CODEC.listOf().fieldOf("blockInfo").forGetter(StructureInfo::blockInfo)).apply(instance, StructureInfo::new));

        public StructureInfo withDirection(Direction direction) {
            if (direction == Direction.NORTH || direction == Direction.UP || direction == Direction.DOWN) return this;
            return new StructureInfo(Vectors.rotateFromNorth(size(), direction), blockInfo().stream().map(i ->
                    new PosAndState(new BlockPos(Vectors.rotateFromNorth(i.pos(), direction)), i.state())).toList());
        }
    }

    /**
     * StructureBlockInfo without the NBT and with a codec.
     */
    public record PosAndState(BlockPos pos, BlockState state) {
        public static final Codec<PosAndState> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                BlockPos.CODEC.fieldOf("pos").forGetter(PosAndState::pos),
                BlockState.CODEC.fieldOf("state").forGetter(PosAndState::state)).apply(instance, PosAndState::new));

        public static PosAndState fromStructureBlockInfo(StructureTemplate.StructureBlockInfo info) {
            return new PosAndState(info.pos, info.state);
        }
    }

//    /**
//     * Called only on the server; syncs the sizes of all structures in {@link StructureBuildingRecipe}s to the client.
//     */
//    public static void syncStructureSizesToConnectedPlayers() {
//        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
//        if (server == null) return;
//        RecipeManager recipes = server.getRecipeManager();
//        StructureManager structures = server.getStructureManager();
//
//        Map<ResourceLocation, Vec3i> structureSizes = new HashMap<>();
//        recipes.getAllRecipesFor(EMRecipeTypes.MULTITOOL_BUILDING.get()).forEach(recipe -> {
//            try {
//                ResourceLocation location = recipe.structure().structure();
//                structures.get(location).ifPresent(template -> structureSizes.put(location, template.getSize()));
//            } catch (ResourceLocationException ignored) {}
//        });
//
//        STRUCTURE_SIZE_MAP = structureSizes;
//        StructureSizesPacket packet = new StructureSizesPacket(structureSizes);
//        server.getPlayerList().getPlayers().forEach(player -> StructureSyncHandler.INSTANCE.sendTo(packet, player.connection.getConnection(), NetworkDirection.PLAY_TO_CLIENT));
//    }

//    public record StructureSizesPacket(Map<ResourceLocation, Vec3i> infoMap) {
//        public static void encode(StructureSizesPacket packet, FriendlyByteBuf buf) {
//            buf.writeMap(packet.infoMap(), FriendlyByteBuf::writeResourceLocation, (buf2, vec) -> buf2.writeBlockPos(new BlockPos(vec.getX(), vec.getY(), vec.getZ())));
//        }
//
//        public static StructureSizesPacket decode(FriendlyByteBuf buf) {
//            return new StructureSizesPacket(buf.readMap(FriendlyByteBuf::readResourceLocation, FriendlyByteBuf::readBlockPos));
//        }
//    }
}
