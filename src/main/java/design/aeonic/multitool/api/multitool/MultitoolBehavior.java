package design.aeonic.multitool.api.multitool;

import design.aeonic.multitool.api.ui.ISelectable;
import design.aeonic.multitool.client.MultitoolSelectScreen;
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
public abstract class MultitoolBehavior extends ForgeRegistryEntry<MultitoolBehavior> implements ISelectable<MultitoolSelectScreen, MultitoolBehavior> {

    // BEHAVIOR DEFINITION

    @Override
    public final boolean showInSelectionScreen(LocalPlayer player, MultitoolSelectScreen screen) {
        return showInSelectionScreen(player, screen.hand, screen.multitool);
    }

    @Override
    public final boolean allowedForPlayer(Player player, MultitoolSelectScreen screen) {
        return allowedForPlayer(player, screen.hand, screen.multitool);
    }

    public boolean showInSelectionScreen(LocalPlayer player, InteractionHand hand, ItemStack multitool) {
        return true;
    }

    public boolean allowedForPlayer(Player player, InteractionHand hand, ItemStack multitool) {
        return true;
    }

    // CLIENT

    /**
     * Called by the multitool item's own method when this behavior is active.
     */
    public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltipComponents, TooltipFlag pIsAdvanced) {}

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
}
