package design.aeonic.multitool.api.structure;

import net.minecraft.resources.ResourceLocation;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public class Structures {
    // Keys are in the form of the *key* ID, not the structure.
    private static Map<ResourceLocation, Pair<BuildableStructure, StructureInfo>> STRUCTURE_MAP = new HashMap<>();

    public static void putStructure(ResourceLocation key, Pair<BuildableStructure, StructureInfo> structure) {
        getStructureMap().put(key, structure);
    }

    public static void clear() {
        STRUCTURE_MAP = new HashMap<>();
    }

    public static @Nullable Pair<BuildableStructure, StructureInfo> getStructure(ResourceLocation key) {
        return getStructureMap().get(key);
    }

    public static Map<ResourceLocation, Pair<BuildableStructure, StructureInfo>> getStructureMap() {
        return STRUCTURE_MAP;
    }

    /**
     * To be used only for a freshly synced structure map - erases the whole map and replaces with the passed one.
     */
    public static void setStructureMap(Map<ResourceLocation, Pair<BuildableStructure, StructureInfo>> structureMap) {
        STRUCTURE_MAP = structureMap;
    }
}
