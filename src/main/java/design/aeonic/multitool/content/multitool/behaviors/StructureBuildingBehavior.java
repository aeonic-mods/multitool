package design.aeonic.multitool.content.multitool.behaviors;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import design.aeonic.multitool.api.Constants;
import design.aeonic.multitool.api.client.Holograms;
import design.aeonic.multitool.api.multitool.MultitoolBehavior;
import design.aeonic.multitool.api.structure.BuildableStructure;
import design.aeonic.multitool.api.structure.StructureInfo;
import design.aeonic.multitool.api.structure.Structures;
import design.aeonic.multitool.data.StructureBuildingRecipe;
import design.aeonic.multitool.client.ClientMultitool;
import design.aeonic.multitool.network.MultitoolSyncHandler;
import design.aeonic.multitool.network.ServerBoundStructureSelectPacket;
import design.aeonic.multitool.registry.EMRecipeTypes;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RenderLevelLastEvent;
import net.minecraftforge.network.NetworkDirection;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;

public class StructureBuildingBehavior extends MultitoolBehavior {

    public static Pair<BuildableStructure, StructureInfo> selectedStructure;
    public static Direction selectedDirection = Direction.NORTH;

    private static Vector3f oldPos = Vector3f.ZERO;

    @Override
    public void onSelected(Player player, InteractionHand hand, ItemStack stack) {
        if (!(player instanceof ServerPlayer))
            syncSelectedStructure(player, hand, stack);
    }

    @Override
    public void renderLevelLast(RenderLevelLastEvent event, InteractionHand hand, ItemStack stack) {
        Minecraft minecraft = Minecraft.getInstance();
        BlockHitResult target = (BlockHitResult) Objects.requireNonNull(minecraft.player).pick(Holograms.getMaxPlacementDistance(), event.getPartialTick(), true);

        if (selectedStructure == null) return;
        BuildableStructure struct = selectedStructure.getLeft();
        BlockPos pos = struct.adjustForPlacement(Objects.requireNonNull(minecraft.level), target);
        Vector3f newPos = new Vector3f(pos.getX(), pos.getY(), pos.getZ());
        oldPos.lerp(newPos, .25f);
        Holograms.drawStructureHologram(minecraft, event.getPoseStack(), event.getPartialTick(), pos, oldPos, selectedDirection, struct, selectedStructure.getRight(), !minecraft.options.hideGui);
    }

    @Override
    public void mouseScroll(InputEvent.MouseScrollEvent event, InteractionHand hand, ItemStack stack) {
        Minecraft minecraft = Minecraft.getInstance();
        Player player = Objects.requireNonNull(minecraft.player);

        if (event.getScrollDelta() != 0) {
            event.setCanceled(true);
            int shift = event.getScrollDelta() > 0 ? 1 : -1;
            if (player.isShiftKeyDown()) {
                ResourceLocation selected = new ResourceLocation(stack.getOrCreateTagElement("Behaviors").getCompound("StructureBuilding").getString("Selected"));

                List<ResourceLocation> structureKeys = Structures.getStructureMap().keySet().stream().toList();
                ResourceLocation newStructure = structureKeys.get(Math.floorMod((structureKeys.indexOf(selected) + shift), structureKeys.size()));
                if (newStructure == selected) return;

                syncSelectedStructure(player, hand, newStructure, selectedDirection);
            } else {
                selectedDirection = shift == 1 ? selectedDirection.getClockWise() : selectedDirection.getCounterClockWise();
                syncSelectedStructure(player, hand, stack);
            }
        }
    }

    private void syncSelectedStructure(Player player, InteractionHand hand, ItemStack stack) {
        ResourceLocation selected =  new ResourceLocation(stack.getOrCreateTagElement("Behaviors").getCompound("StructureBuilding").getString("Selected"));
        syncSelectedStructure(player, hand, selected, selectedDirection);
    }

    private void syncSelectedStructure(Player player, InteractionHand hand, ResourceLocation key, Direction direction) {
        selectedStructure = Structures.getStructure(key);
        if (selectedStructure == null) return;
        player.displayClientMessage(new TranslatableComponent(selectedStructure.getLeft().langKey()).withStyle(Constants.Styles.ACTIONBAR_INFO), true);
        MultitoolSyncHandler.INSTANCE.sendTo(new ServerBoundStructureSelectPacket(key, direction, hand),
                Objects.requireNonNull(Minecraft.getInstance().getConnection()).getConnection(), NetworkDirection.PLAY_TO_SERVER);
    }

    public void handleStructureSelect(ServerPlayer player, InteractionHand hand, ResourceLocation key, Direction direction) {
        ItemStack stack = player.getItemInHand(hand);
        var tag = stack.getOrCreateTagElement("Behaviors").getCompound("StructureBuilding");
        selectedStructure = Structures.getStructure(key);
        tag.putString("Selected", key.toString());
        tag.putString("Direction", direction.getSerializedName());
        stack.getOrCreateTagElement("Behaviors").put("StructureBuilding", tag);
    }

    @Override
    public Component getDisplayName() {
        return Constants.Translations.MULTITOOL_STRUCTURE_BUILDING;
    }

    @Override
    public void drawHudIcon(PoseStack stack, int x, int y) {
        Minecraft.getInstance().getItemRenderer().renderGuiItem(new ItemStack(Items.BRICKS), x - 8, y - 8);
    }
}
