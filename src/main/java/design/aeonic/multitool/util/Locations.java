package design.aeonic.multitool.util;

import design.aeonic.multitool.api.Constants;
import net.minecraft.resources.ResourceLocation;
import org.apache.commons.lang3.StringUtils;

public class Locations {
    public static ResourceLocation make(String... path) {
        return new ResourceLocation(Constants.MOD_ID, StringUtils.join(path, "/"));
    }
}
