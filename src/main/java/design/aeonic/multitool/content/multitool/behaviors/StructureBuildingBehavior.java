package design.aeonic.multitool.content.multitool.behaviors;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import design.aeonic.multitool.EngineersMultitool;
import design.aeonic.multitool.api.Constants;
import design.aeonic.multitool.api.client.Holograms;
import design.aeonic.multitool.api.multitool.MultitoolBehavior;
import design.aeonic.multitool.api.structure.BuildableStructure;
import design.aeonic.multitool.api.structure.StructureBuildingRecipe;
import design.aeonic.multitool.content.multitool.ClientMultitool;
import design.aeonic.multitool.content.multitool.networking.MultitoolSyncHandler;
import design.aeonic.multitool.content.multitool.networking.StructureSelectPacket;
import design.aeonic.multitool.registry.EMRecipeTypes;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RenderLevelLastEvent;
import net.minecraftforge.network.NetworkDirection;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.logging.LogManager;

public class StructureBuildingBehavior extends MultitoolBehavior {

    public static BuildableStructure selectedStructure;

    private static Vector3f oldPos = Vector3f.ZERO;

    // TODO: Move selection to its own method, then sync it in onSelected

    @Override
    public void renderLevelLast(RenderLevelLastEvent event, InteractionHand hand, ItemStack stack) {
        Minecraft minecraft = Minecraft.getInstance();
        BlockHitResult target = (BlockHitResult) Objects.requireNonNull(minecraft.player).pick(Holograms.getMaxPlacementDistance(), event.getPartialTick(), true);

        BuildableStructure struct = selectedStructure;
        if (struct == null) return;
        BlockPos pos = struct.adjustForPlacement(Objects.requireNonNull(minecraft.level), target);
        Vector3f newPos = new Vector3f(pos.getX(), pos.getY(), pos.getZ());
        oldPos.lerp(newPos, .25f);
        Holograms.drawStructureHologram(minecraft, event.getPoseStack(), event.getPartialTick(), pos, oldPos, Direction.NORTH, struct, !minecraft.options.hideGui);
    }

    @Override
    public void mouseScroll(InputEvent.MouseScrollEvent event, InteractionHand hand, ItemStack stack) {
        Minecraft minecraft = Minecraft.getInstance();
        Player player = Objects.requireNonNull(minecraft.player);

        if (player.isShiftKeyDown() && event.getScrollDelta() != 0) {
            event.setCanceled(true);
            ResourceLocation selected =  new ResourceLocation(stack.getOrCreateTagElement("Behaviors").getCompound("StructureBuilding").getString("Selected"));
            int shift = event.getScrollDelta() > 0 ? 1 : -1;

            List<StructureBuildingRecipe> recipes = ClientMultitool.getClientStructureBuildingRecipes();
            List<ResourceLocation> locations = recipes.stream().map(Recipe::getId).toList();
            StructureBuildingRecipe recipe = recipes.get(Math.floorMod((locations.indexOf(selected) + shift), recipes.size()));
            EngineersMultitool.LOGGER.info("{}", locations);
            EngineersMultitool.LOGGER.info("#1 {} {} {} {} {}", selected, shift, recipe, locations.indexOf(selected), recipes.indexOf(recipe));
            if (selectedStructure == recipe.structure()) return;
            EngineersMultitool.LOGGER.info("#2 {} {} {} {} {}", selected, shift, recipe, locations.indexOf(selected), recipes.indexOf(recipe));

            selectedStructure = recipe.structure();
            player.displayClientMessage(new TranslatableComponent(selectedStructure.langKey()).withStyle(Constants.Styles.ACTIONBAR_INFO), true);
            MultitoolSyncHandler.INSTANCE.sendTo(new StructureSelectPacket(recipe, hand), Objects.requireNonNull(minecraft.getConnection()).getConnection(), NetworkDirection.PLAY_TO_SERVER);
        }
    }

    public void handleStructureSelect(ServerPlayer player, InteractionHand hand, StructureBuildingRecipe recipe) {
        selectedStructure = recipe.structure();
        ItemStack stack = player.getItemInHand(hand);
        var tag = stack.getOrCreateTagElement("Behaviors").getCompound("StructureBuilding");
        tag.putString("Selected", recipe.getId().toString());
        stack.getOrCreateTagElement("Behaviors").put("StructureBuilding", tag);
        EngineersMultitool.LOGGER.info("1 {}, 2 {}, 3 {}, 4 {}", recipe.getId().toString(), recipe, selectedStructure, stack.getOrCreateTagElement("Behaviors").getCompound("StructureBuilding").getString("Selected"));
    }

    @Override
    public Component getDisplayName() {
        return Constants.Translations.MULTITOOL_STRUCTURE_BUILDING;
    }

    @Override
    public void drawHudIcon(PoseStack stack, int x, int y) {
        Minecraft.getInstance().getItemRenderer().renderGuiItem(new ItemStack(Items.BRICKS), x - 8, y - 8);
    }

//    public static List<StructureBuildingRecipe> getStructureBuildingRecipes(ServerPlayer player) {
//        MinecraftServer server = player.getServer();
//        if (server != null)
//            return server.getRecipeManager().getAllRecipesFor(EMRecipeTypes.MULTITOOL_BUILDING.get());
//        return Collections.emptyList();
//    }

    public static class Client {
        public static List<StructureBuildingRecipe> getStructureBuildingRecipes(LocalPlayer player) {
            return player.connection.getRecipeManager().getAllRecipesFor(EMRecipeTypes.MULTITOOL_BUILDING.get());
        }
    }
}
