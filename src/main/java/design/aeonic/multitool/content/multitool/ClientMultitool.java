package design.aeonic.multitool.content.multitool;

import design.aeonic.multitool.api.structure.StructureBuildingRecipe;
import design.aeonic.multitool.registry.EMItems;
import design.aeonic.multitool.registry.EMRecipeTypes;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RenderLevelLastEvent;

import java.util.List;
import java.util.Objects;

public class ClientMultitool {

    public static List<StructureBuildingRecipe> getClientStructureBuildingRecipes() {
        LocalPlayer player = Objects.requireNonNull(Minecraft.getInstance().player);
        return player.connection.getRecipeManager().getAllRecipesFor(EMRecipeTypes.MULTITOOL_BUILDING.get());
    }

    public static void renderLevelLast(RenderLevelLastEvent event) {
        Minecraft minecraft = Objects.requireNonNull(Minecraft.getInstance());
        LocalPlayer player = Objects.requireNonNull(minecraft.player);
        for (InteractionHand hand: InteractionHand.values()) {
            ItemStack stack = player.getItemInHand(hand);
            if (stack.is(EMItems.MULTITOOL.get())) {
                EMItems.MULTITOOL.get().getSelectedBehavior(stack).renderLevelLast(event, hand, stack);
                return;
            }
        }
    }

    public static void mouseScroll(InputEvent.MouseScrollEvent event) {
        Minecraft minecraft = Objects.requireNonNull(Minecraft.getInstance());
        LocalPlayer player = Objects.requireNonNull(minecraft.player);
        for (InteractionHand hand: InteractionHand.values()) {
            ItemStack stack = player.getItemInHand(hand);
            if (stack.is(EMItems.MULTITOOL.get())) {
                EMItems.MULTITOOL.get().getSelectedBehavior(stack).mouseScroll(event, hand, stack);
                return;
            }
        }
    }
}
