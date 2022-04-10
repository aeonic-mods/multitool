package design.aeonic.multitool.api.structure;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import design.aeonic.multitool.api.Vectors;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;

import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

/**
 * Represents a structure with some extra data used for placement with the structure building mode
 */
public record BuildableStructure(String langKey, ResourceLocation structure, Vec3i origin, List<AttachmentPoint> attachmentPoints) {

    public static final Codec<BuildableStructure> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("langKey").forGetter(BuildableStructure::langKey),
            ResourceLocation.CODEC.fieldOf("structure").forGetter(BuildableStructure::structure),
            Vec3i.CODEC.fieldOf("origin").forGetter(BuildableStructure::origin),
            AttachmentPoint.CODEC.listOf().optionalFieldOf("attachmentPoints", Collections.emptyList()).forGetter(BuildableStructure::attachmentPoints)
    ).apply(instance, BuildableStructure::new));

    public BlockPos adjustForPlacement(Level level, BlockHitResult target) {
        if (!level.getBlockState(target.getBlockPos()).getMaterial().isReplaceable()) {
            return target.getBlockPos().relative(target.getDirection());
        }
        return target.getBlockPos();
    }

    public Vec3i origin(Direction direction) {
        return Vectors.rotateFromNorth(origin(), direction);
    }

    public PlacementState checkPlacement(Level level, BlockPos placementOrigin, StructureInfo info, Direction direction) {
        if (level.getBlockState(placementOrigin.offset(0, - origin().getY() - 1, 0)).isAir()) return PlacementState.INVALID;
        BlockPos pos = placementOrigin.offset(origin(direction).multiply(-1));
        // I don't remember why this works but it does so I'm going to leave it alone
        Vec3i size = Vectors.rotateFromNorth(info.size().offset(-1, 0, -1), direction);
        AABB checkBox = new AABB(0, 0, 0, size.getX(), size.getY(), size.getZ()).move(pos);
        Stream<BlockState> states = level.getBlockStatesIfLoaded(checkBox);
        if (states.filter(s -> !s.getMaterial().isReplaceable()).toList().size() > 0)
            return PlacementState.OCCLUDED;
        return PlacementState.OK;
    }

    /**
     * Describes the current state of a structure that's being built (or selected in the building tool) at a particular position
     */
    public enum PlacementState {
        /**
         * The structure can be placed here
         */
        OK,
        /**
         * The structure preview should render but it cannot be placed here as its space is occluded by blocks
         */
        OCCLUDED,
        /**
         * The structure preview should not render and it cannot be placed here
         */
        INVALID
    }
}
