package design.aeonic.multitool.api;

import design.aeonic.multitool.api.multitool.MultitoolBehavior;
import design.aeonic.multitool.util.Locations;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;

public class Registries {
    public static final IForgeRegistry<MultitoolBehavior> MULTITOOL_BEHAVIORS = new RegistryBuilder<MultitoolBehavior>()
            .setName(Locations.make("multitool_behaviors"))
            .setType(MultitoolBehavior.class).create();

    public static void create() {}
}
