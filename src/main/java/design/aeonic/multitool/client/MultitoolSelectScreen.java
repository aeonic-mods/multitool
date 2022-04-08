package design.aeonic.multitool.client;

import design.aeonic.multitool.api.Constants;
import design.aeonic.multitool.api.Registries;
import design.aeonic.multitool.api.multitool.MultitoolBehavior;
import design.aeonic.multitool.api.ui.RadialSelectScreen;
import design.aeonic.multitool.registry.EMItems;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

public class MultitoolSelectScreen extends RadialSelectScreen<MultitoolSelectScreen, MultitoolBehavior> {
    public final InteractionHand hand;
    public final ItemStack multitool;

    protected MultitoolSelectScreen(ItemStack multitool, InteractionHand hand) {
        super(Constants.Translations.MULTITOOL_SELECTION_SCREEN, Registries.MULTITOOL_BEHAVIORS.getValues());
        this.multitool = multitool;
        this.hand = hand;
    }

    /**
     * Called on the client only to open this selection screen.
     */
    public static void open(ItemStack multitool, InteractionHand hand) {
        Minecraft.getInstance().setScreen(new MultitoolSelectScreen(multitool, hand));
    }

    @Override
    public void onClose() {
        EMItems.MULTITOOL.get().handleBehaviorSelectScreenClosed(player, hand, options.get(selectedIndex));
        super.onClose();
    }
}
