package design.aeonic.multitool.data;

import design.aeonic.multitool.network.StructureSyncHandler;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class StructureSyncReloadListener implements ResourceManagerReloadListener {
    @Override
    public void onResourceManagerReload(ResourceManager pResourceManager) {
        StructureSyncHandler.syncStructureBlocksToPlayers(null);
    }
}
