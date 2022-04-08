package design.aeonic.multitool.util;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.StringRepresentable;

import java.util.function.Function;
import java.util.function.Supplier;

/**
 * ATs weren't working after rebuilding some seven times so we're duplicating code here.
 * We'll fix it eventually.
 * @see StringRepresentable#fromEnum(Supplier, Function)
 * @see net.minecraftforge.common.IExtensibleEnum#createCodecForExtensibleEnum(Supplier, Function) 
 */
public class Codecs {
    public static <E extends Enum<E> & StringRepresentable> Codec<E> fromEnum(Supplier<E[]> pElementSupplier, Function<String, E> pNamingFunction) {
        E[] ae = pElementSupplier.get();
        return ExtraCodecs.orCompressed(ExtraCodecs.stringResolverCodec(StringRepresentable::getSerializedName, pNamingFunction),
                ExtraCodecs.idResolverCodec(Enum::ordinal, ($) -> $ >= 0 && $ < ae.length ? ae[$] : null, -1));
    }

    public static <E extends Enum<E> & StringRepresentable> Codec<E> fromExtensibleEnum(Supplier<E[]> valuesSupplier, Function<? super String, ? extends E> enumValueFromNameFunction) {
        return Codec.either(Codec.STRING, Codec.INT).comapFlatMap(either -> either.map(str -> {
                var val = enumValueFromNameFunction.apply(str);
                return val != null ? DataResult.success(val) : DataResult.error("Unknown enum value name: " + str);
            }, num -> {
                var values = valuesSupplier.get();
                return num >= 0 && num < values.length ? DataResult.success(values[num]) : DataResult.error("Unknown enum id: " + num);
        }), value -> Either.left(value.getSerializedName()));
    }
}
