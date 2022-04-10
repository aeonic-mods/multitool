package design.aeonic.multitool;

import design.aeonic.multitool.api.Constants;
import design.aeonic.multitool.api.Registries;
import design.aeonic.multitool.api.multitool.MultitoolBehavior;
import design.aeonic.multitool.data.StructureBuildingRecipe;
import design.aeonic.multitool.network.StructureSyncHandler;
import design.aeonic.multitool.client.ClientMultitool;
import design.aeonic.multitool.content.multitool.behaviors.DebugBehavior;
import design.aeonic.multitool.content.multitool.behaviors.EmptyBehavior;
import design.aeonic.multitool.content.multitool.behaviors.StructureBuildingBehavior;
import design.aeonic.multitool.network.MultitoolSyncHandler;
import design.aeonic.multitool.data.StructureSyncReloadListener;
import design.aeonic.multitool.registry.EMItems;
import design.aeonic.multitool.registry.EMRecipeTypes;
import design.aeonic.multitool.registry.EMRegistrate;
import design.aeonic.multitool.registry.EMSounds;
import design.aeonic.multitool.util.Locations;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.forge.event.lifecycle.GatherDataEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static design.aeonic.multitool.api.Constants.MOD_ID;

@Mod(MOD_ID)
@Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class EngineersMultitool {

    public static final EMRegistrate REGISTRATE = new EMRegistrate();
    public static final Logger LOGGER = LogManager.getLogger();

    public EngineersMultitool() {
        var bus = MinecraftForge.EVENT_BUS;

        // Registries
        EMItems.defer();
        EMSounds.defer();
        // Packets
        MultitoolSyncHandler.registerMessages();
        StructureSyncHandler.registerMessages();

        // Server / common
        bus.addListener(EngineersMultitool::addReloadListeners);
        bus.addListener(StructureSyncHandler::onPlayerLogin);
        // Client
        bus.addListener(ClientMultitool::renderLevelLast);
        bus.addListener(ClientMultitool::mouseScroll);
    }

    // Forge events

    public static void addReloadListeners(AddReloadListenerEvent event) {
        event.addListener(new StructureSyncReloadListener());
    }

    // Mod + registry events

    @SubscribeEvent
    public static void registerRecipeSerializers(RegistryEvent.Register<RecipeSerializer<?>> event) {
        event.getRegistry().register(StructureBuildingRecipe.SERIALIZER.setRegistryName(Locations.make("multitool_building")));
    }

    @SubscribeEvent
    public static void registerMultitoolBehaviors(RegistryEvent.Register<MultitoolBehavior> event) {
        event.getRegistry().registerAll(
                EmptyBehavior.INSTANCE.setRegistryName(EmptyBehavior.KEY),
                new DebugBehavior().setRegistryName(Locations.make("debug")),
                new StructureBuildingBehavior().setRegistryName(Locations.make("structure_building"))
        );
    }

    @SubscribeEvent
    public static void onNewRegistries(RegistryEvent.NewRegistry event) {
        Registries.create();
    }

    @SubscribeEvent
    public static void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            EMRecipeTypes.register();
        });
    }

    @SubscribeEvent
    public static void runData(GatherDataEvent event) {
        Constants.Translations.load();
//        TestData.genData();
    }
}
