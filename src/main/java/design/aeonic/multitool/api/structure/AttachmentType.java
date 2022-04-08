package design.aeonic.multitool.api.structure;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import design.aeonic.multitool.util.Codecs;
import net.minecraft.util.StringRepresentable;
import net.minecraftforge.common.IExtensibleEnum;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Describes the type of a structure attachment point.
 * @param type the type of IO for this attachment point
 * @param io the IO directions (input, output or both) for this attachment point
 */
@SuppressWarnings("deprecation")
public record AttachmentType(Type type, IO io) {
    public static Codec<AttachmentType> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Type.CODEC.fieldOf("type").forGetter(AttachmentType::type),
            IO.CODEC.fieldOf("io").forGetter(AttachmentType::io)
    ).apply(instance, AttachmentType::new));

    public static final AttachmentType POWER_LINE = new AttachmentType(Type.POWER, IO.BOTH);
    public static final AttachmentType ITEM_INPUT = new AttachmentType(Type.ITEM, IO.INPUT);
    public static final AttachmentType ITEM_OUTPUT = new AttachmentType(Type.ITEM, IO.OUTPUT);
    public static final AttachmentType ITEM = new AttachmentType(Type.ITEM, IO.BOTH);
    public static final AttachmentType FLUID_INPUT = new AttachmentType(Type.FLUID, IO.INPUT);
    public static final AttachmentType FLUID_OUTPUT = new AttachmentType(Type.FLUID, IO.OUTPUT);
    public static final AttachmentType FLUID = new AttachmentType(Type.FLUID, IO.BOTH);

    /**
     * Defines the type of interaction this connection point should have with other placeable things (for instance, conveyors).
     */
    public enum Type implements IExtensibleEnum, StringRepresentable {
        POWER,
        ITEM,
        FLUID;

        public static final Map<String, Type> BY_NAME = Arrays.stream(values()).collect(Collectors.toMap(Type::getSerializedName, (s) -> s));
        public static final Codec<Type> CODEC = Codecs.fromExtensibleEnum(Type::values, BY_NAME::get);

        @Override
        public void init() {
            BY_NAME.put(name(), this);
        }

        public static Type create(String name) {
            throw new IllegalStateException("You fucked up.");
        }

        @Nonnull
        @Override
        public String getSerializedName() {
            return name();
        }
    }

    /**
     * Defines whether an {@link AttachmentType} should be used for input, output, or both.
     */
    public enum IO implements StringRepresentable {
        INPUT,
        OUTPUT,
        BOTH,
        NONE; // Including for posterity but there is no reason you should need to use this

        public static final Map<String, IO> BY_NAME = Arrays.stream(values()).collect(Collectors.toMap(IO::getSerializedName, (s) -> s));
        public static final Codec<IO> CODEC = Codecs.fromEnum(IO::values, BY_NAME::get);

        @Nonnull
        @Override
        public String getSerializedName() {
            return name();
        }
    }
}