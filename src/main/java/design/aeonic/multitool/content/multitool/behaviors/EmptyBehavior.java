package design.aeonic.multitool.content.multitool.behaviors;

import com.mojang.blaze3d.vertex.PoseStack;
import design.aeonic.multitool.api.Constants;
import design.aeonic.multitool.api.multitool.MultitoolBehavior;
import design.aeonic.multitool.util.Locations;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * An empty behavior that can be used by players to disable all functionality temporarily and as a fallback when registry fetching fails.
 * Also the only behavior (by default) that cannot be disabled or locked behind advancements etc.
 */
public class EmptyBehavior extends MultitoolBehavior {
    public static final EmptyBehavior INSTANCE = new EmptyBehavior();
    public static final ResourceLocation KEY = Locations.make("empty");

    @Override
    public void drawHudIcon(PoseStack stack, int x, int y) {
        Minecraft.getInstance().getItemRenderer().renderGuiItem(new ItemStack(Items.BARRIER), x - 8, y - 8);
    }

    @Override
    public Component getDisplayName() {
        return Constants.Translations.MULTITOOL_EMPTY;
    }
}
