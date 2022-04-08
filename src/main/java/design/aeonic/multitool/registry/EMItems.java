package design.aeonic.multitool.registry;

import com.tterrag.registrate.util.entry.ItemEntry;
import design.aeonic.multitool.content.multitool.MultitoolItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Rarity;

import static design.aeonic.multitool.EngineersMultitool.REGISTRATE;

public class EMItems {

    public static final ItemEntry<MultitoolItem> MULTITOOL = REGISTRATE.item("multitool", MultitoolItem::new)
            .properties(p -> p.stacksTo(1).fireResistant().rarity(Rarity.EPIC))
            .tab(() -> CreativeModeTab.TAB_TOOLS)
            .lang("Engineer's Multitool")
            .model((ctx, prv) -> {}) // Model already exists in non-generated assets
            .register();

    public static void defer() {
        // Noop - Registrate handles our registration here
    }
}
