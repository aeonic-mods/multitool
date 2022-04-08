package design.aeonic.multitool.api.ui;

import com.mojang.blaze3d.vertex.PoseStack;
import design.aeonic.multitool.api.ui.client.RadialSelectScreen;
import design.aeonic.multitool.client.MultitoolSelectScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;

/**
 * Describes a selectable object in a radial selection screen.
 * @param <T> the screen class this object will be used in.
 */
public interface ISelectable<T extends RadialSelectScreen<T, S>, S extends ISelectable<T, S>> {

    /**
     * Renders the behavior's icon in the select screen. The space you have here is dependent on client config.<br /><br />
     * The passed x & y values are at the center of where you should draw your icon; see internal behaviors for how to use this:
     * {@link design.aeonic.multitool.content.multitool.behaviors.EmptyBehavior}
     */
    void drawHudIcon(PoseStack stack, int x, int y);

    /**
     * Gets the options's display name to render in UIs.
     * If the return value is {@link TextComponent#EMPTY}, text will not be rendered and the icon won't be offset to make space.
     */
    default Component getDisplayName() {
        return TextComponent.EMPTY;
    }

    /**
     * Checks whether to show this option in the given selection screen.
     * This <b>is not</b> a replacement for {@link #allowedForPlayer}.
     */
    default boolean showInSelectionScreen(LocalPlayer player, T screen) { return true; }

    /**
     * Checks whether the given player can use this behavior. This should be checked on both sides.
     * However, if the client returns false, the server won't receive a packet at all and the behavior will never be selected on the server.
     * Thus, the check  *should* be equivalent on both sides, but it's not strictly necessary.<br /><br />
     * The usage of this method, outside of changing display inside the client UI, is up to you.
     * For an example implementation, see {@link MultitoolSelectScreen#onClose()}.
     */
    default boolean allowedForPlayer(Player player, T screen) {
        return true;
    }

    /**
     * Called on both sides when this object is selected from a selection screen.
     * @param player the player
     * @param stack the multitool stack
     */
    default void onSelected(Player player, ItemStack stack) {}

    /**
     * Called on both sides when this object is deselected (that is, a different option has been switched to).
     * @param player the player
     * @param stack the multitool stack
     */
    default void onDeselected(Player player, ItemStack stack) {}
}
