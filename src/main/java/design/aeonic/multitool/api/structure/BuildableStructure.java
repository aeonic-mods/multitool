package design.aeonic.multitool.api.structure;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public record BuildableStructure(ResourceLocation structure, List<AttachmentPoint> attachmentPoints) {

    public static final Codec<BuildableStructure> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("structure").forGetter(BuildableStructure::structure),
            AttachmentPoint.CODEC.listOf().fieldOf("attachmentPoints").forGetter(BuildableStructure::attachmentPoints)
    ).apply(instance, BuildableStructure::new));

    enum PlacementState {
        OK,         // The structure can be placed here
        OCCLUDED,   // The structure preview should render but it cannot be placed here as its space is occluded by blocks
        INVALID     // The structure preview should not render and it cannot be placed here
    }
}
