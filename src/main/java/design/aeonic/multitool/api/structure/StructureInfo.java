package design.aeonic.multitool.api.structure;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import design.aeonic.multitool.api.Vectors;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

import java.util.List;

public record StructureInfo(Vec3i size, List<PosAndState> blockInfo) {
    public static final Codec<StructureInfo> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Vec3i.CODEC.fieldOf("size").forGetter(StructureInfo::size),
            PosAndState.CODEC.listOf().fieldOf("blockInfo").forGetter(StructureInfo::blockInfo)).apply(instance, StructureInfo::new));

    @SuppressWarnings("deprecation")
    public StructureInfo withDirection(Direction direction) {
        if (direction == Direction.NORTH || direction == Direction.UP || direction == Direction.DOWN) return this;
        Rotation rot = switch (direction) {
            case EAST   -> Rotation.CLOCKWISE_90;
            case SOUTH  -> Rotation.CLOCKWISE_180;
            case WEST   -> Rotation.COUNTERCLOCKWISE_90;
            default     -> Rotation.NONE;
        };
        return new StructureInfo(Vectors.rotateFromNorth(size(), direction), blockInfo().stream().map(i ->
                new PosAndState(new BlockPos(Vectors.rotateFromNorth(i.pos(), direction)), i.state().rotate(rot))).toList());
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
}
