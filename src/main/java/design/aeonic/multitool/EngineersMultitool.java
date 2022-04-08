package design.aeonic.multitool;

import design.aeonic.multitool.api.Registries;
import design.aeonic.multitool.api.multitool.MultitoolBehavior;
import design.aeonic.multitool.content.multitool.behaviors.DebugBehavior;
import design.aeonic.multitool.content.multitool.behaviors.EmptyBehavior;
import design.aeonic.multitool.content.multitool.networking.MultitoolPacketHandler;
import design.aeonic.multitool.data.Translations;
import design.aeonic.multitool.data.recipes.MultitoolBuildingRecipe;
import design.aeonic.multitool.registry.EMItems;
import design.aeonic.multitool.registry.EMRecipeTypes;
import design.aeonic.multitool.registry.EMRegistrate;
import design.aeonic.multitool.registry.EMSounds;
import design.aeonic.multitool.util.Locations;
import net.minecraft.world.item.crafting.RecipeSerializer;
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
        EMItems.defer();
        EMSounds.defer();
        MultitoolPacketHandler.registerMessages();
    }

    @SubscribeEvent
    public static void registerMultitoolBehaviors(RegistryEvent.Register<MultitoolBehavior> event) {
        event.getRegistry().registerAll(
                EmptyBehavior.INSTANCE.setRegistryName(EmptyBehavior.KEY),
                new DebugBehavior().setRegistryName(Locations.make("debug"))
        );
    }

    @SubscribeEvent
    public static void registerRecipeSerializers(RegistryEvent.Register<RecipeSerializer<?>> event) {
        event.getRegistry().register(MultitoolBuildingRecipe.SERIALIZER.setRegistryName(Locations.make("multitool_building")));
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
        Translations.load();
    }
}
