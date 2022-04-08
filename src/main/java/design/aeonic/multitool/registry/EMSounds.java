package design.aeonic.multitool.registry;

import design.aeonic.multitool.api.Constants;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class EMSounds {
    public static DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, Constants.MOD_ID);

    public static final RegistryObject<SoundEvent> MULTITOOL_OPEN = create("gui.multitool.multitool.open");
    public static final RegistryObject<SoundEvent> MULTITOOL_SELECT = create("gui.multitool.multitool.select");
    public static final RegistryObject<SoundEvent> MULTITOOL_CLOSE = create("gui.multitool.multitool.close");

    private static RegistryObject<SoundEvent> create(String name) {
        return SOUNDS.register(name, () -> new SoundEvent(new ResourceLocation(Constants.MOD_ID, name)));
    }

    public static void defer() {
        SOUNDS.register(FMLJavaModLoadingContext.get().getModEventBus());
    }
}
