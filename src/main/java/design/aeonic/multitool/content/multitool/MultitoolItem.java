package design.aeonic.multitool.content.multitool;

import design.aeonic.multitool.api.Registries;
import design.aeonic.multitool.api.Styles;
import design.aeonic.multitool.api.multitool.MultitoolBehavior;
import design.aeonic.multitool.client.MultitoolSelectScreen;
import design.aeonic.multitool.content.multitool.behaviors.EmptyBehavior;
import design.aeonic.multitool.content.multitool.networking.BehaviorSelectPacket;
import design.aeonic.multitool.content.multitool.networking.MultitoolPacketHandler;
import design.aeonic.multitool.data.Translations;
import design.aeonic.multitool.registry.EMItems;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class MultitoolItem extends Item {
    public MultitoolItem(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pUsedHand) {
        var stack = pPlayer.getItemInHand(pUsedHand);
        if (pPlayer.isCrouching()) {
            if (!(pPlayer instanceof ServerPlayer)) {
                MultitoolSelectScreen.open(stack, pUsedHand);
            }
            pPlayer.awardStat(Stats.ITEM_USED.get(this));
            return InteractionResultHolder.sidedSuccess(stack, pLevel.isClientSide());
        }
        var ret = getSelectedBehavior(stack).use(pLevel, pPlayer, pUsedHand);
        if (ret != null) return ret;

        return super.use(pLevel, pPlayer, pUsedHand);
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        var ret = getSelectedBehavior(stack).onItemUseFirst(stack, context);
        if (ret != null) return ret;

        return super.onItemUseFirst(stack, context);
    }

    @Override
    public UseAnim getUseAnimation(ItemStack pStack) {
        return UseAnim.CROSSBOW;
    }

    /**
     * Called on both sides. The client sends a packet to the server; the server serializes NBT data which is later synced back automatically.
     */
    public void handleBehaviorSelectScreenClosed(Player player, InteractionHand hand, MultitoolBehavior behavior) {
        var stack = player.getItemInHand(hand);
        if (!stack.is(EMItems.MULTITOOL.get()) || !behavior.allowedForPlayer(player, hand, stack)) return;

        if (player instanceof ServerPlayer serverPlayer) {
            setSelectedBehavior(player, stack, behavior);
        } else {
            MultitoolPacketHandler.INSTANCE.sendToServer(new BehaviorSelectPacket(behavior, hand));
        }
    }

    public MultitoolBehavior getSelectedBehavior(ItemStack stack) {
        var behavior = Registries.MULTITOOL_BEHAVIORS.getValue(getSelectedBehaviorId(stack));
        if (behavior == null) return EmptyBehavior.INSTANCE;
        return behavior;
    }

    public ResourceLocation getSelectedBehaviorId(ItemStack stack) {
        String id = stack.getOrCreateTag().getString("Mode");
        return new ResourceLocation(id);
    }

    public void setSelectedBehavior(Player player, ItemStack stack, MultitoolBehavior behavior) {
        var oldBehavior = getSelectedBehavior(stack);
        if (!(oldBehavior == behavior)) oldBehavior.onDeselected(player, stack);
        var key = behavior.getRegistryName();
        stack.getOrCreateTag().putString("Mode", key == null ? EmptyBehavior.KEY.toString() : key.toString());
        behavior.onSelected(player, stack);
    }

    @Override
    public void onUseTick(Level pLevel, LivingEntity pLivingEntity, ItemStack pStack, int pRemainingUseDuration) {
        getSelectedBehavior(pStack).onUseTick(pLevel, pLivingEntity, pStack, pRemainingUseDuration);

        super.onUseTick(pLevel, pLivingEntity, pStack, pRemainingUseDuration);
    }

    @Override
    public void releaseUsing(ItemStack pStack, Level pLevel, LivingEntity pLivingEntity, int pTimeCharged) {
        getSelectedBehavior(pStack).releaseUsing(pStack, pLevel, pLivingEntity, pTimeCharged);

        super.releaseUsing(pStack, pLevel, pLivingEntity, pTimeCharged);
    }

    @Override
    public InteractionResult useOn(UseOnContext pContext) {
        ItemStack stack = pContext.getItemInHand();
        var ret = getSelectedBehavior(stack).useOn(pContext);
        if (ret != null) return ret;

        return super.useOn(pContext);
    }

    @Override
    public void inventoryTick(ItemStack pStack, Level pLevel, Entity pEntity, int pSlotId, boolean pIsSelected) {
        getSelectedBehavior(pStack).inventoryTick(pStack, pLevel, pEntity, pSlotId, pIsSelected);

        super.inventoryTick(pStack, pLevel, pEntity, pSlotId, pIsSelected);
    }

    @Override
    public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltipComponents, TooltipFlag pIsAdvanced) {
        pTooltipComponents.add(Translations.MULTITOOL_MODE.copy().withStyle(Styles.TOOLTIP_PLAIN).append(getSelectedBehavior(pStack).getDisplayName().copy().withStyle(Styles.TOOLTIP_SETTING)));

        getSelectedBehavior(pStack).appendHoverText(pStack, pLevel, pTooltipComponents, pIsAdvanced);

        super.appendHoverText(pStack, pLevel, pTooltipComponents, pIsAdvanced);
    }
}
