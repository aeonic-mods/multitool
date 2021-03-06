package design.aeonic.multitool.content.multitool.behaviors;

import com.mojang.blaze3d.vertex.PoseStack;
import design.aeonic.multitool.api.Constants;
import design.aeonic.multitool.api.multitool.MultitoolBehavior;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class DebugBehavior extends MultitoolBehavior {

    @Override
    public boolean showInSelectionScreen(LocalPlayer player, InteractionHand hand, ItemStack multitool) {
        return allowedForPlayer(player, hand, multitool);
    }

    @Override
    public boolean allowedForPlayer(Player player, InteractionHand hand, ItemStack multitool) {
        return player.isCreative();
    }

    @Override
    public void drawHudIcon(PoseStack stack, int x, int y) {
        Minecraft.getInstance().getItemRenderer().renderGuiItem(new ItemStack(Items.COMMAND_BLOCK), x - 8, y - 8);
    }

    @Override
    public Component getDisplayName() {
        return Constants.Translations.MULTITOOL_DEBUG;
    }
}
