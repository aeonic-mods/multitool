package design.aeonic.multitool.api.structure;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;

/**
 * Describes an attachment point on a multiblock.
 * The face and relative position are rotated with the structure; initial values assume the structure is facing north.
 * By default the multitool doesn't use these attachment points; Immersive Manufacturing implements them and other mods are free to make use of them similarly.
 * @param type        the type of this attachment
 * @param face        the face this attachment is exposed on (when the structure is facing north)
 * @param relativePos the position of this attachment relative to the structure's 0,0,0 (again, when the structure is facing north)
 */
public record AttachmentPoint(AttachmentType type, Direction face, Vec3i relativePos) {
    public static Codec<AttachmentPoint> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            AttachmentType.CODEC.fieldOf("type").forGetter(AttachmentPoint::type),
            Direction.CODEC.fieldOf("face").forGetter(AttachmentPoint::face),
            Vec3i.CODEC.fieldOf("relativePos").forGetter(AttachmentPoint::relativePos)
    ).apply(instance, AttachmentPoint::new));
}
