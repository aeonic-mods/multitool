package design.aeonic.multitool.api.multitool;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistryEntry;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Describes a setting for the multitool. Must be registered with {@link }
 */
public abstract class MultitoolBehavior extends ForgeRegistryEntry<MultitoolBehavior> {

    // BEHAVIOR + DATA

    /**
     * Checks whether the behavior should show in the selection screen. This is not a replacement for {@link #allowedForPlayer}!
     */
    public boolean showInSelectionScreen(LocalPlayer player, ItemStack multitool) {
        return true;
    }

    /**
     * Checks whether the given player can use this behavior. This is checked on both sides;
     * however, if the client returns false, the server won't receive a packet at all and the behavior will never be selected.
     */
    public boolean allowedForPlayer(Player player, ItemStack multitool) {
        return true;
    }

    /**
     * Called on both sides when the behavior is selected from the selection screen.
     * @param player the player
     * @param stack the multitool stack
     */
    public void onSelected(Player player, ItemStack stack) {}

    /**
     * Called on both sides when the behavior is selected from the selection screen.
     * @param player the player
     * @param stack the multitool stack
     */
    public void onDeselected(Player player, ItemStack stack) {}

    // INTERACTIONS

    /**
     * Called by the multitool item's own method when this behavior is active; return null to use its default implementation.
     */
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext ctx) { return null; }

    /**
     * Called by the multitool item's own method when this behavior is active; return null to use its default implementation.
     */
    public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pUsedHand) { return null; }

    /**
     * Called by the multitool item's own method when this behavior is active.
     */
    public void onUseTick(Level pLevel, LivingEntity pLivingEntity, ItemStack pStack, int pRemainingUseDuration) {}

    /**
     * Called by the multitool item's own method when this behavior is active.
     */
    public void releaseUsing(ItemStack pStack, Level pLevel, LivingEntity pLivingEntity, int pTimeCharged) {}

    /**
     * Called by the multitool item's own method when this behavior is active; return null to use its default implementation.
     */
    public InteractionResult useOn(UseOnContext pContext) { return null; }

    // MISC

    /**
     * Called by the multitool item's own method when this behavior is active.
     */
    public void inventoryTick(ItemStack pStack, Level pLevel, Entity pEntity, int pSlotId, boolean pIsSelected) {}

    // CLIENT

    /**
     * Gets the behavior's icon to render in the HUD and wherever relevant, in the form of an itemstack.
     */
    public abstract void renderHudIcon(PoseStack stack, int x, int y);

    /**
     * Gets the behavior's display name for GUIs.
     */
    public abstract Component getDisplayName();

    /**
     * Called by the multitool item's own method when this behavior is active.
     */
    public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltipComponents, TooltipFlag pIsAdvanced) {}
}
