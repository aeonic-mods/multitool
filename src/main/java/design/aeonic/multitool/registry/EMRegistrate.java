package design.aeonic.multitool.registry;

import design.aeonic.multitool.api.Constants;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

public class EMRegistrate extends com.tterrag.registrate.Registrate {
    public EMRegistrate() {
        super(Constants.MOD_ID);
        registerEventListeners(FMLJavaModLoadingContext.get().getModEventBus());
    }
}
